package org.dsinczak.chatapp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.io.StdIn

class ChatServer(host: String, port: Int, chatService: ChatService)(implicit system: ActorSystem, materializer: ActorMaterializer) extends Directives {

  implicit val executionContext: ExecutionContext = system.dispatcher

  def start(): Unit = {
    val bindingFuture = Http().bindAndHandle(route, host, port)

    println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return

    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  val exceptionHandler = ExceptionHandler {
    case e: Exception => complete(HttpResponse(InternalServerError, entity = "it broke!!!"))
  }

  val route: Route =
    handleExceptions(exceptionHandler) {
      path("chat" / "session") {
        get
          onSuccess(chatService.users()) { users =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Users list requested"))
          }
        } ~
        path("chat" / "session" / Segment) { userId: String =>
          concat(
            post {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"User $userId is joining messenger"))
            },
            delete {
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"User $userId left messenger"))
            }
          )
        } ~
        path("chat" / Segment / "thread") { userId =>
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Get $userId threads"))
          }
        } ~
        path("chat" / Segment / "thread" / Segment) { (userId, withUserId) =>
          get {

            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Get $userId messages with user $withUserId"))
          }
        } ~
        path("chat" / Segment / "thread" / Segment) { (userId, withUserId) =>
          post {
            entity(as[String]) { message =>
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Send message '$message' from $userId to $withUserId"))
            }
          }
        }
    }
}
