package com.krishna.auth.user

import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment

import com.krishna.auth.model.UserInfo

object SqlUser:

  lazy val insertUser: (String, UserInfo) => doobie.Update0 = (tableName, userInfo) =>
    (fr"INSERT INTO" ++ Fragment.const(tableName) ++ fr"VALUES ()").update
