package models

import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Transaction(hash:String,value: Long) 
case class TransactionInfo(value: Long, tx: Int, tstamp: Long)

object Transaction{
  implicit val transactionReads = Json.reads[Transaction]
  def getTransactions(height: Int, blockHeight: Int, page: Int): Future[List[Transaction]] = WS.url("http://bitcoinprivacy.net:8080/txs/" + height + "/" + pageSize*(page-1) + "/" + pageSize * page) .get().map {response => (response.json).as[List[Transaction]]}

  def getTransactionPage(height: Int) = Future{Pagination(10,1)}

  def getTransactionInfo(height: Int) = Future{TransactionInfo(100L, 1,1)}

}
