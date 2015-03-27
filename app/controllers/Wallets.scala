package controllers

import models.Wallet

import play.api.Logger
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

object Wallets extends Controller {

  def get(address:String) = Action.async {

    Wallet.get(address) map {
      case wallet:Stream[String] => Ok(Json.toJson(wallet))
      case _ => NoContent
    }
  }

}
