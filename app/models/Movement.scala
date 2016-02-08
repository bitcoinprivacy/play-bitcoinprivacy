package models

import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Movement(tx:String, value: Long, spentInTx: String)
case class MovementsInfo(inputs: Long, outputs: Long, inputSum: Long, outputSum: Long, height: Int)

object Movement{

  implicit val movementReads = Json.reads[Movement]

  def getOutputsInfo(txHash: String, height: Int) = Future{MovementsInfo(1,1,1,1,1)}

  def getInputsInfo(txHash: String, height: Int) = Future{MovementsInfo(1,1,1,1,1)}

  def getOutputs(txHash: String, height: Int, from: Int, to: Int) = getFromApi("outputs",  txHash, from.toString, to.toString) map {response => (response.json).as[List[Movement]]}

  def getInputs(txHash: String, height: Int, from: Int, to: Int) = getFromApi("inputs",  txHash, from.toString, to.toString) map {response => (response.json).as[List[Movement]]}

  def getInfoFromAddress(ad: String, height: Int) = Future{MovementsInfo(1,1,1,1,1)}

  def getFromAddress(ad: String, height: Int, from: Int, to: Int) = getFromApi("movements",  ad, from.toString, to.toString) map {response => (response.json).as[List[Movement]]}

}
