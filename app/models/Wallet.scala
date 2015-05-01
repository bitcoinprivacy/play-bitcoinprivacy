package models

import play.api.libs.json._

import anorm._
import anorm.SqlParser._
import anorm.Column.{ columnToArray}
import scala.concurrent.Future
import org.bitcoinj.core.Address
import org.bitcoinj.params.MainNetParams

object Wallet
 {

  def valueOf(buf: Array[Byte]): String = buf.map("%02X" format _).mkString
 
  implicit def rowToByteArray: Column[Array[Byte]] = {
  Column.nonNull[Array[Byte]] { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case bytes: Array[Byte] => Right(bytes)
      case _ => Left(TypeDoesNotMatch("..."))
    }
  }
  }

  def get(strAddress:String) = Future {
    val address = new Address(MainNetParams.get, strAddress)
    val hexAddress = (if(address.isP2SHAddress) "05" else "00")+valueOf(address.getHash160)

    DB.withConnection { implicit connection =>
      (SQL(
        """
          SELECT
            hash as hash, balance
          FROM addresses
          WHERE balance > 0 and representant = 
          (SELECT representant FROM addresses where hash=X'"""+hexAddress+"""');
        """
      )() map {row => (hashToAddress(row[Array[Byte]]("hash")), row[Option[Long]]("balance").getOrElse(0L))}).toList
    }
  }

  def hashToAddress(hash: Array[Byte]): String = {
    if (hash.length==21)
      new Address(MainNetParams.get,hash.head.toInt,hash.tail).toString
    else "todo"
  }

  def getBlockHeight = {
    DB.withConnection{ implicit connection => 
      SQL("select max(block_height) as c from blocks")().head[Int]("c")
    }
  }

}
