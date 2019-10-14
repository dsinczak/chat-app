package org.dsinczak.chatapp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.pattern.AskTimeoutException
import akka.stream.ActorMaterializer
import org.dsinczak.chatapp.ChatProtocol.{RecipientNotLoggedIn, User, UserAlreadyLoggedIn, UserNotLoggedIn}
import org.dsinczak.chatapp.ChatService.ChatException

import scala.concurrent.ExecutionContext
import scala.io.StdIn

class ChatServer(host: String, port: Int, chatService: ChatService)(implicit system: ActorSystem, materializer: ActorMaterializer) extends Directives with JsonSupport {

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
    case ChatException(UserAlreadyLoggedIn(userId)) => complete(HttpResponse(Conflict, entity = s"User '$userId' is already logged-in."))
    case ChatException(UserNotLoggedIn(userId)) => complete(HttpResponse(Unauthorized, entity = s"User '$userId' is not logged in."))
    case ChatException(RecipientNotLoggedIn(userId)) => complete(HttpResponse(Unauthorized, entity = s"Message recipient '$userId' is not logged in"))
    case _: AskTimeoutException => complete(HttpResponse(RequestTimeout, entity = s"System was not able to respond in desired time"))
    case ex =>
      system.log.error(ex, "WTF (What a Terrible Failure)")
      complete(HttpResponse(InternalServerError, entity = s"Something went wrong"))
  }

  val route: Route =
    handleExceptions(exceptionHandler) {
      path("chat" / "session") {
        get {
          onSuccess(chatService.users()) { usersList => complete(usersList.users) }
        } ~
          post {
            entity(as[User]) { user => onSuccess(chatService.join(user)) { _ => complete(HttpResponse(OK)) } }
          }
      } ~
        path("chat" / "session" / Segment) { userId =>
          delete {
            onSuccess(chatService.leave(userId)) { _ => complete(HttpResponse(OK)) }
          }
        } ~
        path("chat" / Segment / "thread") { userId =>
          get {
            onSuccess(chatService.threadsSummaries(userId)) { tsl => complete(tsl.threads) }
          }
        } ~
        path("chat" / Segment / "thread" / Segment) { (userId, withUserId) =>
          get {
            onSuccess(chatService.threadMessages(userId, withUserId)) { msgl => complete(msgl.messages) }
          } ~
            post {
              entity(as[String]) { message =>
                onSuccess(chatService.send(userId, withUserId, message)) { _ => complete(HttpResponse(OK)) }
              }
            }
        }
    }
}
