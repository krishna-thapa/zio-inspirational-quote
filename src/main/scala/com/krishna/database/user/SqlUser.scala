package com.krishna.database.user

import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment

import com.krishna.model.user.{ LoginForm, RegisterUser, UserInfo }

object SqlUser:

  lazy val validateUser: (String, LoginForm) => doobie.ConnectionIO[Option[UserInfo]] =
    (tableName, login) =>
      (fr"SELECT * FROM" ++ Fragment.const(
        tableName
      ) ++ fr"WHERE email=${login.email} AND password=${login.password}")
        .query[UserInfo]
        .option

  lazy val insertUser: (String, UserInfo) => doobie.Update0 = (tableName, user) =>
    (fr"INSERT INTO" ++ Fragment.const(tableName) ++
      fr"VALUES (${user.userId}, ${user.firstName}, ${user.lastName}, ${user.email}, ${user.password}, ${user.createdDate}, ${user.isAdmin})").update

  lazy val updateUser: (String, RegisterUser) => doobie.Update0 = (tableName, user) =>
    (fr"UPDATE" ++ Fragment.const(
      tableName
    ) ++ fr"SET first_name=${user.firstName}, last_name=${user.lastName}, password=${user.password} WHERE email=${user.email}").update

  lazy val getAllUsers: String => doobie.Query0[UserInfo] = tableName =>
    (fr"SELECT * FROM" ++ Fragment.const(tableName))
      .query[UserInfo]

  lazy val getUser: (String, String) => doobie.ConnectionIO[UserInfo] = (tableName, email) =>
    (fr"SELECT * FROM" ++ Fragment.const(tableName) ++ fr"WHERE email=$email")
      .query[UserInfo]
      .unique

  lazy val adminRole: (String, String) => doobie.Update0 = (tableName, email) =>
    (fr"UPDATE INTO" ++ Fragment.const(tableName) ++ fr"VALUES ()").update

  lazy val delete: (String, String) => doobie.Update0 = (tableName, email) =>
    (fr"DELETE * FROM" ++ Fragment.const(tableName) ++ fr"WHERE email=$email").update
