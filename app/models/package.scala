import scala.concurrent._
import scala.util.control.Exception._
import org.bitcoinj.core.{Address => Add}
import org.bitcoinj.params._
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

  lazy val conf = ConfigFactory.load()
  lazy val network = conf.getString("network") 

  lazy val params = network match {
    case "main" =>
      MainNetParams.get
    case "regtest" =>
      RegTestParams.get
    case "testnet" =>
      TestNet3Params.get
    case _ =>
      throw new Exception(s"Unknow params for network $network")
  }

  def hashToAddress(hash: Array[Byte]): String = try {hash.length match {
   case 20 => new Add(params,0,hash).toString
   case 21 => new Add(params,hash.head.toInt,hash.tail).toString
   case 0 => "No decodable address found"
   case x if (x%20==1) => 
     (for (i <- 1 to hash.length-20 by 20)
     yield hashToAddress(hash.slice(i,i+20)) ).mkString(",")
   case _  => hash.length + " undefined"
  }} catch{case _:Exception => "Bitcoinj failed decoding address"}

  val config = ConfigFactory.load()

  def getFromApi(params:String*) = {
    val url = config.getString("api.url")+params.mkString("/")
    println("XXXXXX"+url)
    WS.url(url).get()
  }
}


