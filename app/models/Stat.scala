package models

import anorm._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import anorm.SqlParser._
import anorm.Column.{ columnToArray}
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams

case class Stat(block_height: Int,total_bitcoins_in_addresses:Long,total_transactions: Long,total_addresses:Long,total_closures:Long, total_addresses_with_balance: Long,
  total_closures_with_balance: Long, total_addresses_no_dust: Long,total_closures_no_dust:Long,gini_closure: Double,gini_address:Double, tstamp: Long, network: String, dustLimit: Int)

case class ServerStat(tstamp: Long, blocks: Int, duration: Long, averageDuration: Long, averageBlocks : Int, databaseSize: Long,
  lastCommand: String, users: Int, clicks: Int)

case class Distribution(addresses: Int, satoshis: Long)

object Stat{
  implicit val statReads = Json.reads[Stat]
  implicit val distribution = Json.reads[Distribution]

  def getStats(height: Int): Future[Stat] = getFromApi("stats") map {_.json.as[Stat]}

  def getDistribution(value: Double, blockHeight: Int): Future[Distribution] = getFromApi("distribution",(value*100000000).toLong.toString) map {_.json.as[Distribution]}

}
