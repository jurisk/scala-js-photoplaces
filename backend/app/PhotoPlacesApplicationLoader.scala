import controllers.{Application, Assets}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.{ApplicationLoader, BuiltInComponentsFromContext}
import router.Routes

class PhotoPlacesApplicationLoader() extends ApplicationLoader {
  def load(context: Context) = new ApplicationComponents(context).application
}

class ApplicationComponents(context: Context) extends BuiltInComponentsFromContext(context) with AhcWSComponents {
  lazy val applicationController = new Application(wsClient, configuration)(environment)
  lazy val assets = new Assets(httpErrorHandler)
  override lazy val router = new Routes(httpErrorHandler, assets, applicationController)
}
