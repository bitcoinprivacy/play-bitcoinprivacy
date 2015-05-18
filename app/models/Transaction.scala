package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Transaction(hash:String,value: Long) 
case class TransactionInfo(value: Long, tx: Int, tstamp: Long)

object Transaction{

  def getTransactions(height: Int, blockHeight: Int, page: Int) = {    
    val query = 
      "SELECT sum(value) as balance, hex(transaction_hash) as address FROM movements WHERE "+ height +" <= " + blockHeight + " and block_height = "+height+
      " GROUP BY transaction_hash limit "+(pageSize*(page-1))+", "+pageSize
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => Transaction(
        row[String]("address"),
        row[Long]("balance"))
      }).toList
    }    
  }

  def getTransactionPage(height: Int) ={
    val query = "select count(distinct(transaction_hash)) as c from movements where block_height = "+height
    DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Pagination(
          row[Int]("c"),
          pageSize
        )}).head
      }
    
  }

  def getTransactionInfo(height: Int) = {
    val query =
      "select ifnull(count(distinct(transaction_hash)),0) as count, ifnull(sum(ifnull(value,0)),0) as value, ifnull((select tstamp from blocks " +
      " where block_height = "+height+"),0) as tstamp  from movements where block_height = "+height
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => TransactionInfo(
        row[Long]("value"),
        row[Int]("count"),
        row[Long]("tstamp")
      )}).head
    }
  }
}
