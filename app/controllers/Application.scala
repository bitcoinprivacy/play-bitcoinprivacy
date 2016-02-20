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
      //(SQL"insert into access (ip,path) values (${request.remoteAddress}, $path)").executeUpdate()
    }} flatMap (_ => action(request))

    lazy val parser = action.parser
  }
 
  def log(path:String)(action: Action[AnyContent]) = {}
    //new Logging(path, action)
  
 
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
      height <- Block.getBlockHeight
    }
    yield
      Ok(views.html.index(height, addressForm))
  }



  def explorer(page: Int) = load("explorer."+page){
    for {
      height <- Block.getBlockHeight
      blockList <- Block.getBlocks(height,page)
      blocksInfo <- Block.getBlocksInfo(height)
      blocksPage <- Block.getBlocksPage(height)
    }
    yield{
      Ok(views.html.explorer(height, blockList,blocksPage, blocksInfo, addressForm, page))
    }
  }
  

  def richList = load("richList"){
    for {
      blockHeight <- Block.getBlockHeight
      addressList <- Address.getRichList(blockHeight, "addresses")
      walletList <- Address.getRichList(blockHeight, "wallets")
      addressInfo <- AddressesSummary.getRichest(blockHeight, "addresses")
      walletsInfo <- AddressesSummary.getRichest(blockHeight, "wallets")
    }
    yield{
      Ok(views.html.richlist(blockHeight, addressList,  walletList, addressInfo, walletsInfo, addressForm))
    }
  }


  def search = Action.async { implicit request =>
    addressForm.bindFromRequest.fold(
      {
        errors =>
        for (blockHeight <- Block.getBlockHeight)
        yield BadRequest(views.html.wrong_search("not found", errors))
          
      },
      {
        case (string: String) =>
          for (blockHeight <- Block.getBlockHeight)
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
        for (blockHeight <- Block.getBlockHeight)
        yield BadRequest(views.html.index(blockHeight, errors))
      },
      {
        case (string: String) => 
          for (blockHeight <- Block.getBlockHeight)
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
          blockHeight <- Block.getBlockHeight
          walletList <- Address.getWallet(address,blockHeight,pageSize*(page-1), pageSize*page)
          walletInfo <- AddressesSummary.get(address, blockHeight)
        }
        yield{
          Ok(views.html.wallet(blockHeight, address,addressForm, walletInfo, Some(walletList), page))
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
        blockHeight <- Block.getBlockHeight
        txList <- Transaction.getTransactions(height.toInt, blockHeight, page)
        txInfo <- Transaction.getTransactionInfo(height.toInt)
        txPage <- Transaction.getTransactionPage(height.toInt)
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
        height <- Block.getBlockHeight
        inputs <- Movement.getInputs(txHash, height, (page-1)*pageSize, pageSize*page)
        outputs<- Movement.getOutputs(txHash, height, (page-1)*pageSize, pageSize*page)
        inputsInfo <- MovementsSummary.getInputsInfo(txHash, height)
        outputsInfo <- MovementsSummary.getOutputsInfo(txHash, height)
        utxos <- UTXO.getFromTx(txHash, height, (page-1)*pageSize, pageSize*page)
        uInfo <- UTXOsSummary.getFromTx(txHash, height)
      }
      yield{
        Ok(views.html.transaction(txHash, inputs, outputs, utxos, inputsInfo, outputsInfo,  uInfo,  addressForm, page))
      }
    } else{
      wrong("Bad request: " + txHash + " is not a valid hash")
    }


  def address(address: String, page: Int) = 
    if (isAddress(address)) load("address."+page+"."+address){
      for {
        height <- Block.getBlockHeight
        utxos <- UTXO.getFromAddress(address,height, pageSize*(page-1), pageSize*page)
        movements <- Movement.getFromAddress(address, height, pageSize*(page-1), pageSize*page)
        movementsInfo <- MovementsSummary.getInfoFromAddress(address, height)
        txInfo <- UTXOsSummary.getFromAddress(address, height)       
      }
      yield{
        Ok(views.html.address(address, utxos, movements, txInfo, movementsInfo, addressForm, page))
      }
    }
    else{
      wrong("Bad request: " + address + " is not a valid address")
    }
  

  def stats(value: String) = 
    if (isPositiveDouble(value))
      load("distribution."+value){
        for {
          height <- Block.getBlockHeight
          stats <- Stat.getStats(height)
          distribution <- Stat.getDistribution(value.toDouble, height)
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
          height <- Block.getBlockHeight
          stats <- Stat.getServerStats(height)
        }
        yield{
          Ok(views.html.server(height, stats, addressForm))
        }
   }
  

  def distributionPost = Action.async { implicit request =>
    valueForm.bindFromRequest.fold({
      errors =>  {
        for {
          blockHeight <- Block.getBlockHeight
          stats <- Stat.getStats(blockHeight)
          distribution <- Stat.getDistribution(1000.0, blockHeight)
        }
        yield BadRequest(views.html.stats(blockHeight, stats, distribution, 1000.0, errors, addressForm))}
      },
      {
        case (value: String) =>
          for {
            blockHeight <- Block.getBlockHeight
          }
          yield
            Redirect(routes.Application.stats(value))
      }
    )
  }
}
