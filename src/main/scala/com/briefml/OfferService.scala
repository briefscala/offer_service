package com.briefml

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.briefml.models.OfferRepository
import com.typesafe.config.ConfigFactory

object OfferService {
  def main(args: Array[String]): Unit = {

    val configuration = Configuration(ConfigFactory.load())

    val serviceName = configuration.appName
    implicit val system: ActorSystem = ActorSystem(serviceName, configuration.config)
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val route = Routes(OfferApi(OfferRepository)).route

    Http().bindAndHandle(route, configuration.interface.host, configuration.interface.port)

    println(s"Service $serviceName is running at ${configuration.interface.host}:${configuration.interface.port}...")
  }
}
