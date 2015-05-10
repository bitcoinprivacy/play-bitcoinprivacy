package controllers

import play.api.mvc._
import play.api.data.{Form}
import play.api.data.validation._
import play.api.data.Forms.{single, nonEmptyText, of}
import play.api.data.format.Formats._
import org.bitcoinj.core.{AddressFormatException, Address=>Ad,Transaction => Tx}
import org.bitcoinj.params.MainNetParams
import models._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.cache._

object Application extends Controller {

  def faq = Cached("faq"){
    Action {
        Ok(views.html.faq())
    }    
  }
 
  def explorer = Cached("explorer"){
    Action.async {
      for {
        height <- Blocks.getBlockHeight
        blockList <- Blocks.getBlocks(10,height)

      }
      yield{
        Ok(views.html.explorer(height, blockList,addressForm))
      }
    }
  }

  def richList = Cached("richList"){
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
        case (string: String) => 
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
    )
  }

  def wallet(address: String, page: Int) =
  {
    if (isAddress(address)){
      Cached("wallet."+page+"."++address) {
        Action.async {
          for {
            blockHeight <- Blocks.getBlockHeight
            representant <- Addresses.getRepresentant(hexAddress(address))
            walletList <- Addresses.getAddresses(representant,page)
            walletInfo <- Addresses.getAddressesInfo(representant)
            walletPage <- Addresses.getAddressesPage(representant, page)
          }
          yield{
            Ok(views.html.wallet(blockHeight, address,addressForm, walletInfo,walletPage, Some(walletList), page))
          }
        }
      }
    } else{
      wrong("Bad request: " + address + " is not a valid address")
    }    
  }

  def wrong(message: String) = Action.async(Future(Ok(views.html.wrong_search(message))))
  
  def stats = Cached("stats"){
    Action.async {
      for {
        blockHeight <- Blocks.getBlockHeight
        statsList <- Stats.getStats
      }
      yield
        Ok(views.html.stats(blockHeight, statsList))
    }
  }
  
  def block(height: String, page: Int) = Cached("block."+page+"."+height){ 
    if (isBlock(height)) {
      Action.async {
        for {
          txList <- Transactions.getTransactions(height.toInt, page)
          txInfo <- Transactions.getTransactionInfo(height.toInt)
          txPage <- Transactions.getTransactionPage(height.toInt, page)
        }
        yield{
          Ok(views.html.block(height.toInt, txList, txPage, txInfo, addressForm,page))
        }
      }
    }
    else{
      wrong("Invalid block number " + height)
    }
  }

  def transaction(txHash: String, page: Int) = Cached("transaction."+page+"."+txHash){
    if (isTx(txHash)){
      Action.async {
        for {
          txoList <- Movements.getMovements(txHash, page)
          txoInfo <- Movements.getMovementsInfo(txHash)
          txoPage <- Movements.getMovementsPage(txHash, page)
        }
        yield{
          Ok(views.html.transaction(txHash, txoList, txoInfo, txoPage,  addressForm, page))
        }
      }
    } else{
      wrong("Bad request: " + txHash + " is not a valid hash")
    }
  }

  def address(address: String, page: Int) = Cached("address."+page+"."+address){
    if (isAddress(address)){
      Action.async {
        for {
          txList <- Outputs.getOutputs(hexAddress(address),page)
          hex = hexAddress(address)
          txInfo <- Outputs.getOutputsInfo(hex)
          txPage <- Outputs.getOutputsPage(hex,page)
        }
        yield{
          Ok(views.html.address(address, txList, txInfo, txPage, addressForm, page))
        }
      }
    } else{
      wrong("Bad request: " + address + " is not a valid address")
    }
  }

  def distribution(value: String = "1.0") = Cached("distribution."+value){
    if (isPositiveDouble(value)){
      Action.async {
        for {
          blockHeight <- Blocks.getBlockHeight
          ginis <- Stats.getGinis
          distribution <- Stats.getDistribution(value.toDouble, blockHeight)
        }
        yield{
          Ok(views.html.distribution(blockHeight, ginis, distribution, value.toDouble, valueForm))
        }
      }
    }else{
        wrong("Value " + value + " is not a positive double")
     
    }

  }

  def distributionPost = Action.async { implicit request =>
    valueForm.bindFromRequest.fold({
      errors =>  {
        for {
          blockHeight <- Blocks.getBlockHeight
          ginis <- Stats.getGinis
          distribution <- Stats.getDistribution(1.0, blockHeight)
        }
        yield BadRequest(views.html.distribution(blockHeight, ginis, distribution, 1.0, errors))}
      },
      {
        case (value: String) =>
          for {
            blockHeight <- Blocks.getBlockHeight
          }
          yield
            Redirect(routes.Application.distribution(value))
      }
    )
  }
}
