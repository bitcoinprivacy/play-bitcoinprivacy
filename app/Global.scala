import play.api._
import play.api.mvc._
import scala.concurrent.Future
import play.api.mvc.Results._

object Global extends GlobalSettings {

  override def onHandlerNotFound(request: RequestHeader): Future[SimpleResult] = {
    
    Future.successful(NotFound(views.html.not_found_page("Page not found on the server : " + request.path)))
    
  } 

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(views.html.not_found_page("Bad Request: " + error)))
  }
    
}
