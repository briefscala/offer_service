package com.briefml

import akka.http.scaladsl.model.DateTime
import com.briefml.models.OfferRepository.OfferTup
import com.briefml.models.OfferStatus.{Expired, Inactive}
import com.briefml.models.{Offer, OfferRepository, OfferStatus, TimeRange}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class OfferApi(offerRepository: OfferRepository) {
  import OfferApi._

  private def maybeMax[T](list: Seq[T])(implicit ev: Ordering[T]): Seq[T] =
    if (list.nonEmpty) Seq(list.max) else Seq.empty

  private implicit def convert[T](opt: Option[T]) = opt.toIterable

  private def deserialize(iter: Iterable[OfferTup]): Iterable[Offer[Int]] = {
    iter.map { case (id, (start, dur), statusDB) =>
      val span = TimeRange(DateTime(start), dur)
      val status =
        if (Offer.isInactive(span)) Inactive
        else if (Offer.isExpired(span)) Expired
        else OfferStatus.withNameInsensitive(statusDB)
      fromTup(id, (start, dur), status)
    }
  }

  def offerById(offerId: Int): Future[Iterable[Offer[Int]]] =
    OfferRepository.byId(offerId).map(deserialize(_))

  def offerByStatus(status: OfferStatus): Future[Iterable[Offer[Int]]] =
    OfferRepository.byStatus(status).map(deserialize)

  def deleteOffer(id: Int): Future[Unit] = OfferRepository.delete(id)

  def cancelOffer(id: Int): Future[Unit] = OfferRepository.cancel(id)

  def upsertOffer(offer: Offer[Int]): Future[Unit] = OfferRepository.upsert(offer)

  /**
    * this method is not thread safe and it is written as an a simple example
    * @param offer
    * @return
    */
  def insertOffer(offer: Offer[Unit]) = {
    val futMaybeMax = OfferRepository.offerList.map(
      _.map {case (key,_,_) => key})
      .map(maybeMax(_))
    futMaybeMax.collect {
      case Nil => OfferRepository.upsert(offer.copy(id = 0))
      case max :: Nil => OfferRepository.upsert(offer.copy(id = max + 1))
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
