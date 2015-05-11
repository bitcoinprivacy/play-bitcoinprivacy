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
import scala.concurrent.duration._
import scala.concurrent._
import scala.reflect.ClassTag

object Application extends Controller {

  def faq = load("faq"){
    Action {
        Ok(views.html.faq(addressForm))
    }    
  }

  // cache for the models data
  def load[A: ClassTag](label: String, info: => A) = {
    Future { 
      Cache.getAs[A](label).getOrElse{
        Cache.set(label, info, timeout)
        println("caching info")
        info
      }  
    }
  }

  // cache for the views direct from http request
  def load(label: String)(view: => play.api.mvc.EssentialAction) = {
    Cache.getAs[play.api.mvc.EssentialAction](label).getOrElse{
      Cache.set(label, view, timeout)
        println("caching view")
        view
    }
  }

  def explorer = load("explorer"){
    Action.async {
      for {
        height <- load("height", Block.getBlockHeight)
        blockList <- load("blocks.10", Block.getBlocks(10,height))

      }
      yield{
        Ok(views.html.explorer(height, blockList,addressForm))
      }
    }
  }

  def richList = load("richList"){
    Action.async {
      for {
        blockHeight <- load("height", Block.getBlockHeight)
        addressList <- load("richlist.addresses",Address.getRichList(blockHeight, "richest_addresses"))
        walletList <- load("richlist.closures",Address.getRichList(blockHeight, "richest_closures"))
      }
      yield{
        Ok(views.html.richlist(blockHeight, addressList zip walletList, addressForm))
      }
    }
  }

  def search = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      {
        errors =>
        for {blockHeight <- load("height", Block.getBlockHeight)
            blocks <- load("blocks."+10, Block.getBlocks(10, blockHeight))}
          yield BadRequest(views.html.explorer(blockHeight, blocks, errors))
      },
      {
        case (string: String) => 
          for (blockHeight <- load("height", Block.getBlockHeight))
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
      load("wallet."+page+"."++address) {
        Action.async {
          for {
            blockHeight <- load("height", Block.getBlockHeight)
            representant <- load("addresses.representant."+address, Address.getRepresentant(hexAddress(address)))
            walletList <- load("addresses."+page+"."+representant, Address.getAddresses(representant,page))
            walletInfo <- load("addresses.info."+representant, Address.getAddressesInfo(representant))
            walletPage <- load("addresses.page."+representant, Address.getAddressesPage(representant, page))
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

  def wrong(message: String) = Action.async(Future(Ok(views.html.wrong_search(message, addressForm))))
  
  def stats = load("stats"){
    Action.async {
      for {
        blockHeight <- load("height", Block.getBlockHeight)
        statsList <- load("stats.values",Stat.getStats)
      }
      yield
        Ok(views.html.stats(blockHeight, statsList, addressForm))
    }
  }
  
  def block(height: String, page: Int) = load("block."+page+"."+height){ 
    if (isBlock(height)) {
      Action.async {
        for {
          txList <- load("transactions."+page+"."+height,Transaction.getTransactions(height.toInt, page))
          txInfo <- load("transactions.info."+height,Transaction.getTransactionInfo(height.toInt))
          txPage <- load("transactions.page."+height,Transaction.getTransactionPage(height.toInt, page))
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

  def transaction(txHash: String, page: Int) = load("transaction."+page+"."+txHash){
    if (isTx(txHash)){
      Action.async {
        for {
          txoList <- load("movements."+txHash,Movement.getMovements(txHash, page))
          txoInfo <- load("movements.info."+txHash,Movement.getMovementsInfo(txHash))
          txoPage <- load("movements.page."+txHash,Movement.getMovementsPage(txHash, page))
        }
        yield{
          Ok(views.html.transaction(txHash, txoList, txoInfo, txoPage,  addressForm, page))
        }
      }
    } else{
      wrong("Bad request: " + txHash + " is not a valid hash")
    }
  }

  def address(address: String, page: Int) = load("address."+page+"."+address){
    if (isAddress(address)){
      Action.async {
        for {
          txList <- load("outputs."+address+"."+page, Output.getOutputs(hexAddress(address),page))
          hex = hexAddress(address)
          txInfo <- load("outputs.info."+address,Output.getOutputsInfo(hex))
          txPage <- load("outputs.page."+address,Output.getOutputsPage(hex,page))
        }
        yield{
          Ok(views.html.address(address, txList, txInfo, txPage, addressForm, page))
        }
      }
    } else{
      wrong("Bad request: " + address + " is not a valid address")
    }
  }

  def distribution(value: String = "1.0") = load("distribution."+value){
    if (isPositiveDouble(value)){
      Action.async {
        for {
          blockHeight <- load("height", Block.getBlockHeight)
          ginis <- load("ginis", Stat.getGinis)
          distribution <- load("stats."+value,Stat.getDistribution(value.toDouble, blockHeight))
        }
        yield{
          Ok(views.html.distribution(blockHeight, ginis, distribution, value.toDouble, valueForm, addressForm))
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
          blockHeight <- load("height", Block.getBlockHeight)
          ginis <- load("ginis", Stat.getGinis)
          distribution <- load("stats.1.0", Stat.getDistribution(1.0, blockHeight))
        }
        yield BadRequest(views.html.distribution(blockHeight, ginis, distribution, 1.0, errors, addressForm))}
      },
      {
        case (value: String) =>
          for {
            blockHeight <- load("height", Block.getBlockHeight)
          }
          yield
            Redirect(routes.Application.distribution(value))
      }
    )
  }
}
