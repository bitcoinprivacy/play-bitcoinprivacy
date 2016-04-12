import scala.concurrent._
import scala.util.control.Exception._

import org.bitcoinj.core.{Address => Add}
import org.bitcoinj.params.MainNetParams
import play.api.libs.ws._

import com.typesafe.config.ConfigFactory

package object models {
  
  implicit def global = scala.concurrent.ExecutionContext.Implicits.global
  implicit def current = play.api.Play.current

  def pageSize = play.Play.application().configuration().getInt("page.size")

  implicit class StringImprovements(val s: String) {
    def toIntOpt = catching(classOf[NumberFormatException]) opt s.toInt
    def toDoubleOpt = catching(classOf[NumberFormatException]) opt s.toDouble
  }

  def hashToAddress(hash: Array[Byte]): String = hash.length match {
   case 20 => new Add(MainNetParams.get,0,hash).toString
   case 21 => new Add(MainNetParams.get,hash.head.toInt,hash.tail).toString
   case 0 => "No decodable address found"
   case x if (x%20==1) => 
     (for (i <- 1 to hash.length-20 by 20)
     yield hashToAddress(hash.slice(i,i+20)) ).mkString(",")
   case _  => hash.length + " undefined"
  }

  val config = ConfigFactory.load()

  def getFromApi(params:String*) = 
    WS.url(config.getString("api.url")+params.mkString("/")).get()
}


