
package controllers

import play.api.mvc._
import play.api.data.{Form}
import play.api.data.validation._
import play.api.data.Forms.{single, nonEmptyText, of}
import play.api.data.format.Formats._
import models.{Wallet,RichList, Stats}
import org.bitcoinj.core.{Address,AddressFormatException}
import org.bitcoinj.params.MainNetParams


object Application extends Controller {

  val addressForm = Form(
    single("address" -> nonEmptyText(minLength=26, maxLength=35).verifying(addressConstraint))
  )
 
  val valueForm = Form(
    single("value" -> of[Double])
  )
 
  def addressConstraint: Constraint[String] = {
    Constraint("constraint.addressCheck"){
      address =>
      try { 
        new Address(MainNetParams.get, address)
        Valid 
      }
      catch {
        case e: AddressFormatException => Invalid(Seq(ValidationError("Invalid Address")))
        case e => Invalid(Seq(ValidationError(e.toString)))
      }
    }
  }

  def index = Action {
    Ok(views.html.index(Wallet.getBlockHeight, addressForm, None))
  }

  def faq = Action {
    Ok(views.html.faq())
  }
 
  def explorer = Action.async {
    val blocksFuture = Wallet.getBlocks(100)
    
    for {blocksList <- blocksFuture }
    yield
      Ok(views.html.explorer(Wallet.getBlockHeight, blocksList))
  }


  def richList = Action.async {
    val addressFuture = RichList.getRichestAddresses
    val walletsFuture = RichList.getRichestWallets

    for {addressList <- addressFuture
      walletList <- walletsFuture }
    yield 
      Ok(views.html.richlist(Wallet.getBlockHeight, addressList, walletList))
  }

  def searchAddress = Action.async { implicit request =>
    addressForm.bindFromRequest.fold({
      errors => scala.concurrent.Future{BadRequest(views.html.index(Wallet.getBlockHeight,errors,None))}},
      {
        case (address) => 
          Wallet.get(address) map {a =>
            Ok(views.html.index(Wallet.getBlockHeight, addressForm, Some(a)))
          }
      }
    )
  }

  def stats = Action.async {
    val statsFuture = Stats.getStats
    

    for {statsList <- statsFuture}
    yield 
      Ok(views.html.stats(Wallet.getBlockHeight, statsList))
  }

  def block(height: Int) = Action.async {
    val statsFuture = Wallet.getTransactions(height)


    for {statsList <- statsFuture}
    yield
      Ok(views.html.block(height, statsList))
  }

  def transaction(txHash: String) = Action.async {
    val statsFuture = Wallet.getMovements(txHash)


    for {statsList <- statsFuture}
    yield
      Ok(views.html.transaction(txHash, statsList))
  }

  def address(address: String) = Action.async {
    val statsFuture = Wallet.getAddressMovements(address)


    for {statsList <- statsFuture}
    yield
      Ok(views.html.address(address, statsList))
  }

  def distributionIndex = Action.async {
    val giniFuture = Stats.getGinis
    val tupleFuture = Stats.getDistribution(0)
    for { ginis <- giniFuture
      (totalBitcoins, totalAdresses, percent) <- tupleFuture }
    yield 
      Ok(views.html.distribution(Wallet.getBlockHeight, ginis, totalBitcoins, percent, totalAdresses, 0, valueForm))
  }

  def distribution = Action.async { implicit request =>
    val giniFuture = Stats.getGinis
    val tupleFuture = Stats.getDistribution(0)
    for { ginis <- giniFuture
      (totalBitcoins, totalAdresses, percent) <- tupleFuture }
    yield 
      valueForm.bindFromRequest.fold({
        errors =>  {BadRequest(views.html.distribution(Wallet.getBlockHeight, ginis, totalBitcoins, percent, totalAdresses, 0, errors))}},
        {
            case (value: Double) =>
               Ok(views.html.distribution(Wallet.getBlockHeight, ginis, totalBitcoins, percent, totalAdresses, value, valueForm))
        }
    )
  }
}
