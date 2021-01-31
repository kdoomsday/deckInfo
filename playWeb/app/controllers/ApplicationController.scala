package controllers

import play.api._
import play.api.mvc._

import javax.inject.Inject

class ApplicationController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def deckStats(deck: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }
}
