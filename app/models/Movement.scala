package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Movement(address:String, hash: String, value: Long) 
case class MovementsInfo(inputs: Long, outputs: Long, in: Long, out: Long, height: Int)

object Movement{
  
  def getMovementsInfo(txHash: String, height: Int) = {
      DB.withConnection { implicit connection =>
        (SQL(
          " select" +
            " ifnull((select count(*) from movements where transaction_hash = X'"+txHash+"'),0) as a, " +
            " ifnull((select count(*) from movements where spent_in_transaction_hash = X'"+txHash+"'),0) as b, " +
            " ifnull((select ifnull(sum(ifnull(`value`,0)),0) from movements where transaction_hash = X'"+txHash+"'),0) as d, " +
            " ifnull((select ifnull(sum(ifnull(`value`,0)),0) from movements where spent_in_transaction_hash = X'"+txHash+"'),0) as c," +
            " ifnull((select block_height from movements where transaction_hash = X'"+txHash+"' limit 1),0) as h"
        )() map {row => MovementsInfo(
          row[Int]("a"),
          row[Int]("b"),
          row[Long]("c"),
          row[Long]("d"),
          row[Int]("h")
        )}).head

    }
  }


  def getMovements(txHash: String, height: Int, page: Int) = {
    val query = " SELECT  ifnull(value, 0) as  balance, address as address, ifnull(hex(spent_in_transaction_hash),'') as tx " +
              " FROM  movements WHERE  transaction_hash = X'"+txHash+"' limit "+(pageSize*(page-1))+","+pageSize 
    val query2 = " SELECT ifnull(n.value,0) as balance, ifnull(n.address,X'') as address, hex(n.transaction_hash) as tx" +
              " FROM movements n left outer join movements m "+
              " on m.transaction_hash = n.spent_in_transaction_hash " +
              " WHERE n.spent_in_transaction_hash = X'"+txHash+"'" +
              " GROUP BY address " +
              " LIMIT " + (pageSize*(page-1)) + ","+pageSize
            
    
    DB.withConnection { implicit connection =>
      (
        (SQL(query2

        )() map {row => Movement(
          hashToAddress(row[Array[Byte]]("address")),
          row[String]("tx"),
          row[Long]("balance")
        )}).toList,
        (SQL(query

        )() map {row => Movement(
          hashToAddress(row[Array[Byte]]("address")),
          row[String]("tx"),
          row[Long]("balance")
        )}).toList
      )
    }
  }

  def getMovementsPage(txHash: String, height: Int) = {
    DB.withConnection {implicit connection =>
      (SQL(
        "select " +
          "  (select count(*) from movements where transaction_hash = X'"+ txHash + "') as a," +
          "  (select count(*) from movements where transaction_hash = X'"++"') as b"
      )() map {row => Pagination(
        
        Math.max(row[Int]("a"), row[Int]("b")),
        pageSize
      )}).head
    }
  }
}
