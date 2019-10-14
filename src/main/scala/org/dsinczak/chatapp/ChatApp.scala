package org.dsinczak.chatapp

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration._
import scala.language.postfixOps

object ChatApp extends App {

  implicit val system: ActorSystem = ActorSystem("chat-app-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = system.settings.config
  val host = config.getString("chat-app.host")
  val port = config.getInt("chat-app.port")
  val retryTimeout = 5 seconds

  val sessionManager = system.actorOf(SessionManager.props(
    (actorFactory, user)=> actorFactory.actorOf(UserSession.props(user))
  ))
  val chatService = new ActorChatService(sessionManager, system)(Timeout(retryTimeout))
  new ChatServer(host, port, chatService).start()

}
