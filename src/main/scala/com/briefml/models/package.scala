package com.briefml

package object models {
  /**
    * a representation of a serialised row
    */
  type OfferData = ((Long, Int), String)
  type OfferRow = Map[Int, OfferData]
  type OfferTup = (Int, (Long, Int), String)
}
