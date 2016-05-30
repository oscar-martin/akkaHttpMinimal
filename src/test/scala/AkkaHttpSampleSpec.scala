import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{WordSpec, Matchers}
import spray.json._
import DefaultJsonProtocol._

class AkkaHttpSampleSpec extends WordSpec with Matchers with ScalatestRouteTest with MyJsonProtocol {

    val akkaHttpSample = new AkkaHttpSample()

    "AkkaHttpSample service" should {
        "return a json object with an error when receiving GETs to the root path" in {
            Get() ~> akkaHttpSample.routes ~> check {
                status shouldEqual StatusCodes.Forbidden
                responseAs[Result] shouldEqual Result("Failure", "")
            }
        }

        "return a json object without any error when POSTing to 'service/test with a Request json object" in {
            Post("/service/test", Request("one", Array("two", "three"))) ~> akkaHttpSample.routes ~> check {
            //postRequest ~> check {
                status shouldEqual StatusCodes.OK
                responseAs[Result] shouldEqual Result("Success", "data-here")
            }
        }

        "return a not found error when requesting anythin else" in {
            Get("/does-not-exist") ~> akkaHttpSample.routes ~> check {
                status shouldEqual StatusCodes.NotFound
                responseAs[String] shouldEqual "Not found"
            }
        }
    }
}
