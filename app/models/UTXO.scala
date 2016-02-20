package models

import play.api.libs.json._
import play.api.libs.ws._


case class UTXO(tx: String, value:Long, address: String)
case class UTXOsSummary(count: Int, sum: Long, minHeight: Int, maxHeight: Int)

object UTXO{

  implicit val utxosReads = Json.reads[UTXO]

  def getFromAddress(address: String, height: Int, from: Int, to: Int) =
    getFromApi("utxos", address, from.toString, to.toString).
      map(_.json.as[List[UTXO]])

  def getFromTx(tx: String, height: Int, from: Int, to: Int) =
    getFromApi("tx_utxos", tx, from.toString, to.toString).
      map(_.json.as[List[UTXO]])

}

object UTXOsSummary{

  implicit val utxossummary = Json.reads[UTXOsSummary]

  def getFromAddress(address: String, height: Int) =
    getFromApi("utxos", address, "summary").
      map(_.json.as[UTXOsSummary])

  def getFromTx(tx: String, height: Int) =
    getFromApi("tx_utxos", tx, "summary").
      map(_.json.as[UTXOsSummary])

}
