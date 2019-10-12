package org.dsinczak.chatapp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.io.StdIn

class ChatServer(host: String, port: Int)(implicit system: ActorSystem, materializer: ActorMaterializer) extends Directives {

  implicit val executionContext: ExecutionContext = system.dispatcher

  def start(): Unit = {
    val bindingFuture = Http().bindAndHandle(route, host, port)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  val route: Route =
    path("chat") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
}
