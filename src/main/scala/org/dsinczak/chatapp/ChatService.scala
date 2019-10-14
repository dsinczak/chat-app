package org.dsinczak.chatapp

import akka.actor.Status.Success
import org.dsinczak.chatapp.ChatProtocol.{ChatAck, ChatError, MessageContent, User, UserId, UserList, UserMessageList, UserThreadSummaryList}

import scala.concurrent.Future

trait ChatService extends SessionManagerService with UserSessionService { }
object ChatService {

  /**
   * This could be done 2 ways:
   * - future ending up exceptionally (like this)
   * - using Future[Either[ChatEvent, ResultType]] as service response.
   *   But then to simplify i should use EitherT to handle inner monad and i did not want to introduce too much
   * @param chatEvent reason of error
   */
  case class ChatException(chatEvent: ChatError) extends RuntimeException
}
trait SessionManagerService {
  def join(user: User): Future[ChatAck]
  def leave(userId: UserId): Future[ChatAck]
  def users(): Future[UserList]
}
trait UserSessionService {
  def send(from:UserId, to: UserId, messageContent: MessageContent): Future[ChatAck]
  def threadsSummaries(userId: UserId): Future[UserThreadSummaryList]
  def threadMessages(userId: UserId, withUserId:UserId): Future[UserMessageList]
}

