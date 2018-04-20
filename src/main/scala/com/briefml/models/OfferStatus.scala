package com.briefml.models

import enumeratum._

sealed trait OfferStatus extends EnumEntry

object OfferStatus extends Enum[OfferStatus] {

  val values = findValues

  case object Active extends OfferStatus
  case object Inactive extends OfferStatus
  case object Expired extends OfferStatus
  case object Cancelled extends OfferStatus
}
