package controllers

import play.api.mvc.{Action, Controller}

class ClientApp extends Controller {
  def home = Action {
    Ok(views.html.home("testIT"))
  }

  def index = Action {
    Redirect(routes.ClientApp.home())
  }
}