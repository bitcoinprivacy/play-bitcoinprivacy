import org.bitcoinj.params.MainNetParams                                                                                                              
import scala.reflect.ClassTag
import scala.util.control.Exception._                                                                                                                       
import org.bitcoinj.core.{Address => Add}
import org.bitcoinj.core.AddressFormatException                                                                                                              
import anorm._
import anorm.SqlParser._

package object models {

  type Date = java.util.Date

  def DB = play.api.db.DB

  def Logger = play.api.Logger

  type DateTime = org.joda.time.DateTime

  import java.util.TimeZone

  import java.text.SimpleDateFormat

  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

  def now = new DateTime(utc(new Date))

  def utc(date:Date) = {
    val tz = TimeZone.getDefault()
    var ret = new Date( date.getTime() - tz.getRawOffset() )

    // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
    if ( tz.inDaylightTime( ret )){
      val dstDate = new Date( ret.getTime() - tz.getDSTSavings() )

      // check to make sure we have not crossed back into standard time
      // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
      if ( tz.inDaylightTime( dstDate )){
        ret = dstDate
      }
    }

    ret
  }

  def asDateTime(date:Date) = 
    new DateTime(date)

  def asDateTime(date:Option[Date]) = 
    if(date.isDefined)
      Some(new DateTime(date.get))
    else
      None

  def asDate(date:DateTime) = 
    date.toDate

  def asDate(date:Option[DateTime]) = 
    if(date.isDefined)
      Some(date.get.toDate)
    else
      None
 

  implicit def current = play.api.Play.current

  implicit def global = scala.concurrent.ExecutionContext.Implicits.global

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

  
  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  def valueOf(buf: Array[Byte]): String = buf.map("%02X" format _).mkString
  
  def stringToInt(s: String): Int = try{s.toInt} catch { case _ : Throwable => -1}
  
  def hexAddress(stringAddress: String): String = {
    val arrayAddress = stringAddress.split(",")
    if (arrayAddress.length == 1) {
      val address = new Add(MainNetParams.get, stringAddress)
      (if(address.isP2SHAddress) "05" else "00")+valueOf(address.getHash160)
    }
    else{
      "0" + arrayAddress.length + 
        (for (i <- 0 until arrayAddress.length) 
        yield  valueOf(new Add(MainNetParams.get, arrayAddress(i)).getHash160) ).mkString("")
    }
  }

  def isTx(hash: String): Boolean = {
    return hex2bytes(hash).length == 32
  }

  def isBlock(block: String): Boolean =
    block.toIntOpt.isDefined

  def isAddress(address: String): Boolean = {
    try{
      new Add(MainNetParams.get, address);
      true
    }
    catch { 
      case _: Throwable => false
    }
  }
}
