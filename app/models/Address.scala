package models

import anorm._
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Address(address:String,balance:Long) extends Model 
case class AddressesInfo(count: Int, balance: Long) extends Info
case class RichestInfo(inAddresses: Long, inWallets: Long, total: Long, count: Int)

object Address  
{
  implicit val addressReads = Json.reads[Address]

  def getRichList(blockHeight: Int, table: String): Future[List[Address]] = {
    val from = if (table == "richest_addresses") "addresses" else "wallets"
    WS.url("http://bitcoinprivacy.net:8080/richlist/"+from+"/1/1000").get().map {response => (response.json).as[List[Address]]}
  }

  def getAddressesPage(hex:String, height: Int) = Future{Pagination(100,100)}

  def getAddressesInfo(hex: String, height: Int) = Future{AddressesInfo(1,19)}

  def getRichestInfo(height: Int) = Future{RichestInfo(1,2,3,4)}

  def getAddresses(hex:String, height: Int,page: Int) =WS.url("http://bitcoinprivacy.net:8080/wallet/"+hex+"/"+pageSize*(page-1)+ "/"+pageSize*page).get().map {response => (response.json).as[List[Address]]}
  
}
