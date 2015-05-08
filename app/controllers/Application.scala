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
 
  def explorer = Action.async {
    for {
      height <- Block.getBlockHeight
      blockList <- Block.getBlocks(25,height)

    }
    yield
      Ok(views.html.explorer(height, blockList,addressForm))
  }

  def richList = Action.async {
    for {
      blockHeight <- Block.getBlockHeight
      addressList <- Address.getRichList(blockHeight, "richest_addresses")
      walletList <- Address.getRichList(blockHeight, "richest_closures")
      addressInfo <- Address.getAddressesInfo(addressList)
      walletInfo <- Address.getAddressesInfo(walletList)
    }
    yield
       Ok(views.html.richlist(blockHeight, addressList zip walletList, addressInfo, walletInfo))
  }

  def search = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      {
        errors =>
        for {blockHeight <- Block.getBlockHeight
            blocks <- Block.getBlocks(25, blockHeight)}
          yield BadRequest(views.html.explorer(blockHeight, blocks, errors))
      },
      {
        case (string: String) => {
         for (blockHeight <- Block.getBlockHeight)
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

  def wallet(address: String) = Action.async {
    for {
      blockHeight <- Block.getBlockHeight
      walletList <- Address.getWallet(address)
      walletInfo <- Address.getAddressesInfo(walletList)
    }
    yield{
      Ok(views.html.wallet(blockHeight, address,addressForm, walletInfo, Some(walletList)))
    }
  }

  def stats = Action.async {
    for {
      blockHeight <- Block.getBlockHeight
      statsList <- Stat.getStats
    }
    yield
      Ok(views.html.stats(blockHeight, statsList))
  }

  def block(blockHeight: String) = Action.async {
      for {
        txList <- Transaction.getTransactions(blockHeight)
        txInfo <- Transaction.getTransactionInfo(txList)
      }
      yield{
        Ok(views.html.block(blockHeight, txList, txInfo, addressForm))    
      }    
  }

  def transaction(txHash: String) = Action.async {
    for {
      txoList <- Transaction.getMovements(txHash)
      txoInfo <- Transaction.getMovementsInfo(txoList)
    }
    yield{
      Ok(views.html.transaction(txHash, txoList, txoInfo, addressForm))
    }
  }

  def address(address: String) = Action.async {
    for {
      txList <- Transaction.getOutputs(address)
      adInfo <- Transaction.getOutputsInfo(txList)
    }
    yield{
      
      Ok(views.html.address(address, txList, adInfo, addressForm))
    }
  }

  def distribution(arg:String = "1") = Action.async {
    for {
      blockHeight <- Block.getBlockHeight
      ginis <- Stat.getGinis
      value = arg.toDoubleOpt.getOrElse(1.0)
      distribution <- Stat.getDistribution(value, blockHeight)
    }
    yield 
      Ok(views.html.distribution(blockHeight, ginis, distribution, value, valueForm))
  }

  def distributionPost = Action.async { implicit request =>
    valueForm.bindFromRequest.fold({
      errors =>  {
        for {
          blockHeight <- Block.getBlockHeight
          ginis <- Stat.getGinis
          distribution <- Stat.getDistribution(1, blockHeight)
        }
        yield BadRequest(views.html.distribution(blockHeight, ginis, distribution, 1, errors))}
      },
      {
        case (value: Double) =>
          for {
            blockHeight <- Block.getBlockHeight
            ginis <- Stat.getGinis
            distribution <- Stat.getDistribution(value, blockHeight)
          }
          yield Ok(views.html.distribution(blockHeight, ginis, distribution, value, valueForm))
      }
    )
  }
}
