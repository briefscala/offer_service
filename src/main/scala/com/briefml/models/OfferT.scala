package com.briefml.models

import java.time.Clock

trait OfferT[ID] {
  def id: ID
  def span: TimeRange
  def status: OfferStatus
  def clock: Clock
}
