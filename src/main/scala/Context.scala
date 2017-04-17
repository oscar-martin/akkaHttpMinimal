import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.ActorMaterializer

object Context {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val log: LoggingAdapter = Logging(system, getClass)
    implicit val executionContext = system.dispatcher

}
