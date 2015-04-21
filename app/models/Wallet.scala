package models

import play.api.libs.json._

import anorm._
import anorm.SqlParser._
import scala.concurrent.Future
import org.bitcoinj.core.Address
import org.bitcoinj.params.MainNetParams

object Wallet
 {

//  implicit val jsonFormat = Json.format[List[String]]
  def valueOf(buf: Array[Byte]): String = buf.map("%02X" format _).mkString
 
  def get(strAddress:String) = Future {
    val address = new Address(MainNetParams.get, strAddress).getHash160
    val hexAddress = "00"+valueOf(address)
    println(hexAddress)
    DB.withConnection { implicit connection =>
      (SQL(
        """
          SELECT
            hex(hash) as hash, balance
          FROM addresses
          WHERE balance > 0 and representant = 
          (SELECT representant FROM addresses where hash=X'"""+hexAddress+"""');
        """
      )() map {row => (row[String]("hash"), row[Option[Long]]("balance").getOrElse(0L))}).toList
    }
//    val x = cryptic map {row => (/*new Address(MainNetParams.get, */row._1/*).toString*/, row._2.getOrElse(0))}
//    println(x)
//    x
  }
  def getBlockHeight = {
    DB.withConnection{ implicit connection => 
      SQL("select max(block_height) as c from blocks")().head[Int]("c")
    }
  }

}
