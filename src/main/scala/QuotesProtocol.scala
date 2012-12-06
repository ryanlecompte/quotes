import akka.actor.ActorRef

/**
 * QuotesProtocol defines the messages that will be sent between actors.
 */
object QuotesProtocol {
  /**
   * FetchQuotes represents a paricular request for symbol price quotes.
   */
  case class FetchQuotes(symbols: Seq[String])
  
  /**
   * FetchQuotesPartition represents a fetch request for a particular partition
   * of quotes.
   * @param requestor the original actor requesting the quote data
   * @param requestId a unique request id for the requesting actor
   * @param symbols the symbols to fetch
   * @param partitionNum the partition number for this message
   * @param totalPartitions the count of all partitions
   */
  case class FetchQuotesPartition(requestor: ActorRef,
      requestId: Long,
      symbols: Seq[String],
      partitionNum: Int,
      totalPartitions: Int)
  
  /**
   * QuotePartitionResults represents data for a particular partition request.
   * @param requestor the original actor requesting the quote data
   * @param requestId a unique request id for the requesting actor
   * @param quotePrices the price data for a particular partition
   * @param partitionNum the partition number for this message
   * @param totalPartitions the count of all partitions
   */
  case class QuotePartitionResults(requestor: ActorRef,
      requestId: Long,
      quotePrices: Map[String, Double],
      partitionNum: Int,
      totalPartitions: Int)
  
  /**
   * AggregatedQuotes represents the final result of all outstanding partitions
   * for a particular request.
   * @param quotePrices the aggregated price data
   */
  case class AggregatedQuotes(quotePrices: Map[String, Double])
}
