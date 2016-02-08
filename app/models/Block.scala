package models

import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException
import play.api.cache.Cache
import scala.io.Source

abstract class Model
abstract class Info

case class Pagination(total: Int, size: Int)
case class BlocksInfo(total: Int, txs: Long)
case class Block(hash: String, height: Int, tx: Long, value: Long, tstamp: Long) extends Model


object Block{
  
  implicit val blockReads = Json.reads[Block]

  def getBlocks(height: Int, page: Int): Future[List[Block]] = WS.url("http://bitcoinprivacy.net:8080/blocks/" + pageSize*(page-1) + "/" + pageSize*page).get().map {response => (response.json).as[List[Block]]}

  def getBlocksInfo(height: Int) = WS.url("http://bitcoinprivacy.net:8080/stats").get().map{r =>  BlocksInfo((r.json \ "block_height").as[Int], (r.json \ "total_transactions").as[Int])}

  def getBlocksPage(height: Int) = WS.url("http://bitcoinprivacy.net:8080/stats").get().map{r => Pagination((r.json \ "block_height").as[Int],pageSize)}

  def getBlockHeight = WS.url("http://bitcoinprivacy.net:8080/stats").get().map{response => (response.json \ "block_height").as[Int] }
}

