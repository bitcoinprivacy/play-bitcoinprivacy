package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Transaction(hash:String,value: Long) 
case class TransactionInfo(value: Long, tx: Int)

object Transactions{

  def getTransactions(height: Int, page: Int) = Future {
      DB.withConnection { implicit connection =>
        (SQL(
          "SELECT  sum(value) as balance, hex(transaction_hash) as address FROM movements WHERE block_height = "+height+" GROUP BY transaction_hash limit "+pageSize+"*"+(page-1)+", "+pageSize
        )() map {row => Transaction(
          row[String]("address"),
          row[Long]("balance"))
        }).toList
      }
  }

  def getTransactionPage(height: Int, page: Int) = Future {
    DB.withConnection { implicit connection =>
      (SQL(
        "select count(distinct(transaction_hash)) as c from movements where block_height = "+height
      )() map {row => Pagination(
        page,
        (row[Int]("c")+pageSize-1)/pageSize,
        pageSize
      )}).head
    }
  }

  def getTransactionInfo(height: Int) = Future {
    DB.withConnection { implicit connection =>
      (SQL(
        "select count(distinct(transaction_hash)) as count, sum(value) as value from movements where block_height = "+height
      )() map {row => TransactionInfo(
        row[Long]("value"), 
        row[Int]("count")
      )}).head
    }
  }
}
