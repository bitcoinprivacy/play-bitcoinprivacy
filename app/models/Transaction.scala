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
case class TransactionsSummary(value: Long, tx: Int, tstamp: Long)

object Transaction{

  implicit val transactionReads = Json.reads[Transaction]

  def get(height: Int, blockHeight: Int, from: Int, to: Int) =
    getFromApi("txs", height.toString, from.toString, to.toString).
      map (_.json.as[List[Transaction]])

}

object TransactionsSummary{

  implicit val transactionsummaryReads = Json.reads[TransactionsSummary]

  def get(height: Int, blockHeight: Int) =
    getFromApi("txs", height.toString, "summary").
      map(_.json.as[TransactionsSummary])

}
