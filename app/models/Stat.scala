package models

import anorm._
import anorm.SqlParser._
import anorm.Column.{ columnToArray}
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams

case class Stat(height: Int, bitcoins: Long, addresses: Long, wallets: Long, addressesPositive: Long, walletsPositive: Long, addressesNoDust: Long, walletsNoDust: Long, giniAddresses: Double, giniWallets: Double)
case class Distribution(percent: Double, addresses: Long, bitcoins: Long)

object Stat{

  def getStats(height: Int) = {
    DB.withConnection { implicit connection =>
      (SQL(
        "select * from stats where block_height = " + height
      )() map {row => Stat(
        row[Int]("block_height"),
        row[Int]("total_bitcoins_in_addresses"),
        row[Int]("total_addresses"),
        row[Int]("total_closures"),
        row[Int]("total_addresses_with_balance"),
        row[Int]("total_closures_with_balance"),
        row[Int]("total_addresses_no_dust"),
        row[Int]("total_closures_no_dust"),
        Math.round(1000000000*row[Double]("gini_address"))/1000000000.0,
        Math.round(1000000000*row[Double]("gini_closure"))/1000000000.0
      )}).head
    }
  }

  
  def getDistribution(value: Double, blockHeight: Int) = {

      DB.withConnection { implicit connection =>
        val satoshis = 100000000*value;
        (SQL(
          "select count(1) as a, sum(balance)/100000000 as b, (select total_bitcoins_in_addresses from stats where block_height = " +blockHeight  + ")   as c from addresses where balance > " + satoshis
        )() map {row => Distribution(
          Math.round(10000*row[Option[Long]]("b").getOrElse(0L)/row[Long]("c"))/100,
          row[Long]("a"), 
          Math.round(row[Option[Long]]("b").getOrElse(0L))
        )}).head
      }  

  }
}
