package com.briefml

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{complete, entity, get, onComplete, path, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import com.briefml.models.{Offer, OfferStatus}
import io.circe._
import akka.http.scaladsl.model.MediaTypes.`application/json`

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import io.circe.parser._
import io.circe.syntax._

case class Routes(offerApi: OfferApi) {
  import Routes._

  private val version = "v1"

  val route: Route =
    pathPrefix("api" / version) {
      path("ping") {
        get {
          complete(s"pong $version")
        } ~
        post {
          complete(s"post pong $version")
        }
      } ~ path("offer_by_id" / IntNumber) { offerId =>
        val futureOffer: Future[Iterable[Offer[Int]]] = offerApi.offerById(offerId)
        onComplete(futureOffer) {
          case Success(iter) =>
            iter.headOption match {
              case Some(offer) => complete(offer.asJson.pretty(Printer.spaces2))
              case None => complete(StatusCodes.NotFound)
            }
          case Failure(error) =>
            complete(
              HttpResponse(
                status = StatusCodes.InternalServerError,
                entity = HttpEntity(error.getMessage)))
        }
      } ~ (path("offers") & put) {
        entity(as[Offer[Unit]]) { offer: Offer[Unit] =>
          val future = offerApi.insertOffer(offer)
            .map(_ => s"the offer starting ${offer.span.start} for ${offer.span.duration} was successfully added")
          complete(future)
        }
      } ~ (path("offer_update") & post) {
        entity(as[Offer[Int]]) { offer: Offer[Int] =>
          val future = offerApi.upsertOffer(offer)
            .map(_ => s"the offer with offer_id ${offer.id} was successfully updated")
          complete(future)
        }
      } ~ (path("offer" / "delete" / IntNumber) & delete) { offerId =>
        val future = offerApi.deleteOffer(offerId).map(_=> s"the offer with offer_id $offerId/h was permanently deleted")
        complete(future)
      } ~ path("offer_by_status" / Segment) { status =>
        val future = offerApi.offerByStatus(OfferStatus.withNameInsensitive(status))
        complete(future)
      }
    }
}

object Routes {
  private def jsonContentTypes: List[ContentTypeRange] =
    List(`application/json`)

  implicit def unmarshaller[Entity](implicit ev: Decoder[Entity]): FromEntityUnmarshaller[Entity] = {
    Unmarshaller.stringUnmarshaller
      .forContentTypes(jsonContentTypes: _*)
      .flatMap { _ => _ => json =>
        decode[Entity](json).fold(Future.failed, Future.successful)
      }
  }
  implicit def marshaller[P](implicit ev: Encoder[P]): ToEntityMarshaller[P] = {
    Marshaller.withFixedContentType(`application/json`) { entity =>
      HttpEntity(`application/json`, entity.asJson.noSpaces)
    }
  }
}

