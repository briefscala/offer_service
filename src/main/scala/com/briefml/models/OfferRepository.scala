package com.briefml.models

import com.briefml.OfferApi
import com.briefml.models.OfferStatus.Cancelled

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import OfferRepository._

trait OfferRepository {
  def findById(offerId: Int): Future[Option[OfferTup]]
  def offerList: Future[Seq[OfferTup]]
  def upsert(offer: Offer[Int]): Future[Unit]
  def cancel(id: Int): Future[Unit]
  def delete(offerId: Int): Future[Unit]
  def byStatus(status: OfferStatus): Future[Seq[OfferTup]]
  def byId(offerId: Int): Future[Option[OfferTup]]
}

object OfferRepository extends OfferRepository {
  /**
    * a representation of a serialised row
    */
  type OfferData = ((Long, Int), String)
  type OfferRow = Map[Int, OfferData]
  type OfferTup = (Int, (Long, Int), String)

  private var offers: OfferRow = Map.empty

  def findById(offerId: Int): Future[Option[OfferTup]] = Future.successful(
    offers.find { case (id,_) => id == offerId}
      .map { case (id,(span,status)) => (id, span, status)}
  )

  def offerList: Future[Seq[OfferTup]] = Future.successful(
    offers.map { case (id, (span, status)) => (id, span, status)}.toSeq
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

  def byStatus(status: OfferStatus): Future[Seq[OfferTup]] =
    offerList.map(_.filter { case (_,span,st) => Offer.getStatus(span, st) == status })

  def byId(offerId: Int): Future[Option[OfferTup]] =
    offerList.map(_.find { case (id,_,_) => id == offerId})
}
