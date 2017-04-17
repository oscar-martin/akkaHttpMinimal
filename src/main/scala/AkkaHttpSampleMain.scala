import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import kamon.Kamon
import sun.misc.{Signal, SignalHandler}

object AkkaHttpSampleMain extends App {
    import Context._

    val httpConfig = ConfigFactory.load().getConfig("http")
    val interface = httpConfig.getString("interface")
    val port = httpConfig.getInt("port")

    Kamon.start()

    val bindingFuture = Http().bindAndHandle(ServerRoutes().routes, interface, port)

    log.info(s"Online at http://$interface:$port/ ...")

    Signal.handle(new Signal("INT"), new SignalHandler() {
        def handle(sig: Signal) {
            log.info(s"Gracefully Service shutdown")
            Kamon.shutdown()
            bindingFuture
                .flatMap(_.unbind()) // trigger unbinding from the port
                .onComplete(_ ⇒ system.terminate()) // and shutdown when done
        }
    })

}