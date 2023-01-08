package com.krishna.database.user

import com.krishna.model.user.UserInfo
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.fragment.Fragment

object SqlUser:

  lazy val insertUser: (String, UserInfo) => doobie.Update0 = (tableName, user) =>
    (fr"INSERT INTO" ++ Fragment.const(tableName) ++ fr"VALUES ()").update
