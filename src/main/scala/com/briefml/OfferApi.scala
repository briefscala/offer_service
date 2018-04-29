package com.briefml

import akka.http.scaladsl.model.DateTime
import com.briefml.models._
import com.briefml.models.OfferStatus.{Expired, Inactive}
import com.briefml.models.{Offer, OfferRepository, OfferStatus, TimeRange}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class OfferApi(offerRepository: OfferRepository) {
  import OfferApi._

  private def maybeMax[T](list: Iterator[T])(implicit ev: Ordering[T]): Seq[T] =
    if (list.nonEmpty) Seq(list.max) else Seq.empty

  private implicit def convert[T](opt: Option[T]) = opt.toIterable

  private def deserialize(iter: Iterator[OfferTup]): Iterator[Offer[Int]] = {
    iter.map { case (id, (start, dur), statusDB) =>
      val span = TimeRange(DateTime(start), dur)
      val status =
        if (Offer.isInactive(span)) Inactive
        else if (Offer.isExpired(span)) Expired
        else OfferStatus.withNameInsensitive(statusDB)
      fromTup(id, (start, dur), status)
    }
  }

  def offerById(offerId: Int): Future[Option[Offer[Int]]] =
    offerRepository.byId(offerId).map(_.toIterator)
      .map(deserialize).map(_.toList.headOption)

  def offerByStatus(status: OfferStatus): Future[Iterator[Offer[Int]]] =
    offerRepository.byStatus(status).map(deserialize)

  def deleteOffer(id: Int): Future[Unit] = offerRepository.delete(id)

  def cancelOffer(id: Int): Future[Unit] = offerRepository.cancel(id)

  def upsertOffer(offer: Offer[Int]): Future[Unit] = offerRepository.upsert(offer)

  /**
    * this method is not thread safe and it is written as an a simplification example in the absence of a db
    * @param offer
    * @return
    */
  def insertOffer(offer: Offer[Unit]) = {
    val futMaybeMax = offerRepository.offerIter.map(
      _.map {case (key,_,_) => key})
      .map(maybeMax(_))
    futMaybeMax.collect {
      case Nil => offerRepository.upsert(offer.copy(id = 0))
      case max :: Nil => offerRepository.upsert(offer.copy(id = max + 1))
    }
  }
}

object OfferApi {
  def fromTup[ID](
    id: ID,
    span: (Long, Int),
    status: OfferStatus): Offer[ID] = {
    val (start, duration) = span
    Offer(id, TimeRange(DateTime(start), duration), status)
  }
}
