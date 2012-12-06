import akka.actor.{ActorSystem, ActorPath, ActorRef, Props}
import akka.testkit.{TestActorRef, TestKit, ImplicitSender}
import akka.dispatch.{Future, Await}
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.{pipe, ask}
import org.scalatest.{WordSpec, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers

class QuoteRequestPartitionerSpec extends TestKit(ActorSystem("QuoteRequestPartitionerSpec"))
       with ImplicitSender
       with WordSpec
       with BeforeAndAfterAll
       with MustMatchers {

  import QuotesProtocol._
  
  override def afterAll() {
    system.shutdown()
  }

  "QuoteRequestPartitionerSpec" should {
    "properly handles a request to fetch multiple quotes" in {
      val partitioner = TestActorRef(new QuoteRequestPartitioner(2))
      val symbols = Seq("XOM", "GOOG", "AAPL", "AAV");
      partitioner ! FetchQuotes(symbols)
      fishForMessage(1 minute) {
        case AggregatedQuotes(quotePrices) =>
          quotePrices.size == symbols.size && symbols.forall(quotePrices.contains)
      }
    }
  }
}