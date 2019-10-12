package org.dsinczak.chatapp

import akka.actor.Status.Success
import akka.actor.{Actor, ActorLogging, Props}
import org.dsinczak.chatapp.Chat.User

class UserSession(user: User) extends Actor with ActorLogging {

  override def receive: Receive = {
    case _ => sender() ! Success
  }

}
object UserSession {
  def props(user: User): Props = Props(new UserSession(user))
}