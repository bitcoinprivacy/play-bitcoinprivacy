package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Movement(address:String, hash: String, value: Long) 
case class MovementsInfo(inputs: Long, outputs: Long, in: Long, out: Long)

object Movements{
  
  def getMovementsInfo(txHash: String) = Cached("movements.info"+txHash) {
      DB.withConnection { implicit connection =>
        (SQL(
          " select" +
            " (select count(*) from movements where transaction_hash = X'"+txHash+"') as a, " +
            " (select count(*) from movements where spent_in_transaction_hash = X'"+txHash+"') as b, " +
            " (select sum(`value`) from movements where transaction_hash = X'"+txHash+"') as d, " +
            " (select sum(`value`) from movements where spent_in_transaction_hash = X'"+txHash+"') as c"
        )() map {row => MovementsInfo(
          row[Int]("a"),
          row[Int]("b"),
          row[Long]("c"),
          row[Long]("d")
        )}).head

    }
  }


  def getMovements(txHash: String, page: Int) = Cached("asd") {
    val query = " SELECT  ifnull(value, 0) as  balance, address as address, ifnull(hex(spent_in_transaction_hash),'') as tx " +
              " FROM  movements WHERE  transaction_hash = X'"+txHash+"' limit "+(pageSize*(page-1))+","+pageSize 
    val query2 = " SELECT ifnull(n.value,0) as balance, n.address as address, hex(n.transaction_hash) as tx" +
              " FROM movements n left outer join movements m "+
              " on m.transaction_hash = n.spent_in_transaction_hash " +
              " WHERE n.spent_in_transaction_hash = X'"+txHash+"'" +
              " GROUP BY address " +
              " LIMIT " + (pageSize*(page-1)) + ","+pageSize
            
    println(query)
    println(query2)

    DB.withConnection { implicit connection =>
      (
        (SQL(query

        )() map {row => Movement(
          hashToAddress(row[Array[Byte]]("address")),
          row[String]("tx"),
          row[Long]("balance")
        )}).toList,
        (SQL(query2

        )() map {row => Movement(
          hashToAddress(row[Array[Byte]]("address")),
          row[String]("tx"),
          row[Long]("balance")
        )}).toList
      )
    }
  }

  def getMovementsPage(txHash: String, page: Int) = Cached(txHash) {
    DB.withConnection {implicit connection =>
      (SQL(
        "select " +
          "  (select count(*) from movements where transaction_hash = X'"+ txHash + "') as a," +
          "  (select count(*) from movements where transaction_hash = X'"++"') as b"
      )() map {row => Pagination(
        page,
        (Math.max(row[Int]("a"), row[Int]("b"))+pageSize-1)/pageSize,
        pageSize
      )}).head
    }
  }
}
