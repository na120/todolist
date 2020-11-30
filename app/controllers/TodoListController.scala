package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents, Result}
import models.{Task, Tasks}
import models.{User, Users}

/**
  * TodoListコントローラ
  */
@Singleton
class TodoListController @Inject()(tasks: Tasks)(users: Users)(cc: ControllerComponents)
    extends AbstractController(cc) {

  /**
    *
    * @return
    */
  /**
    * インデックスページを表示
    */
  def index = Action { implicit request =>
    // 200 OK ステータスで app/views/index.scala.html をレンダリングする
    Ok(views.html.index("Welcome to Play application!"))
  }

  def todolist = Action { implicit request =>
    val entries = tasks.list

    Ok(views.html.todolist(entries))
  }

  def notDoneList = Action { implicit request =>
    val entries = tasks.list

    Ok(views.html.notDoneList(entries))
  }

  def entry(id: Int) = Action {
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.entry(e))
      case None    => NotFound(s"No entry for id=${id}")
    }

  }

  def startRegistration = Action { request =>
    Ok(views.html.titleForm(request)).withNewSession
  }

  def registerTitle = Action { request =>
    (for {
      param <- request.body.asFormUrlEncoded
      title <- param.get("title").flatMap(_.headOption)
    } yield {
      Ok(views.html.descriptionForm(request)).withSession(request.session + ("task::title" -> title))

    }).getOrElse[Result](Redirect("/todolist/register"))
  }

  def registerDescription = Action { request =>
    (for {
      title       <- request.session.get("task::title")
      param       <- request.body.asFormUrlEncoded
      description <- param.get("description").flatMap(_.headOption)
    } yield {
      val task = Task(title, description, false)
      Ok(views.html.confirm(task)(request)).withSession(request.session + ("task::description" -> description))
    }).getOrElse[Result](Redirect("/todolist/register"))

  }

  def confirm = Action { request =>
    (for {
      title       <- request.session.get("task::title")
      description <- request.session.get("task::description")
    } yield {
      tasks.save(Task(title, description, false))
      Redirect("/todolist").withNewSession
    }).getOrElse[Result](Redirect("/todolist/register"))
  }

  def editTitle(id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.editTitle(e)(request)).withNewSession
      case None    => NotFound(s"No entry for id=${id}")
    }
  }

  def confirmTitle(id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) =>
        for {
          param    <- request.body.asFormUrlEncoded
          newTitle <- param.get("title").flatMap(_.headOption)
        } tasks.save(Task(id, newTitle, e.description, e.isDone, e.createdAt));
        Redirect("/todolist").withNewSession

      case None => NotFound(s"No entry for id=${id}")
    }
  }

  def editDescription(id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.editDescription(e)(request)).withNewSession
      case None    => NotFound(s"No entry for id=${id}")
    }
  }

  def confirmDescription(id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) =>
        for {
          param          <- request.body.asFormUrlEncoded
          newDescription <- param.get("description").flatMap(_.headOption)
        } tasks.save(Task(id, e.title, newDescription, e.isDone, e.createdAt));
        Redirect("/todolist").withNewSession

      case None => NotFound(s"No entry for id=${id}")
    }
  }

  def editDone(id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) => Ok(views.html.editDone(e)(request)).withNewSession
      case None    => NotFound(s"No entry for id=${id}")
    }
  }

  def confirmDone(id: Int) = Action { request =>
    tasks.findByID(id) match {
      case Some(e) =>
        for {
          param   <- request.body.asFormUrlEncoded
          newDone <- param.get("isDone").flatMap(_.headOption)
        } tasks.save(Task(id, e.title, e.description, newDone.toBoolean, e.createdAt));
        Redirect("/todolist").withNewSession

      case None => NotFound(s"No entry for id=${id}")
    }
  }

  def delete(id: Int) = Action {
    tasks.delete(id)
    Redirect("/todolist").withNewSession
  }

  def login = Action { implicit request =>
    Ok(views.html.login("ログイン"))
  }

  def userRegister = Action { implicit request =>
    Ok(views.html.userRegister("新規ユーザー登録"))
  }

  def newUserCheck = Action { implicit request =>
    (for {
      param   <- request.body.asFormUrlEncoded
      newName <- param.get("name").flatMap(_.headOption)
      newPass <- param.get("password").flatMap(_.headOption)

    } yield {
      users.findByName(newName) match {
        case Some(e) =>
          Ok(views.html.userRegister("入力したユーザー名はすでに使用されています"))
        case None =>
          val hash = utility.Digest(newPass)
          users.save(User(newName, hash));
          Ok(views.html.login("ログイン"))
      }
    }).getOrElse[Result](Redirect("/login"))
  }

  def userCheck = Action { implicit request =>
    (for {
      param    <- request.body.asFormUrlEncoded
      name     <- param.get("name").flatMap(_.headOption)
      password <- param.get("password").flatMap(_.headOption)
    } yield {
      val hash = utility.Digest(password)
      users.findNameAndPass(name, hash) match {
        case Some(e) =>
          val entries = tasks.list

          Ok(views.html.todolist(entries)).withNewSession
        case None =>
          Ok(views.html.login("ユーザー名かパスワードが間違っています"))
      }
    }).getOrElse[Result](Redirect("/login"))
  }

}
