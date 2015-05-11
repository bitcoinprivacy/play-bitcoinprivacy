package models

import anorm._
import anorm.SqlParser._
import anorm.Column.{ columnToArray}
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams

case class Stat(name: String, value: Any)
case class Distribution(a: Long, b: Long, c:Long)

object Stat{

  def getStats = {
      DB.withConnection { implicit connection =>
        (SQL(
          "select * from stats order by block_height desc limit 1"
        )() map {row => List(
          Stat("Block height in database", row[Int]("block_height")),
          Stat("Total Bitcoins in known addresses", row[Int]("total_bitcoins_in_addresses")),
          Stat("Number of known addresses", row[Int]("total_addresses")),
          Stat("Number of known (partial) wallets", row[Int]("total_closures")),
          Stat("Number of known addresses with a positive balance", row[Int]("total_addresses_with_balance")),
          Stat("Number of wallets with a positive balance", row[Int]("total_closures_with_balance")),
          Stat("Number of known non-dust addresses", row[Int]("total_addresses_no_dust")),
          Stat("Number of known non-dust wallets" , row[Int]("total_closures_no_dust"))
        )}).head
      }

  }

  def getGinis = {

      DB.withConnection { implicit connection =>
        (SQL(
          "select * from stats order by block_height desc limit 1,1;"
        )() map {row => List(
          Stat("Gini coefficient of non-dust addresses", row[Double]("gini_address")),
          Stat("Gini coefficient of non-dust wallets" , row[Double]("gini_closure"))
        )}).head
      }

  }

  def getDistribution(value: Double, blockHeight: Int) = {

      DB.withConnection { implicit connection =>
        val satoshis = 100000000*value;
        (SQL(
          "select count(1) as a, sum(balance)/100000000 as b, (select total_bitcoins_in_addresses from stats order by block_height desc limit 1,1)  as c from addresses where balance > " + satoshis
        )() map {row => Distribution(
          Math.round(row[Option[Long]]("b").getOrElse(0L)), 
          row[Long]("a"), 
          Math.round(10000*row[Option[Long]]("b").getOrElse(0L)/row[Long]("c"))/100
        )}).head
      }  

  }
}
