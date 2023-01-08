package com.krishna.database.user

import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment

import com.krishna.model.user.UserInfo

object SqlUser:

  lazy val insertUser: (String, UserInfo) => doobie.Update0 = (tableName, user) =>
    (fr"INSERT INTO" ++ Fragment.const(tableName) ++ fr"VALUES ()").update
