

import org.bitcoinj.params.MainNetParams                                                                                                              
import scala.reflect.ClassTag
import scala.util.control.Exception._                                                                                                                       
import org.bitcoinj.core.{Address => Add}
import org.bitcoinj.core.AddressFormatException                                                                                                              
import anorm._
import anorm.SqlParser._
import play.api.cache.{Cache, Cached => Cacheed}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent._

package object models {
  
  def DB = play.api.db.DB
  implicit def global = scala.concurrent.ExecutionContext.Implicits.global
  implicit def current = play.api.Play.current

  def pageSize = play.Play.application().configuration().getInt("page.size")

  implicit class StringImprovements(val s: String) {
    def toIntOpt = catching(classOf[NumberFormatException]) opt s.toInt
    def toDoubleOpt = catching(classOf[NumberFormatException]) opt s.toDouble
  }

  implicit def rowToByteArray: Column[Array[Byte]] = {
    Column.nonNull[Array[Byte]] { (value, meta) =>
      val MetaDataItem(qualified, nullable, clazz) = meta
      value match {
        case bytes: Array[Byte] => Right(bytes)
        case _ => Left(TypeDoesNotMatch("..."))
      }
    }
  }

 def hashToAddress(hash: Array[Byte]): String = {   
    if (hash.length==20){
      return new Add(MainNetParams.get,0,hash).toString
    }
    if (hash.length==21){
      return new Add(MainNetParams.get,hash.head.toInt,hash.tail).toString
    }
    if (hash.length%20==1){
      return (for (i <- 1 to hash.length-20 by 20) yield hashToAddress(hash.slice(i,i+20)) ).mkString(",")
    }
    else{
      return hash.length + " undefined"
    }
 }

}
