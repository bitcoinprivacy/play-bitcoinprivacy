package controllers

import play.api.mvc._

object Secure extends Controller {

  def redirect = Action { implicit request =>
    MovedPermanently("https://" + request.host + request.uri)
  }
}
