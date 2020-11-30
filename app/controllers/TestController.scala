package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}

class TestController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  def hello = Action { implicit request =>
    Ok(views.html.hello(s"Hello,!"))
  }
  def hello2(id: String) = Action { implicit request =>
    Ok(views.html.hello(s"Hello,${id}!"))
  }
}
