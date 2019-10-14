package org.dsinczak.chatapp

import akka.actor.{Actor, ActorLogging, ActorRef, ActorRefFactory, Props, Terminated}
import akka.event.LoggingReceive
import org.dsinczak.chatapp.ChatProtocol._
import org.dsinczak.chatapp.SessionManager.UserSessionFactory

import scala.collection.mutable

class SessionManager(userSessionFactory: UserSessionFactory) extends Actor with ActorLogging {

  val sessions = new mutable.HashMap[UserId, (User, ActorRef)]()

  override def receive: Receive = LoggingReceive {
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
    case msg:GetUserThreadSummaryList                              => userThreadSummaryList(msg)

    // Message list retrieval
    case GetUserMessageList(userId, _) if isNotLoggedIn(userId) => userNotLoggedIn(userId)
    case msg:GetUserMessageList                                 => userMessageList(msg)

    case Terminated(userSession)  => userTerminated(userSession)
  }

  private def userMessageList(msg: GetUserMessageList): Unit = sessions(msg.userId)._2.forward(msg)

  private def userThreadSummaryList(msg: GetUserThreadSummaryList): Unit =
    sessions(msg.userId)._2.forward(msg)

  private def sendMessage(sendMessage:SendMessage): Unit = {
    sessions(sendMessage.from)._2 ! sendMessage
    sessions(sendMessage.to)._2   ! sendMessage
    sender() ! Done
  }

  private def recipientNotLoggedIn(userId: UserId): Unit = sender() ! RecipientNotLoggedIn(userId)

  private def userNotLoggedIn(userId: UserId): Unit = sender() ! UserNotLoggedIn(userId)

  private def isNotLoggedIn(user: UserId) = !sessions.contains(user)

  private def isLoggedIn(user: User) = sessions.contains(user.userId)

  private def userDuplicateJoin(user: User): Unit = {
    log.info("Duplicate login attempt for user {}", user.userId)
    sender() ! UserAlreadyLoggedIn(user.userId)
  }

  private def userTerminated(terminatedUserSession: ActorRef): Unit =
    sessions.find { case (_, (_, userSession)) => terminatedUserSession == userSession }
      .map { case (userId, (_, _)) => userId }
      .foreach { userId =>
        log.error("User {} session was removed abruptly.", userId)
        sessions.remove(userId)
      }

  private def userList(): Unit = sender() ! UserList(sessions.values.map(_._1).toList)

  private def userLeave(userId: UserId): Unit = {
    sessions.remove(userId)
      .fold(log.warning("User {} was never logged in", userId)) { userSession =>
        context.system.stop(userSession._2)
      }
    sender() ! Done
  }

  private def userJoin(user: User): Unit = {
    val userSession = userSessionFactory(context, user)
    context.watch(userSession)
    sessions += (user.userId -> (user, userSession))
    sender() ! Done
  }
}

object SessionManager {

  // This was added to decouple actors and make them easier to test
  type UserSessionFactory = (ActorRefFactory, User) => ActorRef

  def props(userSessionFactory: UserSessionFactory): Props = Props(new SessionManager(userSessionFactory))

}