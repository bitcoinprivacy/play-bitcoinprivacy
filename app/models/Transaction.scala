package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Transaction(hash:String,value: Long) 
case class TransactionInfo(value: Long, tx: Int)

object Transaction{

  def getTransactions(height: Int, page: Int) = {
    
      val query = "SELECT sum(value) as balance, hex(transaction_hash) as address FROM movements WHERE block_height = "+height+" GROUP BY transaction_hash limit "+(pageSize*(page-1))+", "+pageSize
      
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Transaction(
          row[String]("address"),
          row[Long]("balance"))
        }).toList
      }
    
  }

  def getTransactionPage(height: Int, page: Int) ={
    
      val query = "select count(distinct(transaction_hash)) as c from movements where block_height = "+height
      
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Pagination(
          page,
          (row[Int]("c")+pageSize-1)/pageSize,
          pageSize
        )}).head
      }
    
  }

  def getTransactionInfo(height: Int) = {
    
      val query = "select count(distinct(transaction_hash)) as count, ifnull(sum(ifnull(value,0)),0) as value from movements where block_height = "+height
      
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => TransactionInfo(
          row[Long]("value"),
          row[Int]("count")
        )}).head
      }
    
  }
}
