import java.util.concurrent.Executors

import scala.xml.XML

import akka.actor.{Actor, ActorLogging, ActorRef, Terminated}
import akka.pattern.pipe
import akka.dispatch.{ExecutionContext, Future}

/**
 * QuoteFetcher is responsible for fetching the price of a single quote
 * and returning the result to its sender. This component fetches the
 * quote price asynchronously using futures, however ideally we would use
 * asynchronous I/O here to avoid utilizing thread just for blocking I/O.
 */
class QuoteFetcher extends Actor with ActorLogging {
  import QuotesProtocol._
  
  implicit val ec = context.dispatcher
  val QuotesSourceUrl = "http://www.google.com/ig/api?stock=%s"

  /**
   * Handles incoming messages.
   */
  override def receive = {
    case FetchQuotesPartition(requestor, requestId, symbols, partitionNum, totalPartitions) => 
      fetchPrices(requestor, requestId, symbols, partitionNum, totalPartitions)
  }
  
  /**
   * Asynchronously fetches prices for the specified symbols.
   * @param requestor the requestor
   * @param requestId the request identifier
   * @param symbols the symbols to fetch
   * @param partitionNum the partition number
   * @param totalPartitions the total number of partitions
   */
  private def fetchPrices(requestor: ActorRef,
      requestId: Long,
      symbols: Seq[String],
      partitionNum: Int,
      totalPartitions: Int) {
    log.info("Fetching prices for {} (partition {} out of {})", symbols, partitionNum, totalPartitions)
    Future.sequence(symbols.map(fetchPrice)) map { results =>
      QuotePartitionResults(requestor, requestId, results.toMap, partitionNum, totalPartitions)
    } pipeTo sender
  }
  
  /**
   * Asynchronously fetches the price for a symbol.
   * @param symbol the symbol to fetch
   * @return a future representing the fetch
   */
  private def fetchPrice(symbol: String): Future[(String, Double)] = {
    Future[(String, Double)] {
     val xml = XML.load(QuotesSourceUrl.format(symbol))
     symbol -> (xml \ "finance" \ "last" \ "@data" text).toDouble
    }
  }
}