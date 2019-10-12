package org.dsinczak.chatapp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer


object ChatApp extends App {

  implicit val system: ActorSystem = ActorSystem("chat-app-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = system.settings.config
  val host = config.getString("chat-app.host")
  val port = config.getInt("chat-app.port")

  new ChatServer(host, port).start()

}
