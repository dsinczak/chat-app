package org.dsinczak.chatapp

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props, Terminated}
import org.dsinczak.chatapp.Chat._
import org.dsinczak.chatapp.SessionManager.UserSessionFactory

import scala.collection.mutable

class SessionManager(userSessionFactory: UserSessionFactory) extends Actor with ActorLogging {

  val sessions = new mutable.HashMap[UserId, (User, ActorRef)]()

  override def receive: Receive = {
    // User management
    case Join(user) if isLoggedIn(user) => userDuplicateJoin(user)
    case Join(user)                     => userJoin(user)
    case Leave(userId)                  => userLeave(userId)
    case GetUserList                    => userList()

    // Message sending
    case SendMessage(from, _, _) if isNotLoggedIn(from) =>
      sender() ! UserNotLoggedIn(from)
    case SendMessage(_, to, _)   if isNotLoggedIn(to) =>
      sender() ! RecipientNotLoggedIn(to)
    case msg@SendMessage(from, to, _) =>
      sessions(from)._2 ! msg
      sessions(to)._2   ! msg
      sender() ! Success
    // Thread list retrieval
    case GetUserThreadList(userId) if isNotLoggedIn(userId) =>
      sender() ! UserNotLoggedIn(userId)
    case msg@GetUserThreadList(userId) =>
      sessions(userId)._2.forward(msg)

    // Message list retrieval
    case GetUserMessageList(userId, _) if isNotLoggedIn(userId) =>
      sender() ! UserNotLoggedIn(userId)
    case msg@GetUserMessageList(userId, _) =>
      sessions(userId)._2.forward(msg)

    case Terminated(userSession)        => userTerminated(userSession)
  }

  private def isNotLoggedIn(user: UserId) = {
    !sessions.contains(user)
  }

  private def isLoggedIn(user: User) = {
    sessions.contains(user.id)
  }

  private def userDuplicateJoin(user: User): Unit = {
    log.info("Duplicate login attempt for user {}", user.id)
    sender() ! UserAlreadyLoggedIn(user.id)
  }

  private def userTerminated(terminatedUserSession: ActorRef): Unit = {
    sessions.find { case (_, (_, userSession)) => terminatedUserSession == userSession }
      .map { case (userId, (_, _)) => userId }
      .foreach { userId =>
        log.error("User {} session was removed abruptly.")
        sessions.remove(userId)
      }
  }

  private def userList(): Unit = {
    log.debug("User list requested")
    sender() ! UserList(sessions.values.map(_._1).toList)
  }

  private def userLeave(userId: UserId): Unit = {
    log.info("User {} logout", userId)
    sessions.remove(userId)
      .fold(log.warning("User {} was never logged in")) { userSession =>
        context.system.stop(userSession._2)
      }
    sender() ! Success
  }

  private def userJoin(user: User): Unit = {
    log.info("Logging in user {}", user)
    val userSession = userSessionFactory(context, user)
    context.watch(userSession)
    sessions += (user.id -> (user, userSession))
    sender() ! Success
  }
}

object SessionManager {

  // This was added to decouple actors and make them easier to test
  type UserSessionFactory = (ActorRefFactory, User) => ActorRef

  def props(userSessionFactory: UserSessionFactory): Props = Props(new SessionManager(userSessionFactory))
}