package models

import play.api.libs.json._

import anorm._
import anorm.SqlParser._
import anorm.Column.{ columnToArray}
import scala.concurrent.Future
import org.bitcoinj.core.Address
import org.bitcoinj.params.MainNetParams

object Stats{
  def getStats = Future {
    DB.withConnection { implicit connection =>
      (SQL(
        """
          select * from stats order by block_height desc limit 1,1;
        """
      )() map {row => List(
        ("Block height in database", row[Int]("block_height")),
        ("Total Bitcoins in known addresses", row[Int]("total_bitcoins_in_addresses")),
        ("Number of known addresses", row[Int]("total_addresses")),
        ("Number of address clusters (closures)", row[Int]("total_closures")),
        ("Number of known addresses with a positive balance", row[Int]("total_addresses_with_balance")),
        ("Number of closures with a positive balance", row[Int]("total_closures_with_balance")),
        ("Number of known non-dust addresses", row[Int]("total_addresses_no_dust")),
        ("Number of known non-dust closures" , row[Int]("total_closures_no_dust"))
      )}).head
      
    }
}

def getGinis = Future {
    DB.withConnection { implicit connection =>
      (SQL(
       """                                                                                                                                                                                                
          select * from stats order by block_height desc limit 1,1;                                                                                                                                         
        """
      )() map {row => List(
        ("Gini addresses", row[Double]("gini_address")),
        ("Gini wallets" , row[Double]("gini_closure"))
      )}).head

    }
}

def getDistribution(value: Double, blockHeight: Int) = Future { 
  DB.withConnection { implicit connection =>
      val satoshis = 100000000*value;
      (SQL(
        "  select count(1) as a, sum(balance)/100000000 as b, (select total_bitcoins_in_addresses from stats order by block_height desc limit 1,1)  as c from addresses where balance > " + satoshis
      )() map {row => (
        Math.round(row[Long]("b")), row[Long]("a"), Math.round(10000*row[Long]("b")/row[Long]("c"))/100
      )}).head
   
  
 
  }
}

}

