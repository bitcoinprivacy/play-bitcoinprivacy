import org.bitcoinj.params.MainNetParams                                                                                                              
import play.api.cache.Cache
import scala.reflect.ClassTag
import scala.util.control.Exception._                                                                                                                       
import org.bitcoinj.core.{Address,AddressFormatException}                                                                                                              
import play.api.data.{Form}
import play.api.data.validation._
import play.api.data.Forms.{single, nonEmptyText, of}
import play.api.data.format.Formats._
import play.api.cache.Cache
import models._

package object controllers{
  implicit def current = play.api.Play.current

  implicit def global = scala.concurrent.ExecutionContext.Implicits.global  

  def getFromCache(name: String)(a: => play.api.mvc.EssentialAction) = {
    Cache.getOrElse(name, 24*60*60){a}
  }

  
  
  val addressForm = Form(
    single("address" -> nonEmptyText(minLength=0, maxLength=64).verifying(chainConstraint))
  )

  val valueForm = Form(
    single("value" -> of[Double])
  )
 
  def chainConstraint: Constraint[String] = {
    Constraint("constraint.addressCheck"){
      string => {  
        if (string.toIntOpt.isDefined || isAddress(string) || isTx(string))
          Valid
        else
          Invalid(Seq(ValidationError("Not found pattern")))
      }
    }
  }
  
 
  implicit class StringImprovements(val s: String) {
    def toIntOpt = catching(classOf[NumberFormatException]) opt s.toInt
    def toDoubleOpt = catching(classOf[NumberFormatException]) opt s.toDouble
  }

 def isTx(hash: String): Boolean = {
    return hex2bytes(hash).length == 32
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

  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }
}
