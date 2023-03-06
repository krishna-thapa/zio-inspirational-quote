package com.krishna.util

import java.text.SimpleDateFormat
import java.util.{ Calendar, Date }

object DateConversion:

  val now: Date                     = Calendar.getInstance().getTime
  val dateFormatter: Date => String = value => new SimpleDateFormat("yyyy-MM-dd").format(value)

  def getCurrentDate: String = dateFormatter(now)
