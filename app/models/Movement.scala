package models

import play.api.libs.json._
import play.api.libs.ws._
import scala.concurrent.Future

case class Movement(tx:String, value: Long, spentInTx: String, address: String)
case class MovementsSummary(sum: Long, count: Long, maxHeight: Int, minHeight: Int)

object Movement{

  implicit val movementReads = Json.reads[Movement]

  def getOutputs(txHash: String, height: Int, from: Int, to: Int) =
    getFromApi("outputs",  txHash, from.toString, to.toString).
      map (_.json.as[List[Movement]])

  def getInputs(txHash: String, height: Int, from: Int, to: Int) =
    getFromApi("inputs",  txHash, from.toString, to.toString).
      map {_.json.as[List[Movement]]}

  def getFromAddress(ad: String, height: Int, from: Int, to: Int) =
    getFromApi("movements",  ad, from.toString, to.toString).
      map {_.json.as[List[Movement]]}

}

object MovementsSummary{

  implicit val movementsSummaryReads = Json.reads[MovementsSummary]

  def getOutputsInfo(txHash: String, height: Int) =
    getFromApi("outputs", txHash, "summary").
      map(_.json.as[MovementsSummary])

  def getInputsInfo(txHash: String, height: Int) =
  getFromApi("inputs", txHash, "summary").
    map(_.json.as[MovementsSummary])

  def getInfoFromAddress(ad: String, height: Int) =
  getFromApi("movements", ad, "summary").
    map(_.json.as[MovementsSummary])
}
