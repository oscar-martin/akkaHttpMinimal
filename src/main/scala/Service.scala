
trait ServiceComponent {

    val serviceProvider: Service

    trait Service {
        def saveThing(request: Request): Result

        def getThing(strParam: String, intParam: Int, optParam: Option[String], listParam: List[String]): Result
    }
}

trait DefaultServiceComponent extends ServiceComponent {
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