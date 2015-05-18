package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException
import play.api.cache.Cache
import scala.concurrent._
abstract class Model
abstract class Info
case class Block(hash: String, height: Int, tx: Long, value: Long, tstamp: Long) extends Model
case class Pagination(total: Int, size: Int)
case class BlocksInfo(total: Int, txs: Long)

object Block{
  def getBlocks(height: Int, page: Int) =     
      DB.withConnection { implicit connection =>
        (SQL(" SELECT " +
          "  hex(hash) as hash,"+
          "  block_height, "+
          "  tstamp, " +
          "  txs, "+
          "  btcs "+
          " FROM " + 
          "  blocks " +
          " WHERE " + 
          "  block_height <= " + (height) + 
          
          " ORDER BY " +
          "  block_height desc " +
          " LIMIT "+((page-1)*pageSize)+","+pageSize)() map {
          row => Block(
            row[String]("hash"),
            row[Int]("block_height"),
            row[Int]("txs"),
            row[Long]("btcs"),
            row[Long]("tstamp")
          )}).toList
  }

  def getBlocksInfo(height: Int) =
    DB.withConnection{implicit connection => (SQL("select count(1) as total, sum(txs) as txs from blocks where block_height <= "+ height)() map {
      row => BlocksInfo(
        row[Int]("total"),
        row[Long]("txs")
      )}).head
    }

  def getBlocksPage(height: Int) =
    DB.withConnection{implicit connection => (SQL("select count(1) as total from blocks where block_height <= "+ height)() map {
      row => Pagination(
        row[Int]("total"),
        pageSize
      )}).head
    }


  def getBlockHeight = 
    DB.withConnection{ implicit connection => SQL("select max(block_height) as c from richest_closures")().head[Int]("c")}

}
