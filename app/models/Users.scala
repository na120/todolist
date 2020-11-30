package models

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

import play.api.db.slick.{DatabaseConfigProvider => DBConfigProvider}

/**
  * task テーブルへの Accessor
  */
@Singleton
class Users @Inject()(dbcp: DBConfigProvider)(implicit ec: ExecutionContext) extends Dao(dbcp) {

  import profile.api._
  import utility.Await

  val table = "user"

  /**
    * DB上に保存されている全てのタスクを取得する
    *
    * @return
    */
  def save(user: User): Int = Await.result(
    db.run(sqlu"INSERT INTO #$table (name, password) VALUES ('#${user.name}', '#${user.password}')")
  )

  def findNameAndPass(name: String, password: String): Option[User] = Await.result(
    db.run(sql"SELECT name, password FROM #$table WHERE name='#$name' AND password='#$password' ".as[User].headOption)
  )

  def findByName(name: String): Option[User] = Await.result(
    db.run(sql"SELECT name,password FROM #$table WHERE name='#$name' ".as[User].headOption)
  )
}
