package org.dsinczak.chatapp

import akka.actor.{ActorRef, ActorSystem, Status}
import akka.pattern.ask
import akka.util.Timeout
import org.dsinczak.chatapp.ChatProtocol._

import scala.concurrent.Future

class ActorChatService(sessionManager: ActorRef, actorSystem: ActorSystem)(implicit askTimeout: Timeout) extends ChatService {

  override def join(user: ChatProtocol.User): Future[Status.Success] = (sessionManager ? Join(user)).mapTo[Status.Success]

  override def leave(userId: UserId): Future[Status.Success] = (sessionManager ? Leave(userId)).mapTo[Status.Success]

  override def users(): Future[ChatProtocol.UserList] = (sessionManager ? GetUserList).mapTo[ChatProtocol.UserList]

  override def send(from: UserId, to: UserId, messageContent: MessageContent): Future[Status.Success] = (sessionManager ? SendMessage(from, to, messageContent)).mapTo[Status.Success]

  override def threadsSummaries(userId: UserId): Future[ChatProtocol.UserThreadSummaryList] = (sessionManager ? GetUserThreadSummaryList).mapTo[ChatProtocol.UserThreadSummaryList]

  override def threadMessages(userId: UserId, withUserId: UserId): Future[ChatProtocol.UserMessageList] = (sessionManager ? GetUserMessageList(userId, withUserId)).mapTo[ChatProtocol.UserMessageList]

}
