import akka.http.scaladsl.model._
import akka.http.scaladsl.server.MissingQueryParamRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Inside, Matchers, WordSpec}

trait TestServiceComponent extends ServiceComponent {
    val serviceProvider = new ServiceImpl

    class ServiceImpl extends Service {
        def saveThing(request: Request): Result = {
            Result("Success", "data-here")
        }

        def getThing(strParam: String, intParam: Int, optParam: Option[String], listParam: List[String]): Result = {
            Result(strParam, s"$intParam")
        }
    }
}

class AkkaHttpSampleSpec extends WordSpec with Matchers with ScalatestRouteTest with EntitiesJsonProtocols with Inside {

    "AkkaHttpSample service" should {
        "return a json object with an error when receiving GETs to the root path" in {
            val serverRoutes = new ServerRoutes() with TestServiceComponent
            Get() ~> serverRoutes.routes ~> check {
                status shouldEqual StatusCodes.Forbidden
                responseAs[Result] shouldEqual Result("Failure", "")
            }
        }

        "return a json object without any error when GETing 'service/test entity" in {
            val strParam = "hey"
            val intParam = 3
            val listParam = "uno" :: "dos" :: Nil
            val listParamStr = listParam.map { p => s"list=$p" }.mkString("&")

            val response =  Result(strParam, s"$intParam")
            val serverRoutes = new ServerRoutes() with TestServiceComponent
            Get(s"/service/test?strParam=$strParam&intParam=$intParam&$listParamStr") ~> serverRoutes.routes ~> check {
                status shouldEqual StatusCodes.OK
                responseAs[Result] shouldEqual response
            }
        }

        "return a json object without any error when POSTing to 'service/test with a Request json object" in {
            val request = Request("one", Array("two", "three"))
            val response =  Result("Success", "data-here")

            val serverRoutes = new ServerRoutes() with TestServiceComponent
            Post("/service/test", request) ~> serverRoutes.routes ~> check {
                status shouldEqual StatusCodes.OK
                responseAs[Result] shouldEqual response
            }
        }

        "return a not found error when requesting anythin else" in {
            val serverRoutes = new ServerRoutes() with TestServiceComponent
            Get("/does-not-exist") ~> serverRoutes.routes ~> check {
                status shouldEqual StatusCodes.NotFound
                responseAs[String] shouldEqual "Not found"
            }
        }

//        "reject a GET without a mandatory query parameter" in {
//            val intParam = 3
//            val listParam = "uno" :: "dos" :: Nil
//            val listParamStr = listParam.map { p => s"list=$p" }.mkString("&")
//
//            val serverRoutes = new ServerRoutes() with TestServiceComponent
//            Get(s"/service/test?intParam=$intParam&$listParamStr") ~> serverRoutes.routes ~> check {
//                inside(rejection) {
//                    case MissingQueryParamRejection("strParam") ⇒
//                }
//            }
//        }

    }
}
