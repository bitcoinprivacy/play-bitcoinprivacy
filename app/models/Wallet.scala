package models

import play.api.libs.json._

import anorm._
import anorm.SqlParser._
import scala.concurrent.Future


object Wallet
 {

//  implicit val jsonFormat = Json.format[List[String]]

 
  def get(address:String) = Future {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT
            hex(address)
          FROM addresses
          WHERE representant = 
          (SELECT representant FROM addresses where address=$address);
        """
      )() map {row => row[String]("address")} 
           }
  }

}
