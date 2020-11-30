package models

/**
  * Domain model of task
  * @param name          ユーザー名
  * @param password      パスワード
  */
case class User(name: String, password: String)

object User extends DomainModel[User] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[User] = GetResult(r => User(r.nextString, r.nextString))

}
