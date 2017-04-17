import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import com.monsanto.arch.kamon.prometheus.akka_http.AkkaHttpEndpoint
import kamon.Kamon

object ServerRoutes {

    def apply()(implicit system: ActorSystem) = new ServerRoutes() with DefaultServiceComponent
}

class ServerRoutes(implicit system: ActorSystem) extends EntitiesJsonProtocols {
    this: ServiceComponent =>

    val Logger = Logging(system, getClass)
    val rootGets = Kamon.metrics.counter("GET /")
    val serviceTestPosts = Kamon.metrics.counter("POST service/test")
    val serviceTestGets = Kamon.metrics.counter("GET service/test")
    val otherRequests = Kamon.metrics.counter("Other requests")

    val routes =
        (pathSingleSlash & get) {
            rootGets.increment()
            complete(StatusCodes.Forbidden, Result("Failure", ""))
        } ~
        path("metrics") {
            AkkaHttpEndpoint(system).route
        } ~
            path("service" / "test") {
                post {
                    entity(as[Request]) { request =>
                        serviceTestPosts.increment()
                        Logger.info(s"Received: param1=${request.param1} param2=${request.param2.mkString(",")}")
                        complete(StatusCodes.OK, serviceProvider.saveThing(request))
                    }
                } ~
                get {
                    // http://doc.akka.io/docs/akka/2.4.5/scala/http/routing-dsl/directives/parameter-directives/parameters.html
                    parameter('strParam, 'intParam.as[Int], 'optionalParam.?, 'listParam.*) { (strParam, intParam, optionalParam, listParam) =>
                        serviceTestGets.increment()
                        complete(StatusCodes.OK, serviceProvider.getThing(strParam, intParam, optionalParam, listParam.toList))
                    }
                }
        } ~ {
            otherRequests.increment()
            // handled fallback
            complete((StatusCodes.NotFound, "Not found"))
        }
}
