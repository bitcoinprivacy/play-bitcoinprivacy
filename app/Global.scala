import play.api._
import play.api.mvc._
import scala.concurrent.Future._
import scala.concurrent._
import play.api.mvc.Results._
import controllers._

object Global extends GlobalSettings {

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {   
    Future.successful(NotFound(views.html.not_found_page("Page not found on the server : " + request.path, addressForm)))    
  } 

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest(views.html.not_found_page("Bad Request: " + error, addressForm)))
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(
        views.html.not_found_page("exception " + ex, addressForm)
    ))
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
  //if ( !request.headers.get("x-forwarded-proto").getOrElse("").contains("https")) {
  //  Some(Secure.redirect)
  //} else {
    super.onRouteRequest(request)
  //}
  }
}
