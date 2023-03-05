package com.krishna.model

import java.util.UUID

case class FavQuote(
  id: Int,
  userId: UUID,
  csvId: String,
  favTag: Boolean
)
