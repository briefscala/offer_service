package com.briefml

import com.typesafe.config.Config

case class Configuration(
  appName: String,
  interface: Interface,
  config: Config
)

case class Interface(
  host: String,
  port: Int
)

object Configuration {
  def apply(config: Config): Configuration = Configuration(
    config = config,
    appName = config.getString("offer.app-name"),
    interface = Interface(
      config.getString("offer.interface"),
      port = config.getInt("offer.port")
    )
  )
}
