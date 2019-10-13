package org.dsinczak.chatapp

import java.time.Clock
import java.time.LocalDateTime.now

import akka.actor.{Actor, ActorLogging, Props}
import org.dsinczak.chatapp.Chat._
import org.dsinczak.chatapp.UserSession.{Messages, Threads}

import scala.collection.mutable

class UserSession(user: User, clock: Clock = Clock.systemDefaultZone()) extends Actor with ActorLogging {

  val threads = new Threads()

  override def receive: Receive = {
    case SendMessage(from, to, content) if user.userId == from =>
      log.info("User {} sends message to {}. Sender {} message add: {}", from, to, user.userId, content)
      threads.getOrElseUpdate(to, new Messages()).addOne(UserMessage(now(clock), content))
    case SendMessage(from, to, content) if user.userId == to =>
      log.info("User {} sends message to {}. Recipient {} message add: {}", from, to, user.userId, content)
      threads.getOrElseUpdate(from, new Messages()).addOne(UserMessage(now(clock), content))
    case GetUserThreadSummaryList(_) =>
      log.info("User {} thread list requested", user.userId)
      sender() ! UserThreadSummaryList(threads.toList.map { case (userId: UserId, messages: Messages) => UserThreadSummary(userId, messages.last) })
    case GetUserMessageList(_, withUserId) =>
      log.info("User {} messages exchanged with {} requested", user.userId, withUserId)
      sender() ! UserMessageList(threads.getOrElse(withUserId, List()).toList)
  }
}

object UserSession {

  private type Messages = mutable.ListBuffer[UserMessage]
  private type Threads = mutable.HashMap[UserId, Messages]

  def props(user: User): Props = Props(new UserSession(user))
  def props(user: User, clock: Clock): Props = Props(new UserSession(user, clock))
}