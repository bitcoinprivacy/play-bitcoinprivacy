import org.bitcoinj.params.MainNetParams                                                                                                              
import play.api.cache.Cache
import scala.reflect.ClassTag
import scala.util.control.Exception._                                                                                                                       
import org.bitcoinj.core.{Address,AddressFormatException}                                                                                                              
import play.api.data.{Form}
import play.api.data.validation._
import play.api.data.Forms.{single, nonEmptyText, of}
import play.api.data.format.Formats._
import play.api.cache.{Cached => Cacheed}
import models._
import scala.concurrent.duration._
import scala.concurrent._

package object controllers{
  
  def cache_timeout = 1000*60*60*24 // a day in millis
  def cache_height_timeout = 60 // 1 minute

  implicit def current = play.api.Play.current

  implicit def global = scala.concurrent.ExecutionContext.Implicits.global  

  def Cached(name: String)(a: play.api.mvc.EssentialAction) = {    
    val label = Await.result(Blocks.getBlockHeight, Duration(50, "millis")) + "."+name
    println("retrieving view from cache " + label)
    Cacheed(label)(a)
  }

  val addressForm = Form(
    single("address" -> nonEmptyText(minLength=0, maxLength=64).verifying(chainConstraint))
  )

  val valueForm = Form(
    single("value" -> nonEmptyText().verifying(distributionConstraint))
  )
 
  def chainConstraint: Constraint[String] = {
    Constraint("constraint.addressCheck"){
      string => {  
        if (isBlock(string) || isAddress(string) || isTx(string))
          Valid
        else
          Invalid(Seq(ValidationError("Not found search pattern")))
      }
    }
  }
  
  def distributionConstraint: Constraint[String] = {
    Constraint("constraint.doubleCheck"){
      string => {  
        if (isPositiveDouble(string))
          Valid
        else
          Invalid(Seq(ValidationError("Not a valid positive number")))
      }
    }
  }
  
  implicit class StringImprovements(val s: String) {
    def toIntOpt = catching(classOf[NumberFormatException]) opt s.toInt
    def toDoubleOpt = catching(classOf[NumberFormatException]) opt s.toDouble
  }

 def isTx(hash: String): Boolean = {
   hash.replaceAll("[^0-9a-fA-F]", "") == hash &&
   hex2bytes(hash).length == 32
 }

  def isBlock(block: String): Boolean =
    block.toIntOpt.isDefined

  def isAddress(address: String): Boolean = {
    try{
      new Address(MainNetParams.get, address);
      true
    }
    catch { 
      case _: Throwable => false
    }
  }

  def isPositiveDouble(string: String): Boolean = 
    string.toDoubleOpt.isDefined && 
    string.toDouble > 0

  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }
}
