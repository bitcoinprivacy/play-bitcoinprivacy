
package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms.{single, nonEmptyText}
import models.Wallet

object Application extends Controller {

  val addressForm = Form(
    single("address" -> nonEmptyText)
  )
 
 
  def index = Action {
    Ok(views.html.index(Wallet.getBlockHeight, addressForm, None))
  }
 
  def searchAddress = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      errors => scala.concurrent.Future{BadRequest},
      {
        case (address) =>
          Wallet.get(address) map {a =>
            Ok(views.html.index(Wallet.getBlockHeight, addressForm, Some(a)))
          }
      }
    )
  }
}
