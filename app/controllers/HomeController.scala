package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.ws._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(
  val controllerComponents: ControllerComponents,
  ws: WSClient
)(implicit ec: ExecutionContext) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def data() = Action.async { implicit request: Request[AnyContent] =>
    val githubApi = "https://api.github.com/user/repos"
    val tokenOpt = sys.env.get("GITHUB_TOKEN")
    val req = ws.url(githubApi).addHttpHeaders(
      "Accept" -> "application/vnd.github.v3+json"
    ) ++ tokenOpt.map(t => Seq("Authorization" -> s"token $t")).getOrElse(Seq())
    req.get().map { response =>
      val repos = response.json.as[JsArray].value.map { repo =>
        val name = (repo \ "name").head.as[String]
        val url = (repo \ "html_url").head.as[String]
        (name, url)
      }
      Ok(views.html.data(repos))
    }
  }
}
