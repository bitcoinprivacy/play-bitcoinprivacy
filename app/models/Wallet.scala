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
  
  def getBlocks(number: Int) = Future {

    DB.withConnection { implicit connection => 
      (SQL(
        "select hex(hash) as hash, block_height from blocks order by block_height desc limit "+number)() map {
      row => (row[String]("hash"), row[Int]("block_height"))}).toList
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

  
  def getTransactions(height: Int) = Future {

    DB.withConnection { implicit connection =>
      (SQL(
        "SELECT  sum(value) as balance, hex(transaction_hash) as address FROM movements WHERE block_height = "+height+" GROUP BY transaction_hash")() map {                                                                                               
      row => (row[String]("address"), row[Int]("balance"))}).toList                                                                                                                                   

    }
  }

  def getAddressMovements(strAddress: String) = Future  {
    val address = new Address(MainNetParams.get, strAddress)
    val hexAddress = (if(address.isP2SHAddress) "05" else "00")+valueOf(address.getHash160)
    DB.withConnection { implicit connection =>
      (SQL(
        "SELECT ifnull(value,0) as balance, hex(transaction_hash) as address, hex(spent_in_transaction_hash) as spent_tx FROM movements WHERE address = X'"+hexAddress+"'"
      )() map {row => (row[String]("address"), row[String]("spent_tx"), row[Int]("balance"))}).toList
    }
  }

def getMovements(txHash: String) = Future {
  DB.withConnection { implicit connection => 
    (SQL(
      "SELECT  value as balance, address as address, hex(spent_in_transaction_hash) as spent_in, 'x' as paid_in FROM  movements WHERE  transaction_hash = X'"+txHash+"'" +
        " union " +
      "SELECT value as balance, address as address, 'x' as spent_in, hex(transaction_hash) as paid_in FROM movements WHERE  spent_in_transaction_hash = X'"+txHash+"'"
    )() map {row => (hashToAddress(row[Array[Byte]]("address")), row[String]("paid_in"),row[String]("spent_in"), row[Int]("balance"))}).toList
  }
}
}


