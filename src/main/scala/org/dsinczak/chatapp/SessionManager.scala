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
    case SendMessage(from, _, _) if isNotLoggedIn(from) => userNotLoggedIn(from)
    case SendMessage(_, to, _)   if isNotLoggedIn(to)   => recipientNotLoggedIn(to)
    case msg@SendMessage(_,_,_)                         => sendMessage(msg)

    // Thread list retrieval
    case GetUserThreadSummaryList(userId) if isNotLoggedIn(userId) => userNotLoggedIn(userId)
    case msg@GetUserThreadSummaryList(userId)                      => userThreadSummaryList(msg, userId)

    // Message list retrieval
    case GetUserMessageList(userId, _) if isNotLoggedIn(userId) => userNotLoggedIn(userId)
    case msg@GetUserMessageList(userId, _)                      => userMessageList(msg, userId)

    case Terminated(userSession)  => userTerminated(userSession)
  }

  private def userMessageList(msg: GetUserMessageList, userId: UserId): Unit = {
    sessions(userId)._2.forward(msg)
  }

  private def userThreadSummaryList(msg: GetUserThreadSummaryList, userId: UserId): Unit = {
    sessions(userId)._2.forward(msg)
  }

  private def sendMessage(sendMessage:SendMessage): Unit = {
    sessions(sendMessage.from)._2 ! sendMessage
    sessions(sendMessage.to)._2   ! sendMessage
    sender() ! Success
  }

  private def recipientNotLoggedIn(userId: UserId): Unit = {
    sender() ! RecipientNotLoggedIn(userId)
  }

  private def userNotLoggedIn(userId: UserId): Unit = {
    sender() ! UserNotLoggedIn(userId)
  }

  private def isNotLoggedIn(user: UserId) = {
    !sessions.contains(user)
  }

  private def isLoggedIn(user: User) = {
    sessions.contains(user.userId)
  }

  private def userDuplicateJoin(user: User): Unit = {
    log.info("Duplicate login attempt for user {}", user.userId)
    sender() ! UserAlreadyLoggedIn(user.userId)
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
    sessions += (user.userId -> (user, userSession))
    sender() ! Success
  }
}

object SessionManager {

  // This was added to decouple actors and make them easier to test
  type UserSessionFactory = (ActorRefFactory, User) => ActorRef

  def props(userSessionFactory: UserSessionFactory): Props = Props(new SessionManager(userSessionFactory))
}