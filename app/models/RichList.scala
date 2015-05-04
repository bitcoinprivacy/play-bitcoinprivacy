package models

import play.api.libs.json._

import anorm._
import anorm.SqlParser._
import anorm.Column.{ columnToArray}
import scala.concurrent.Future
import org.bitcoinj.core.Address
import org.bitcoinj.params.MainNetParams

object RichList
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


def getRichList(blockHeight: Int, table: String) = Future {
    DB.withConnection { implicit connection =>
      (SQL(
        """
          select hash, ifnull(balance,0) as balance from """+table+""" where hash is not null and  block_height = (select max(block_height) from """+table+""");
        """
      )() map {row => (hashToAddress(row[Option[Array[Byte]]]("hash").getOrElse(Array.empty)), row[Long]("balance"))}).toList
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

