package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Output(hash :String, spent: String ,value: Long) 
case class OutputsInfo(out: Long, in: Long, outputs: Int, inputs: Int, minHeight: Int, maxHeight: Int)

object Output{
  def getOutputs(hex: String, page: Int) = {
    
        DB.withConnection { implicit connection =>
          (SQL(
            "SELECT ifnull(value,0) as balance, hex(transaction_hash) as tx, ifnull(hex(spent_in_transaction_hash),'') " + 
              " as spent_in_tx FROM movements WHERE address = X'"+hex+"' limit " + (pageSize*(page-1)) +","+pageSize
          )() map {row => Output(
            row[String]("tx"), 
            row[String]("spent_in_tx"), 
            row[Long]("balance")
          )}).toList
      
        }
    
  }

  def getOutputsPage(hex: String, page: Int) = {
    
      val query = "select floor(("+(pageSize-1)+"+count(*))/"+pageSize+") as c from movements where address = X'"+hex+"'"
      
      DB.withConnection { implicit connection =>
        (SQL(query

        )() map {row => Pagination(
          page,
          row[Int]("c"),
          pageSize
        )}).head
      }
    
  }

  def getOutputsInfo(hex: String) = {
    
      val query = "select "+
      "sum( CASE WHEN spent_in_transaction_hash IS NOT NULL THEN value ELSE 0 END) AS a," +
      "sum( value) AS b," +
      "sum( CASE WHEN spent_in_transaction_hash IS NOT NULL THEN 1 ELSE 0 END) AS c," +
      "count(*) AS d,"  +
      " max(block_height) as ma,"+
      " min(block_height) as mi"+
      " from movements where address = X'"+hex+"'"
      
      DB.withConnection { implicit connection =>
        (SQL(query

        )() map {row => OutputsInfo(
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
