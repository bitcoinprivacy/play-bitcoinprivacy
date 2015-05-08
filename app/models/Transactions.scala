package models

import play.api.libs.json._
import anorm._
import scala.concurrent.Future
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.core.AddressFormatException

case class Transaction(hash:String,value: Long) 
case class Movement(address:String,mode: String, hash: String, value: Long) 
case class Output(hash :String, spent: String ,value: Long) 
case class MovementsInfo(inputs: Long, outputs: Long, in: Long, out: Long)
case class TransactionInfo(value: Long, tx: Int)
case class OutputsInfo(out: Long, in: Long, outputs: Int, inputs: Int)

object Transactions{
  def getTransactions(blockHeight: String) = Future {
    val height = stringToInt(blockHeight)

      DB.withConnection { implicit connection =>
        (SQL(
          "SELECT  sum(value) as balance, hex(transaction_hash) as address FROM movements WHERE block_height = "+height+" GROUP BY transaction_hash"
        )() map {row => Transaction(
          row[String]("address"),
          row[Long]("balance"))
        }).toList
      }

  }

  def getOutputsInfo(list: List[Output]) = Future{
    OutputsInfo(
      list.map{ case out => if (out.spent =="") 0 else out.value}.sum,
      list.map{ case out => out.value}sum,
      list.length,
      list.map{ case out => if (out.spent=="") 1 else 0}.sum
    )
  }


  def getMovementsInfo(list: List[Movement]) = Future {
    MovementsInfo(
      list.map{ case tx => if (tx.mode=="in") 1 else 0}.sum,
      list.map{ case tx => if (tx.mode=="out") 1 else 0}.sum,
      list.map{ case tx => if (tx.mode=="in") tx.value else 0}.sum,
      list.map{ case tx => if (tx.mode=="out") tx.value else 0}.sum
    )
  }

  def getTransactionInfo(list: List[Transaction ]) = Future {

    TransactionInfo(
      list.map{ case transaction => transaction.value }.sum,
      list.length

    )
  }

  def getOutputs(strAddress: String) = Future  {
    try{

        val hex = hexAddress(strAddress)
        DB.withConnection { implicit connection =>
          (SQL(
            "SELECT ifnull(value,0) as balance, hex(transaction_hash) as tx, hex(spent_in_transaction_hash) " + 
              " as spent_in_tx FROM movements WHERE address = X'"+hex+"'"
          )() map {row => Output(
            row[String]("tx"), 
            row[String]("spent_in_tx"), 
            row[Long]("balance")
          )}).toList
      
        }
    }catch{
      case e: Throwable =>
        List.empty
    }
  }

  def getMovements(txHash: String) = Future {
    val hash = txHash.replaceAll("[^0-9a-fA-F]", "")
    if (hash == txHash){
     
        DB.withConnection { implicit connection =>
          (SQL(
            "SELECT  value as balance, address as address, hex(spent_in_transaction_hash) as tx, 'out' as mode " +
              " FROM  movements " +
              " WHERE  transaction_hash = X'"+txHash+"'" +
              " UNION ALL " +
              " SELECT n.value as balance, n.address as address, hex(n.transaction_hash) as tx, 'in' as mode " +
              " FROM movements n left outer join movements m "+
              " WHERE m.transaction_hash = n.spent_in_transaction_hash and n.spent_in_transaction_hash = X'"+txHash+"'" +
              " GROUP BY n.address "
          )() map {row => Movement(
            hashToAddress(row[Array[Byte]]("address")), 
            row[String]("mode"),
            row[String]("tx"), 
            row[Long]("balance"))
          }).toList
        }
     
    }
    else{
      List.empty
    }
  }
}
