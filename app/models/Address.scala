package models

import play.api.libs.json._

import anorm._
import anorm.SqlParser._
import scala.concurrent.Future

case class Address(
  address      : String
) 
