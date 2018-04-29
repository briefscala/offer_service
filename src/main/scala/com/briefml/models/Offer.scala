package com.briefml.models

import java.time.Clock

import akka.http.scaladsl.model.DateTime
import com.briefml.models.OfferStatus.{Expired, Inactive}
import io.circe.{Decoder, Encoder}

sealed case class TimeRange(start: DateTime, duration: Int) {
  def endMillis: Long = start.clicks + duration * 3600000
}

case class Offer[ID](
  id: ID,
  span: TimeRange,
  status: OfferStatus = Inactive,
  clock: Clock = Clock.systemUTC()) extends OfferT[ID]

object Offer {

  def getStatus(rowSpan: (Long, Int), statusName: String, clock: Clock = Clock.systemUTC()): OfferStatus = {
    val span = TimeRange(DateTime(rowSpan._1), rowSpan._2)
    if (isInactive(span, clock)) Inactive
    else if (isExpired(span, clock)) Expired
    else OfferStatus.withNameInsensitive(statusName)
  }

  def now(clock: Clock = Clock.systemUTC()) = DateTime(clock.millis())

  def isInactive(span: TimeRange, clock: Clock = Clock.systemUTC()) = span.start > now(clock)

  def isExpired(span: TimeRange, clock: Clock = Clock.systemUTC()) = span.endMillis < now(clock).clicks

  implicit def offerEncoder: Encoder[Offer[Int]] =
    Encoder.forProduct4("id", "start_date", "duration", "status")(o =>
      (o.id, o.span.start.toIsoDateTimeString, o.span.duration, o.status.entryName.toLowerCase)
    )

  import cats.implicits._

  implicit def offerDecoder: Decoder[Offer[Unit]] = Decoder.instance ( cursor =>
    (
      cursor.get[String]("start_date"),
      cursor.get[Int]("duration")
    ).mapN { case (date, duration) =>
      Offer(
        (),
        TimeRange(
          DateTime.fromIsoDateTimeString(date).getOrElse(DateTime.now),
          duration
        ),
        Inactive
      )
    }
  )

  implicit def offerUpdateDecoder: Decoder[Offer[Int]] = Decoder.instance ( cursor =>
    (
      cursor.get[Int]("id"),
      cursor.get[String]("start_date"),
      cursor.get[Int]("duration"),
      cursor.get[Option[String]]("status")
    ).mapN { case (id, date, duration, status) =>
      val someStatus = status match {
        case Some(st) => OfferStatus.withNameInsensitive(st)
        case None => Inactive
      }
      Offer(
        id,
        TimeRange(
          DateTime.fromIsoDateTimeString(date).getOrElse(DateTime.now),
          duration
        ),
        someStatus
      )
    }
  )
}