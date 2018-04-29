package com.briefml.models

import com.briefml.OfferApi
import com.briefml.models.OfferStatus.Cancelled

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object OfferRepositoryInMemory extends OfferRepository {

  private var offers: OfferRow = Map.empty

  def findById(offerId: Int): Future[Option[OfferTup]] = Future.successful(
    offers.find { case (id,_) => id == offerId}
      .map { case (id,(span,status)) => (id, span, status)}
  )

  def offerIter: Future[Iterator[OfferTup]] = Future.successful(
    offers.toIterator.map { case (id, (span, status)) => (id, span, status)}
  )

  def upsert(offer: Offer[Int]): Future[Unit] = {
    val (offerId, offerData) = (
      offer.id, ((offer.span.start.clicks, offer.span.duration), offer.status.entryName.toLowerCase)
    )
    Future.successful(offers += (offerId -> offerData))
  }

  def cancel(id: Int): Future[Unit] = {
    findById(id).collect {
      case Some((i, span, _)) =>
        upsert(OfferApi.fromTup(i, span, Cancelled))
      case None => ()
    }
  }

  def delete(offerId: Int): Future[Unit] = {
    Future.successful(
      offers = offers.filterNot { case (id, _) => id == offerId}
    )
  }

  def byStatus(status: OfferStatus): Future[Iterator[OfferTup]] =
    offerIter.map(_.filter { case (_,span,st) => Offer.getStatus(span, st) == status })

  def byId(offerId: Int): Future[Option[OfferTup]] =
    offerIter.map(_.find { case (id,_,_) => id == offerId})
}
