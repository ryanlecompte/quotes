import akka.actor.{ActorSystem, ActorPath, ActorRef, Props}
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.dispatch.{Future, Await}
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.{pipe, ask}
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers

class QuoteFetcherSpec extends TestKit(ActorSystem("QuoteFetcherSpec"))
       with ImplicitSender
       with WordSpec
       with BeforeAndAfterAll
       with MustMatchers {

  import QuotesProtocol._
  
  override def afterAll() {
    system.shutdown()
  }

  "QuoteFetcher" should {
    "fetches a single symbol" in {
      val fetcher = TestActorRef[QuoteFetcher]
      val symbols = Seq("XOM")
      fetcher ! FetchQuotesPartition(testActor, 1L, symbols, 1, 1)
      fishForMessage(1 minute) {
        case QuotePartitionResults(_, _, quotePrices, _, _)  =>
          quotePrices.contains(symbols.head)
      }
    }
    
    
    "fetches multiple symbols" in {
      val fetcher = TestActorRef[QuoteFetcher]
      val symbols = Seq("XOM", "GOOG")
      fetcher ! FetchQuotesPartition(testActor, 1L, symbols, 1, 1)
      fishForMessage(1 minute) {
        case QuotePartitionResults(_, _, quotePrices, _, _)  =>
          symbols.forall(quotePrices.contains)
      }
    }
  }
}