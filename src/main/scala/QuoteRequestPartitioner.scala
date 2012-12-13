import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinRouter

/**
 * QuoteRequestPartitioner is responsible for handling quote requests and 
 * merging their results.
 */
class QuoteRequestPartitioner(partitionSize: Int) extends 
  Actor with ActorLogging {
  
  import QuotesProtocol._
 
  // Used to keep track of unique quote requests. In a production system,
  // we could use something like Twitter's snowflake project to generate
  // unique identifiers without hitting the database or worrying about
  // running out of unique values.
  private[this] var uniqueIdGenerator = 1L
  // Used to keep track of when we have successfully processed all partitions
  // for a particular actor.
  private[this] var outstandingRequests = Map.empty[Long, List[Map[String, Double]]]
  // Used to balance requests across multiple actors.
  private[this] val quoteRouter = context.actorOf(Props[QuoteFetcher].
      withRouter(RoundRobinRouter(nrOfInstances = 5)))

  override def preStart() {
    log.info("Successfully created with partition size {}.", partitionSize)
  }
  
  /**
   * Handles incoming messages.
   */
  override def receive = {
    case FetchQuotes(symbols) => fetchWithPartitions(symbols)
    case QuotePartitionResults(requestor, requestId, quotePrices, partitionNum, totalPartitions) =>
      val currentPrices = outstandingRequests.getOrElse(requestId, List.empty) :+ quotePrices
      outstandingRequests += requestId -> currentPrices
      if (currentPrices.size == totalPartitions) {
        // we have all of our partitions - let's send the final results
        requestor ! AggregatedQuotes(merge(outstandingRequests(requestId)))
        // remove the entry for this actor
        outstandingRequests -= requestId
      }
  }
  
  /**
   * Dispatches partitioned/grouped requests.
   * @param symbols the symbols to partition
   */
  private def fetchWithPartitions(symbols: Seq[String]) {
    val groups = symbols.grouped(partitionSize).toSeq
    val requestId = nextId()
    log.info("Dispatching {} quote request partitions.", groups.size)
    groups.zipWithIndex.foreach { case (partition, index) =>
      quoteRouter ! FetchQuotesPartition(sender, requestId, partition, index + 1, groups.size)
    }
  }
  
  /**
   * Merges multiple maps into a single map.
   * @param maps the list of maps to merge
   * @return a single/merged map
   */
  private def merge[A,B](maps: List[Map[A,B]]): Map[A,B] = {
    maps.foldLeft(Map.empty[A,B]) { _ ++ _ } 
  }

  /**
   * Increments and returns the next id.
   * @return the id
   */
  private def nextId() = {
    uniqueIdGenerator += 1
    uniqueIdGenerator
  }
}