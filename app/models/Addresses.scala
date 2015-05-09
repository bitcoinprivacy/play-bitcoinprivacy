package models

import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Address(hash:String,balance:Long) extends Model 
case class AddressesInfo(count: Int, balance: Long) extends Info

object Addresses  
{ 
  def getRichList(blockHeight: Int, table: String) = Future {

      DB.withConnection { implicit connection =>
        (SQL(
          """
          select hash, ifnull(balance,0) as balance from """+table+""" where 
           hash is not null and  block_height = (select max(block_height) from """+table+""");
          """
        )() map {row => Address(hashToAddress(row[Option[Array[Byte]]]("hash").getOrElse(Array.empty)), row[Long]("balance"))}).toList}
  }

  def getAddressesPage(address:String, page: Int) = Future{
        try{
      val hex = hexAddress(address)

        DB.withConnection { implicit connection =>
          (SQL("""
          select floor(("""+(pageSize-1)+""" + count(*))/"""+pageSize+""") as c from (
            SELECT
              hash as hash, balance
            FROM addresses 
            WHERE balance > 0 and representant = 
            (SELECT representant FROM addresses where hash=X'"""+hex+"""')
               UNION
            SELECT address as hash, sum(value) as balance 
              FROM movements where 
               address is not null and
              spent_in_transaction_hash is null and address=X'"""+hex+"""' 
              UNION
           SELECT address as hash , 0 from movements where address=X'"""+hex+"""' 
          )   
          where hash is not null;
          
         """)() map {row => Pagination(
           page,
           row[Int]("c"),
           pageSize
         )}).head
      }
    } catch{
      case _: Throwable => 
        Pagination(0,0,0)
    }
  }

  def getAddressesInfo(address: String): Future[AddressesInfo] = cached("address.info."+address){ 
    Future{
      try{
        val hex = hexAddress(address)

        DB.withConnection { implicit connection =>
          (SQL("""
          select count(distinct(hash)) as c, sum(balance) as v from (
            SELECT
              hash as hash, balance
            FROM addresses 
            WHERE balance > 0 and representant = 
            (SELECT representant FROM addresses where hash=X'"""+hex+"""')
               UNION
            SELECT address as hash, sum(value) as balance 
              FROM movements where 
               address is not null and
              spent_in_transaction_hash is null and address=X'"""+hex+"""' 
              UNION
           SELECT address as hash , 0 from movements where address=X'"""+hex+"""' 
          )   
          where hash is not null;
          
         """)() map {row => AddressesInfo(
           row[Int]("c"),
           row[Long]("v")
         )}).head
        }
      } catch{
        case _: Throwable =>
          AddressesInfo(0,0)
      }
     }
  }

  def getAddresses(address:String,page: Int) = Future {
    
    try{
      val hex = hexAddress(address)

        DB.withConnection { implicit connection =>
          (SQL("""
          select hash, balance as balance from (
            SELECT
              hash as hash, balance
            FROM addresses 
            WHERE balance > 0 and representant = 
            (SELECT representant FROM addresses where hash=X'"""+hex+"""')
               UNION
            SELECT address as hash, sum(value) as balance 
              FROM movements where 
               address is not null and
              spent_in_transaction_hash is null and address=X'"""+hex+"""' 
              UNION
           SELECT address as hash , 0 from movements where address=X'"""+hex+"""' 
          )   
          where hash is not null """+
          " group by hash limit "+(page-1)+"*"+pageSize+","+pageSize
          )() map {row => Address(
           hashToAddress(row[Array[Byte]]("hash")), 
           row[Option[Long]]("balance").getOrElse(0L)
         )}).toList

      }
    } catch{
      case _: Throwable => List.empty
    }
  }
}
