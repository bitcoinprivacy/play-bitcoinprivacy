package controllers

import play.api.mvc._
import play.api.data.{Form}
import play.api.data.validation._
import play.api.data.Forms.{single, nonEmptyText, of}
import play.api.data.format.Formats._
import models.{Wallet,RichList,Stats}
import org.bitcoinj.core.{Address,Transaction}
import org.bitcoinj.params.MainNetParams


object Application extends Controller {


  val addressForm = Form(
    single("address" -> nonEmptyText(minLength=0, maxLength=64).verifying(chainConstraint))
  )

  val valueForm = Form(
    single("value" -> of[Double])
  )
 
  def chainConstraint: Constraint[String] = {
    Constraint("constraint.addressCheck"){
      string => 
      {  if (string.toIntOpt.isDefined || isAddress(string) || isTx(string))
        Valid
        else
          Invalid(Seq(ValidationError("Not found pattern")))
      }
    }
  }

  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
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

  def index = Action.async {
    for ( height <- Wallet.getBlockHeight )
    yield Ok(views.html.index(height, addressForm, "", None))
  }

  def faq = Action {
    Ok(views.html.faq())
  }
 
  def explorer = Action.async {
    for {
      height <- Wallet.getBlockHeight
      blockList <- Wallet.getBlocks(25,height)

    }
    yield
      Ok(views.html.explorer(height, blockList,addressForm))
  }

  def richList = Action.async {
    for {
      blockHeight <- Wallet.getBlockHeight
      addressList <- RichList.getRichList(blockHeight, "richest_addresses")
      walletList <- RichList.getRichList(blockHeight, "richest_closures")
    }
    yield
       Ok(views.html.richlist(blockHeight, addressList zip walletList))
  }

  def walletPost = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      {
        errors =>
        for (blockHeight <- Wallet.getBlockHeight)
          yield BadRequest(views.html.index(blockHeight,errors,"",None))
      },
      {
        case (string: String) => {
         for (blockHeight <- Wallet.getBlockHeight)
          yield
            if (isBlock(string))
              Redirect(routes.Application.block(string))
            else if (isAddress(string))
              Redirect(routes.Application.wallet(string))
            else if (isTx(string))
              Redirect(routes.Application.transaction(string))
            else
              Redirect(routes.Application.index)
        }
      }
    )
  }


  def wallet(address: String) = Action.async {
    for {
      blockHeight <- Wallet.getBlockHeight
      walletList <- Wallet.getWallet(address)
    }
    yield
      Ok(views.html.index(blockHeight, addressForm, address, Some(walletList)))
  }

  def stats = Action.async {
    for {
      blockHeight <- Wallet.getBlockHeight
      statsList <- Stats.getStats
    }
    yield
      Ok(views.html.stats(blockHeight, statsList))
  }

  def block(blockHeight: String) = Action.async {
    for {
       txList <- Wallet.getTransactions(blockHeight)
    }
    yield
      Ok(views.html.block(blockHeight, txList, addressForm))
  }

  def transaction(txHash: String) = Action.async {
    for {txoList <- Wallet.getMovements(txHash)}
    yield
      Ok(views.html.transaction(txHash, txoList, addressForm))
  }

  def address(address: String) = Action.async {
    val statsFuture = Wallet.getAddressMovements(address)


    for {statsList <- statsFuture}
    yield
      Ok(views.html.address(address, statsList, addressForm))
  }

  def distribution(arg:String = "1") = Action.async {
    for {
      blockHeight <- Wallet.getBlockHeight
      ginis <- Stats.getGinis
      value = arg.toDoubleOpt.getOrElse(1.0)
      (totalBitcoins, totalAdresses, percent) <- Stats.getDistribution(value, blockHeight)
    }
    yield 
      Ok(views.html.distribution(blockHeight, ginis, totalBitcoins, percent, totalAdresses, value, valueForm))
  }

  def distributionPost = Action.async { implicit request =>
    valueForm.bindFromRequest.fold({
      errors =>  {
        for {
          blockHeight <- Wallet.getBlockHeight
          ginis <- Stats.getGinis
          (totalBitcoins, totalAddresses, percent) <- Stats.getDistribution(1, blockHeight)
        }
        yield BadRequest(views.html.distribution(blockHeight, ginis, totalBitcoins, percent, totalAddresses, 1, errors))}},
      {
        case (value: Double) =>
          for {
          blockHeight <- Wallet.getBlockHeight
          ginis <- Stats.getGinis
          (totalBitcoins, totalAddresses, percent) <- Stats.getDistribution(value, blockHeight)
          }
          yield Ok(views.html.distribution(blockHeight, ginis, totalBitcoins, percent, totalAddresses, value, valueForm))
        }
    )
  }
}
