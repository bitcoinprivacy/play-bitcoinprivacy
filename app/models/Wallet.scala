package models

import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

abstract class AddressData
case class Address(hash:String,balance:Long) extends AddressData
case class AddressesInfo(count: Int, balance: Long) extends AddressData

object Address  
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

  def getAddressesInfo(list: List[Address]) = Future{
    
      AddressesInfo(
        list.length, 
        list.map{a => a.balance}.sum
      )
    
  }

  def getWallet(address:String) = Future {
    
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
          where hash is not null
          group by hash;
         """)() map {row => Address(
           hashToAddress(row[Array[Byte]]("hash")), 
           row[Option[Long]]("balance").getOrElse(0L)
         )}).toList

      }
    } catch{
      case _:AddressFormatException => List.empty
    }
  }
}
