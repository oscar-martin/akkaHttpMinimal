import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Request(param1: String, param2: Array[String])
case class Result(status: String, data: String)

trait EntitiesJsonProtocols extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val requestFormat = jsonFormat2(Request)
    implicit val resultFormat = jsonFormat2(Result)
}
