package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Output(hash :String, spent: String ,value: Long) 
case class OutputsInfo(out: Long, in: Long, outputs: Int, inputs: Int, minHeight: Int, maxHeight: Int)

object Output{
  def getOutputs(hex: String, height: Int, page: Int) = { 
    val query = 
      "SELECT ifnull(value,0) as balance, ifnull(hex(transaction_hash),'') as tx, ifnull(hex(spent_in_transaction_hash),'') " +
      " as spent_in_tx FROM movements WHERE address = X'"+hex+"' limit " + (pageSize*(page-1)) +","+pageSize;
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => Output(
        row[String]("tx"),
        row[String]("spent_in_tx"),
        row[Long]("balance")
      )}).toList
    }
  }

  def getOutputsPage(hex: String, height: Int) = {    
    val query = "select count(*) as c from movements where address = X'"+hex+"'"
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => Pagination(
        row[Int]("c"),
        pageSize
      )}).head
    }
  }

  def getOutputsInfo(hex: String, height: Int) = {    
    val query = "select "+
      "ifnull(sum( CASE WHEN spent_in_transaction_hash IS NOT NULL THEN ifnull(value,0) ELSE 0 END),0) AS a," +
      "ifnull(sum( ifnull(value,0)),0) AS b," +
      "ifnull(sum( CASE WHEN spent_in_transaction_hash IS NOT NULL THEN 1 ELSE 0 END),0) AS c," +
      "count(*) AS d,"  +
      " ifnull(max(block_height),0) as ma,"+
      " ifnull(min(block_height),0) as mi"+
      " from movements where address = X'"+hex+"'"
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => OutputsInfo(
        row[Long]("a"),
        row[Long]("b"),
        row[Int]("c"),
        row[Int]("d"),
        row[Int]("mi"),
        row[Int]("ma")
      )}).head
    }
  }
}
