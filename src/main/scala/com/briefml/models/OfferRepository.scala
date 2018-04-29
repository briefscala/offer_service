package com.briefml.models
import scala.concurrent.Future

trait OfferRepository {
  def findById(offerId: Int): Future[Option[OfferTup]]
  def offerIter: Future[Iterator[OfferTup]]
  def upsert(offer: Offer[Int]): Future[Unit]
  def cancel(id: Int): Future[Unit]
  def delete(offerId: Int): Future[Unit]
  def byStatus(status: OfferStatus): Future[Iterator[OfferTup]]
  def byId(offerId: Int): Future[Option[OfferTup]]
}
