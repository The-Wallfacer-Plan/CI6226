package controllers

import play.api.mvc.{Action, Controller}

object ClientApp extends Controller {
  def index = Action {
    Ok(views.html.search("test"))
  }
}
