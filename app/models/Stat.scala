package models

import anorm._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import anorm.SqlParser._
import anorm.Column.{ columnToArray}
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams

case class Stat(block_height: Int,total_bitcoins_in_addresses:Int,total_transactions: Int,total_addresses:Int,total_closures:Int, total_addresses_with_balance: Int,
  total_closures_with_balance: Int, total_addresses_no_dust: Int,total_closures_no_dust:Int,gini_closure: Double,gini_address:Double, tstamp: Long)

case class ServerStat(tstamp: Long, blocks: Int, duration: Long, averageDuration: Long, averageBlocks : Int, databaseSize: Long,
  lastCommand: String, users: Int, clicks: Int)

case class Distribution(addresses: Int, satoshis: Long)

object Stat{
  implicit val statReads = Json.reads[Stat]
  implicit val distribution = Json.reads[Distribution]

  def getStats(height: Int): Future[Stat] = WS.url("http://bitcoinprivacy.net:8080/stats").get().map {response => (response.json).as[Stat]}

  def getServerStats(height: Int) = Future{ServerStat(1L,1,10L,10L,10,10L,"lala", 10, 10)}
  
  def getDistribution(value: Double, blockHeight: Int): Future[Distribution] = WS.url("http://bitcoinprivacy.net:8080/distribution/"+(value*100000000).toInt).get().map {response => (response.json).as[Distribution]}

}
