package org.dsinczak.chatapp

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import org.dsinczak.chatapp.ChatProtocol._
import org.dsinczak.chatapp.ChatService.ChatException

import scala.concurrent.Future

class ActorChatService(sessionManager: ActorRef, actorSystem: ActorSystem)(implicit askTimeout: Timeout) extends ChatService {

  import actorSystem.dispatcher

  override def join(user: ChatProtocol.User): Future[ChatAck] =
    (sessionManager ? Join(user))
      .flatMap {
        case s: ChatAck => Future.successful(s)
        case e: ChatProtocol.ChatError => Future.failed(ChatException(e))
      }

  override def leave(userId: UserId): Future[ChatAck] =
    (sessionManager ? Leave(userId)).flatMap {
      case s: ChatAck => Future.successful(s)
      case e: ChatProtocol.ChatError => Future.failed(ChatException(e))
    }

  override def users(): Future[ChatProtocol.UserList] =
    (sessionManager ? GetUserList).flatMap {
      case l: ChatProtocol.UserList => Future.successful(l)
      case e: ChatProtocol.ChatError => Future.failed(ChatException(e))
    }

  override def send(from: UserId, to: UserId, messageContent: MessageContent): Future[ChatAck] =
    (sessionManager ? SendMessage(from, to, messageContent)).flatMap {
      case s: ChatAck => Future.successful(s)
      case e: ChatProtocol.ChatError => Future.failed(ChatException(e))
    }

  override def threadsSummaries(userId: UserId): Future[ChatProtocol.UserThreadSummaryList] =
    (sessionManager ? GetUserThreadSummaryList(userId)).flatMap {
      case l: ChatProtocol.UserThreadSummaryList => Future.successful(l)
      case e: ChatProtocol.ChatError => Future.failed(ChatException(e))
    }

  override def threadMessages(userId: UserId, withUserId: UserId): Future[ChatProtocol.UserMessageList] =
    (sessionManager ? GetUserMessageList(userId, withUserId)).flatMap {
      case l: ChatProtocol.UserMessageList => Future.successful(l)
      case e: ChatProtocol.ChatError => Future.failed(ChatException(e))
    }

}
