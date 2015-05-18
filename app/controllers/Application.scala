package controllers

import models._
import org.bitcoinj.core.{Address => Ad, Transaction => Tx}
import play.api.cache._
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, of, single}
import play.api.data.format.Formats._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import scala.concurrent._
import scala.reflect.ClassTag
import anorm._
import play.api.db.DB


object Application extends Controller {

  def faq =  Action {
    Ok(views.html.faq(addressForm))
  }

  val timeout = play.Play.application().configuration().getInt("cache.duration")

  // cache for the models data

  def load[A: ClassTag](label: String, info: => A) = {
    Future {
      Cache.getAs[A](label).getOrElse{
        Cache.set(label, info, timeout)
        info
      }  
    }
  }

  // cache for the views direct from http request

  case class Logging[A](path:String, action: Action[A]) extends Action[A] {
    def apply(request:Request[A]): Future[Result] = 
      Future {DB.withConnection("bitcoinprivacy"){ implicit connection =>
      (SQL"insert into access (ip,path) values (${request.remoteAddress}, $path)").executeUpdate()
    }} flatMap (_ => action(request))

    lazy val parser = action.parser
  }
 
  def log(path:String)(action: Action[AnyContent]) = 
    new Logging(path, action)
  
 
  def load(path: String)(fR: => Future[Result]) = 
    new Logging(path, 
      Cache.getAs[Action[AnyContent]](path).getOrElse {
        val action = Action.async(fR)
        Cache.set(path, action, timeout)
        action
      }
    )
    
    
  def index = load("index"){
    for {
      height <- load("height", Block.getBlockHeight)
    }
    yield
      Ok(views.html.index(height, addressForm))
  }



  def explorer(page: Int) = load("explorer."+page){
    for {
      height <- load("height", Block.getBlockHeight)
      blockList <- load("blocks."+page, Block.getBlocks(height,page))
      blocksInfo <-load("blocks.info", Block.getBlocksInfo(height))
      blocksPage <- load("blocks.page", Block.getBlocksPage(height))
    }
    yield{
      Ok(views.html.explorer(height, blockList,blocksPage, blocksInfo, addressForm, page))
    }
  }
  

  def richList = load("richList"){
    for {
      blockHeight <- load("height", Block.getBlockHeight)
      addressList <- load("richlist.addresses",Address.getRichList(blockHeight, "richest_addresses"))
      walletList <- load("richlist.closures",Address.getRichList(blockHeight, "richest_closures"))
      listInfo <- load("richest.info", Address.getRichestInfo(blockHeight))
    }
    yield{
      Ok(views.html.richlist(blockHeight, addressList zip walletList, listInfo, addressForm))
    }
  }


  def search = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      {
        errors =>
        for (blockHeight <- load("height", Block.getBlockHeight))
        yield BadRequest(views.html.wrong_search("not found", errors))
          
      },
      {
        case (string: String) =>
          for (blockHeight <- load("height", Block.getBlockHeight))
          yield     
            if (isBlock(string))
              Redirect(routes.Application.block(string, 1))
            else if (isAddress(string))
              Redirect(routes.Application.address(string))
            else if (isTx(string))
              Redirect(routes.Application.transaction(string))
            else
              Redirect(routes.Application.index)                
      }      
    )
  }


  def searchWallet = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      {
        errors =>
        for (blockHeight <- load("height", Block.getBlockHeight))
        yield BadRequest(views.html.index(blockHeight, errors))
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
              Redirect(routes.Application.index)                
      }      
    )
  }

  def wallet(address: String, page: Int) =
  {
    if (isAddress(address)){
      load("wallet."+page+"."++address) {
        for {
          blockHeight <- load("height", Block.getBlockHeight)
          representant <- load("addresses.representant."+address, Address.getRepresentant(hexAddress(address)))
          walletList <- load("addresses."+page+"."+representant, Address.getAddresses(representant,blockHeight,page))
          walletInfo <- load("addresses.info."+representant, Address.getAddressesInfo(representant, blockHeight))
          walletPage <- load("addresses.page."+representant, Address.getAddressesPage(representant, blockHeight))
        }
        yield{
          Ok(views.html.wallet(blockHeight, address,addressForm, walletInfo,walletPage, Some(walletList), page))
        }
      }
    } else{
      wrong("Bad request: " + address + " is not a valid address")
    }
  }

  def wrong(message: String) = Action.async(Future(Ok(views.html.wrong_search(message, addressForm))))
  
  def block(height: String, page: Int) = 
    if (isBlock(height)) load("block."+page+"."+height){
      for {
        blockHeight <- load("height", Block.getBlockHeight)
        txList <- load("transactions."+page+"."+height,Transaction.getTransactions(height.toInt, blockHeight, page))
        txInfo <- load("transactions.info."+height,Transaction.getTransactionInfo(height.toInt))
        txPage <- load("transactions.page."+height,Transaction.getTransactionPage(height.toInt))
      }
      yield{
        Ok(views.html.block(height.toInt, txList, txPage, txInfo, addressForm,page))
      }
    }
    else{
      wrong("Invalid block number " + height)
    }

  def transaction(txHash: String, page: Int) = 
    if (isTx(txHash)) load("transaction."+page+"."+txHash) {
      for {
        height <- load("height", Block.getBlockHeight) 
        txoList <- load("movements."+txHash,Movement.getMovements(txHash, height, page))
        txoInfo <- load("movements.info."+txHash,Movement.getMovementsInfo(txHash, height))
        txoPage <- load("movements.page."+txHash,Movement.getMovementsPage(txHash, height))
      }
      yield{
        Ok(views.html.transaction(txHash, txoList, txoInfo, txoPage,  addressForm, page))
      }
    } else{
      wrong("Bad request: " + txHash + " is not a valid hash")
    }


  def address(address: String, page: Int) = 
    if (isAddress(address)) load("address."+page+"."+address){
      for {
        height <- load("height", Block.getBlockHeight) 
        txList <- load("outputs."+address+"."+page, Output.getOutputs(hexAddress(address), height ,page))
        hex = hexAddress(address)
        txInfo <- load("outputs.info."+address,Output.getOutputsInfo(hex, height))
        txPage <- load("outputs.page."+address,Output.getOutputsPage(hex, height))
      }
      yield{
        Ok(views.html.address(address, txList, txInfo, txPage, addressForm, page))
      }
    }
    else{
      wrong("Bad request: " + address + " is not a valid address")
    }
  

  def stats(value: String) = 
    if (isPositiveDouble(value))
      load("distribution."+value){
        for {
          height <- load("height", Block.getBlockHeight)
          stats <- load("stats", Stat.getStats(height))
          distribution <- load("stats."+value,Stat.getDistribution(value.toDouble, height))
        }
        yield{
          Ok(views.html.stats(height, stats, distribution, value.toDouble, valueForm, addressForm))
        }
      }
      else{
        wrong("Value " + value + " is not a positive double")
    }
  

def server = 
   Action.async{
        for {
          height <- load("height", Block.getBlockHeight)
          stats <- Future{Stat.getServerStats(height)}
        }
        yield{
          Ok(views.html.server(height, stats, addressForm))
        }
   }
  

  def distributionPost = Action.async { implicit request =>
    valueForm.bindFromRequest.fold({
      errors =>  {
        for {
          blockHeight <- load("height", Block.getBlockHeight)
          stats <- load("stats", Stat.getStats(blockHeight))
          distribution <- load("stats.1000.0", Stat.getDistribution(1000.0, blockHeight))
        }
        yield BadRequest(views.html.stats(blockHeight, stats, distribution, 1000.0, errors, addressForm))}
      },
      {
        case (value: String) =>
          for {
            blockHeight <- load("height", Block.getBlockHeight)
          }
          yield
            Redirect(routes.Application.stats(value))
      }
    )
  }
}
