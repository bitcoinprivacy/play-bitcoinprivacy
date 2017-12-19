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

case class BlocksInfo(total: Int, txs: Long)
case class Block(hash: String, height: Int, tx: Long, value: Long, tstamp: Long)


object Block{

  implicit val blockReads = Json.reads[Block]

  def getBlocks(height: Int, from: Int, to: Int): Future[List[Block]] = WS.url(config.getString("api.url")+"blocks/" + from + "/" + to).get().map {response => (response.json).as[List[Block]]}

  def getBlocksInfo(height: Int) = WS.url(config.getString("api.url")+"stats").get().map{r =>  BlocksInfo((r.json \ "block_height").as[Int], (r.json \ "total_transactions").as[Int])}

  def getBlockHeight = WS.url(config.getString("api.url")+"stats").get().map{response => (response.json \ "block_height").as[Int] }
}

