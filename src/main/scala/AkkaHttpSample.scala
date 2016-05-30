import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.event.{Logging, LoggingAdapter}

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import sun.misc.Signal
import sun.misc.SignalHandler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import DefaultJsonProtocol._

case class Request(param1: String, param2: Array[String])
case class Result(status: String, data: String)

trait MyJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val requestFormat = jsonFormat2(Request)
    implicit val resultFormat = jsonFormat2(Result)
}

class AkkaHttpSample extends MyJsonProtocol {
    val log = LoggerFactory.getLogger(getClass)
    val SERVICE_NAME = "[test-service]"

    val routes =
        (pathSingleSlash & get) {
            complete(StatusCodes.Forbidden, Result("Failure", ""))
        } ~
        (path("service" / "test") & post) {
            entity(as[Request]) { request =>
                log.info(s"Received: param1=${request.param1} param2=${request.param2.mkString(",")}")
                complete(StatusCodes.OK, Result("Success", "data-here"))
            }
        } ~
        // handled fallback
        complete((StatusCodes.NotFound, "Not found"))

}

object AkkaHttpSample extends App {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val log: LoggingAdapter = Logging(system, getClass)
    implicit val executionContext = system.dispatcher

    val httpConfig = ConfigFactory.load().getConfig("http")
    val interface = httpConfig.getString("interface")
    val port = httpConfig.getInt("port")

    val router = new AkkaHttpSample()
    val bindingFuture = Http().bindAndHandle(router.routes, interface, port)

    log.info(s"$AkkaHttpSample.SERVICE_NAME online at http://$interface:$port/ ...")

    Signal.handle(new Signal("INT"), new SignalHandler() {
        def handle(sig: Signal) {
            log.info(s"Gracefully $AkkaHttpSample.SERVICE_NAME Service shutdown")
            bindingFuture
                .flatMap(_.unbind()) // trigger unbinding from the port
                .onComplete(_ ⇒ system.terminate()) // and shutdown when done
        }
    })

}