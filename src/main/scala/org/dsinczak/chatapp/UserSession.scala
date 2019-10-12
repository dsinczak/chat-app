package org.dsinczak.chatapp

import java.time.LocalDateTime.now

import akka.actor.{Actor, ActorLogging, Props}
import org.dsinczak.chatapp.Chat._
import org.dsinczak.chatapp.UserSession.{Messages, Threads}

import scala.collection.mutable

class UserSession(user: User) extends Actor with ActorLogging {

  val threads = new Threads()

  override def receive: Receive = {
    case SendMessage(from, to, content) if user.id == from =>
      log.info("User {} sends message to {}. Sender message add", from, to)
      threads.getOrElseUpdate(to, new Messages()).addOne(UserMessage(now(), content))
    case SendMessage(from, to, content) if user.id == to =>
      log.info("User {} sends message to {}. Recipient message add", from, to)
      threads.getOrElseUpdate(from, new Messages()).addOne(UserMessage(now(), content))
    case GetUserThreadList(_) =>
      log.info("User {} thread list requested", user.id)
      sender() ! UserThreadList(threads.toList.map { case (userId: UserId, messages: Messages) => UserThread(userId, messages.last) })
    case GetUserMessageList(_, withUserId) =>
      log.info("User {} messages exchanged with {} requested", user.id, withUserId)
      sender() ! UserMessageList(threads.getOrElse(withUserId, List()).toList)
  }
}

object UserSession {

  private type Messages = mutable.ListBuffer[UserMessage]
  private type Threads = mutable.HashMap[UserId, Messages]

  def props(user: User): Props = Props(new UserSession(user))
}