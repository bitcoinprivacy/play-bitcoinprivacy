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

//case class Pagination(current: Int, total: Int)

object Application extends Controller {
  // cache block height and let a external process ( bge ) change it
  // model calls should use limit and a paginator....

  def faq = getFromCache("faq"){
    Action {
        Ok(views.html.faq())
    }    
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
      }
      yield{
        Ok(views.html.richlist(blockHeight, addressList zip walletList))
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
              Redirect(routes.Application.block(string, 1))
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

  def wallet(address: String, page: Int) = getFromCache("wallet."+page+"."+address){
    Action.async {
      for {
        blockHeight <- Blocks.getBlockHeight
        walletList <- Addresses.getAddresses(address,page)
        walletInfo <- Addresses.getAddressesInfo(address)
        walletPage <- Addresses.getAddressesPage(address, page)
      }
      yield{
        Ok(views.html.wallet(blockHeight, address,addressForm, walletInfo,walletPage, Some(walletList)))
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
  
  def block(blockHeight: String, page: Int) = getFromCache("block."+page+"."+blockHeight){
    val height = stringToInt(blockHeight)
    Action.async {
      for {
        txList <- Transactions.getTransactions(height, page)
        txInfo <- Transactions.getTransactionInfo(height)
        txPage <- Transactions.getTransactionPage(height, page)
      }
      yield{
        Ok(views.html.block(height, txList, txPage, txInfo, addressForm))
      }
    }
  }

  def transaction(txHash: String, page: Int) = getFromCache("transaction."+page+"."+txHash){
    Action.async {
      for {
        txoList <- Movements.getMovements(txHash, page)
        txoInfo <- Movements.getMovementsInfo(txHash)
        txoPage <- Movements.getMovementsPage(txHash, page)
      }
      yield{
        Ok(views.html.transaction(txHash, txoList, txoInfo, txoPage,  addressForm))
      }
    }
  }

  def address(address: String, page: Int) = getFromCache("address."+page+"."+address){
    Action.async {
      for {
        txList <- Outputs.getOutputs(address,page)
        txInfo <- Outputs.getOutputsInfo(address)
        txPage <- Outputs.getOutputsPage(address,page)
      }
      yield{
        Ok(views.html.address(address, txList, txInfo, txPage, addressForm))
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
