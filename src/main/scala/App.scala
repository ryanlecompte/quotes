import akka.actor.{ActorSystem, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.duration._
import akka.util.Timeout

/**
 * Main application.
 */
object App {
  import QuotesProtocol._
  
  /**
   * Uses actors to fetch quote prices.
   * @param args the command-line arguments
   */
  def main(args: Array[String]) {
    val symbols = Array(
        "AAPL",
        "GOOG",
        "CMCSA",
        "INTC",
        "AA",
        "SWY",
        "AAN",
        "AAV",
        "XOM",
        "ACI",
        "ADT",
        "ADX",
        "ALX",
        "AMD")
        
    implicit val timeout = Timeout(1.minutes)
    val system = ActorSystem("Quotes")
    
    // Submit our quote price request.
    val partitioner = system.actorOf(Props(new QuoteRequestPartitioner(2)))
    val results = partitioner ? FetchQuotes(symbols)
    
    // Handle response and gracefully shutdown actor system.
    results.onSuccess {
      case response => println("Symbol prices: " + response)
      system.shutdown()
    }
  }
}