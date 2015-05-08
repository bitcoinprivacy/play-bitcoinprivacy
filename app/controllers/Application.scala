package controllers

import play.api.mvc._
import play.api.data.{Form}
import play.api.data.validation._
import play.api.data.Forms.{single, nonEmptyText, of}
import play.api.data.format.Formats._
import org.bitcoinj.core.{Address=>Ad,Transaction => Tx}
import org.bitcoinj.params.MainNetParams
import models._
import play.api.libs.concurrent.Execution.Implicits._

object Application extends Controller {
  
  def faq = Action {
    Ok(views.html.faq())
  }
 
  def explorer = getFromCache("explorer"){
    Action.async {
      for {
        height <- Blocks.getBlockHeight
        blockList <- Blocks.getBlocks(25,height)

      }
      yield{
        Ok(views.html.explorer(height, blockList,addressForm))
      }
    }
  }

  def richList = getFromCache("richList"){
    Action.async {
      for {
        blockHeight <- Blocks.getBlockHeight
        addressList <- Addresses.getRichList(blockHeight, "richest_addresses")
        walletList <- Addresses.getRichList(blockHeight, "richest_closures")
        addressInfo <- Addresses.getAddressesInfo(addressList)
        walletInfo <- Addresses.getAddressesInfo(walletList)
      }
      yield{
        Ok(views.html.richlist(blockHeight, addressList zip walletList, addressInfo, walletInfo))
      }
    }
  }

  def search = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      {
        errors =>
        for {blockHeight <- Blocks.getBlockHeight
            blocks <- Blocks.getBlocks(25, blockHeight)}
          yield BadRequest(views.html.explorer(blockHeight, blocks, errors))
      },
      {
        case (string: String) => {
         for (blockHeight <- Blocks.getBlockHeight)
          yield
            if (isBlock(string))
              Redirect(routes.Application.block(string))
            else if (isAddress(string))
              Redirect(routes.Application.wallet(string))
            else if (isTx(string))
              Redirect(routes.Application.transaction(string))
            else
              Redirect(routes.Application.explorer)
        }
      }
    )
  }

  def wallet(address: String) = getFromCache("wallet."+address){
    Action.async {
      for {
        blockHeight <- Blocks.getBlockHeight
        walletList <- Addresses.getWallet(address)
        walletInfo <- Addresses.getAddressesInfo(walletList)
      }
      yield{
        println("generamos views")
        Ok(views.html.wallet(blockHeight, address,addressForm, walletInfo, Some(walletList)))
      }
    }
  }
  
  def stats = getFromCache("stats"){
    Action.async {
      for {
        blockHeight <- Blocks.getBlockHeight
        statsList <- Stats.getStats
      }
      yield
        Ok(views.html.stats(blockHeight, statsList))
    }
  }
  
  def block(blockHeight: String) = getFromCache("block."+blockHeight){
    Action.async {
      for {
        txList <- Transactions.getTransactions(blockHeight)
        txInfo <- Transactions.getTransactionInfo(txList)
      }
      yield{
        Ok(views.html.block(blockHeight, txList, txInfo, addressForm))
      }
    }
  }

  def transaction(txHash: String) = getFromCache("transaction."+txHash){
    Action.async {
      for {
        txoList <- Transactions.getMovements(txHash)
        txoInfo <- Transactions.getMovementsInfo(txoList)
      }
      yield{
        Ok(views.html.transaction(txHash, txoList, txoInfo, addressForm))
      }
    }
  }

  def address(address: String) = getFromCache("address."+address){
    Action.async {
      for {
        txList <- Transactions.getOutputs(address)
        adInfo <- Transactions.getOutputsInfo(txList)
      }
      yield{
        Ok(views.html.address(address, txList, adInfo, addressForm))
      }
    }
  }

  def distribution(arg:String = "1") = getFromCache("distribution."+arg){
    val value = arg.toDoubleOpt.getOrElse(1.0)
    Action.async {
      for {
        blockHeight <- Blocks.getBlockHeight
        ginis <- Stats.getGinis
        distribution <- Stats.getDistribution(value, blockHeight)
      }
      yield{
        Ok(views.html.distribution(blockHeight, ginis, distribution, value, valueForm))
      }
    }
  }

  def distributionPost = Action.async { implicit request =>
    valueForm.bindFromRequest.fold({
      errors =>  {
        for {
          blockHeight <- Blocks.getBlockHeight
          ginis <- Stats.getGinis
          distribution <- Stats.getDistribution(1, blockHeight)
        }
        yield BadRequest(views.html.distribution(blockHeight, ginis, distribution, 1, errors))}
      },
      {
        case (value: Double) =>
          for {
            blockHeight <- Blocks.getBlockHeight
          }
          yield
            Redirect(routes.Application.distribution(value.toString))
      }
    )
  }
}
