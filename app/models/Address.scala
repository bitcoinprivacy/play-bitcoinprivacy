package models

import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Address(hash:String,balance:Long) extends Model 
case class AddressesInfo(count: Int, balance: Long) extends Info
case class RichestInfo(inAddresses: Long, inWallets: Long, total: Long, count: Int)

object Address  
{ 
  def getRichList(blockHeight: Int, table: String) = {
    val query = "select hash, ifnull(balance,0) as balance from "+table+" where hash is not null and  block_height = "+blockHeight 
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => Address(hashToAddress(row[Option[Array[Byte]]]("hash").getOrElse(Array.empty)),(row[Long]("balance")/100000000).toLong)}).toList
    }
  }

  def getAddressesPage(hex:String, height: Int) = {
    Pagination(100,100)
/*    val query = "select floor(("+(pageSize-1)+" + count(*))/"+pageSize+") as c from (SELECT hash as hash, balance FROM addresses WHERE balance > 0 and representant = X'"+hex+"') m "
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => Pagination(row[Int]("c"), pageSize)}).head
    }*/
  }

  def getAddressesInfo(hex: String, height: Int) = {
    val query =
//      "select sum(value) as v, count(distinct(address)) as c from movements where address in (select hash from addresses where representant = X'"+hex+"') and spent_in_transaction_hash is null;"
    "select 1 as c, 1 as v"
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => AddressesInfo(row[Int]("c"),row[Long]("v"))}).head
    }
  }

  def getRichestInfo(height: Int) = {
    val query = 
      " select (select count(1) from richest_addresses where block_height = "+height+") as c, " +
      " (select sum(ifnull(balance,0)) from richest_addresses where block_height = "+height+")  as v1, " +
      " (select sum(ifnull(balance,0)) from richest_closures where block_height = "+height+")  as v2, " +
      " (select total_bitcoins_in_addresses from stats where block_height = " + height +") as t"
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => RichestInfo( row[Long]("v1"),row[Long]("v2"),row[Long]("t"),row[Int]("c"))}).head
    }
  }

  def getRepresentant(hex:String) = {
    val query =
      "SELECT hex(representant) as representant FROM addresses where hash=X'"+hex+"' union select '"+hex+"' as representant"
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => row[String]("representant")}).head
    }
  }

  def getAddresses(hex:String, height: Int,page: Int) = {
5
    val subquery = "select hex(hash) as hash from addresses where representant = X'"+hex+"' limit 10000470"
    val hashes = DB.withConnection { implicit connection =>
      (SQL(subquery)() map {row => "X'"+row[String]("hash")+"'"}).toList
    }
    val stringHashes = hashes.mkString(",")
    println(stringHashes)
    val query =
      "select sum(value) as balance, address from movements where address in ("+stringHashes+") and spent_in_transaction_hash is null group by address " 
    DB.withConnection { implicit connection =>
      (SQL(query)() map {row => Address(hashToAddress(row[Array[Byte]]("address")),row[Option[Long]]("balance").getOrElse(0L))}).toList
    }
  }
}
