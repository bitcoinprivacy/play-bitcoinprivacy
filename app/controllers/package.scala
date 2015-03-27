package object controllers {

  def DB = play.api.db.DB

  implicit def current = play.api.Play.current

  implicit def global = scala.concurrent.ExecutionContext.Implicits.global

  
}
