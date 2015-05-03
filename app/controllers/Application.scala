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
    Ok(views.html.index(Wallet.getBlockHeight, addressForm, "", None))
  }

  def faq = Action {
    Ok(views.html.faq())
  }
 
  def explorer = Action.async {
    val height = Wallet.getBlockHeight
    val blocksFuture = Wallet.getBlocks(25, height)
    
    for {blocksList <- blocksFuture }
    yield
      Ok(views.html.explorer(height, blocksList))
  }

  def richList = Action.async {
    val blockHeight = Wallet.getBlockHeight
    val addressFuture = RichList.getRichList(blockHeight, "richest_addresses")
    val walletsFuture = RichList.getRichList(blockHeight, "richest_closures")
    
    for {addressList <- addressFuture
      walletList <- walletsFuture
    }
    yield{ 
      Ok(views.html.richlist(blockHeight, addressList, walletList))
    }
  }

  def searchAddress = Action.async { implicit request =>
    addressForm.bindFromRequest.fold({
      errors => scala.concurrent.Future{BadRequest(views.html.index(Wallet.getBlockHeight,errors,"",None))}},
      {
        case (address) => 
          
          Wallet.get(address) map {a =>
            Ok(views.html.index(Wallet.getBlockHeight, addressForm, address, Some(a)))
          }
      }
    )
  }

  def wallet(address: String) = Action.async {
    val statsFuture = Wallet.get(address)


    for {statsList <- statsFuture}
    yield
      Ok(views.html.index(Wallet.getBlockHeight, addressForm, address, Some(statsList)))
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
    val blockH = Wallet.getBlockHeight
    val giniFuture = Stats.getGinis
    val tupleFuture = Stats.getDistribution(1, blockH)
    for { ginis <- giniFuture
      (totalBitcoins, totalAdresses, percent) <- tupleFuture }
    yield 
      Ok(views.html.distribution(blockH, ginis, totalBitcoins, percent, totalAdresses, 1, valueForm))
  }

  def distribution = Action.async { implicit request =>
    valueForm.bindFromRequest.fold({
      errors =>  {
        val blockH = Wallet.getBlockHeight
        val tupleF = Stats.getDistribution(1, blockH)
        val ginisF = Stats.getGinis
        for {ginis <- ginisF; (totalBitcoins, totalAddresses, percent) <- tupleF}
          yield BadRequest(views.html.distribution(blockH, ginis, totalBitcoins, percent, totalAddresses, 1, errors))}},
      {
        case (value: Double) =>
          val blockH = Wallet.getBlockHeight
          val tupleF = Stats.getDistribution(value, blockH)
          val ginisF = Stats.getGinis
          for {ginis <- ginisF; (totalBitcoins, totalAddresses, percent) <- tupleF}
            yield Ok(views.html.distribution(blockH, ginis, totalBitcoins, percent, totalAddresses, value, valueForm))
      }
    )
  }
}
