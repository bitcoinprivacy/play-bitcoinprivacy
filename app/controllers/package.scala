import org.bitcoinj.core.{Address => BitcoinJAddress}
import org.bitcoinj.params.MainNetParams
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, of, single}
import play.api.data.format.Formats._
import play.api.data.validation._
import scala.concurrent._
import scala.reflect.ClassTag
import scala.util.control.Exception._

package object controllers {
  
  implicit def current = play.api.Play.current

  implicit def global = scala.concurrent.ExecutionContext.Implicits.global  

  def hexAddress(stringAddress: String): String = {
    val arrayAddress = stringAddress.split(",")
    if (arrayAddress.length == 1) {
      val address = new BitcoinJAddress(MainNetParams.get, stringAddress)
      (if(address.isP2SHAddress) "05" else "00")+valueOf(address.getHash160)
    }
    else{
      "0" + arrayAddress.length + 
        (for (i <- 0 until arrayAddress.length) 
        yield  valueOf(new BitcoinJAddress(MainNetParams.get, arrayAddress(i)).getHash160) ).mkString("")
    }
  }
  
  val addressForm = Form(
    single("address" -> nonEmptyText(minLength=0, maxLength=64).transform(_.trim, (x:String)=> x)
    ).verifying(chainConstraint))

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

  def valueOf(buf: Array[Byte]): String = buf.map("%02X" format _).mkString
  
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
      new BitcoinJAddress(MainNetParams.get, address);
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
