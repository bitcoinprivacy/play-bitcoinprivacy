
package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.{single, nonEmptyText}

object Application extends Controller {

  val addressForm = Form(
    single("address" -> nonEmptyText)
  )
 
 
  def index = Action {
    Ok(views.html.index(666, addressForm))
  }
 
  def searchAddress = Action { implicit request =>
    addressForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case (address) =>
          Ok(address)
          Redirect(routes.Application.index())
      }
    )
  }
}
