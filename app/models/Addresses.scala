package models

import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Address(hash:String,balance:Long) extends Model 
case class AddressesInfo(count: Int, balance: Long) extends Info

object Addresses  
{ 
  def getRichList(blockHeight: Int, table: String) = Cached("richlist."+table+"."+blockHeight){
      val query = "select hash, ifnull(balance,0) as balance from "+table+" where hash is not null and  block_height = (select max(block_height) from "+table+");"
      println(query)
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Address(hashToAddress(row[Option[Array[Byte]]]("hash").getOrElse(Array.empty)), row[Long]("balance"))}).toList}

  }

  def getAddressesPage(hex:String, page: Int) = Cached("addresses.page."+hex){

      val query = "select floor(("+(pageSize-1)+" + count(*))/"+pageSize+") as c from (SELECT hash as hash, balance FROM addresses WHERE balance > 0 and representant = X'"+hex+"') m "
      println(query)
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Pagination(page, row[Int]("c"), pageSize)}).head
      }

  }

  def getAddressesInfo(hex: String): Future[AddressesInfo] = Cached("addresses.info."+hex){ 

      val query = "select count(*) as c, ifnull(sum(ifnull(m.balance,0)),0) as v from (SELECT hash as hash, balance FROM addresses WHERE balance > 0 and representant = X'"+hex+"') m  "
      println(query)
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => AddressesInfo(
          row[Int]("c"),
          row[Long]("v")
        )}).head
      }  

  }

  def getRepresentant(hex:String) = Cached("representant."+hex){

      val query ="SELECT hex(representant) as representant FROM addresses where hash=X'"+hex+"' union select '"+hex+"' as representant"
      println(query)
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row =>
          println(row[String]("representant")); row[String]("representant")
        }).head
      }
  }

  def getAddresses(hex:String,page: Int) = Cached("address."+page+"."+hex) {

      val query = "select m.hash as hash, m.balance as balance from (SELECT hash as hash, balance FROM addresses WHERE balance > 0 and representant = X'"+hex+"') m  limit "+(page-1)*pageSize+","+pageSize
      println(query)
      DB.withConnection { implicit connection =>
        (SQL(query)() map {row => Address(
          hashToAddress(row[Array[Byte]]("hash")),
          row[Option[Long]]("balance").getOrElse(0L)
        )}).toList
      }

  }
}
