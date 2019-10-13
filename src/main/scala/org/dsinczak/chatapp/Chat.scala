package org.dsinczak.chatapp

import java.time.LocalDateTime

object Chat {

  type UserName = String
  type UserId = String
  type MessageContent = String
  type Timestamp = LocalDateTime

  case class User(userId: UserId, name: UserName)
  case class UserList(users: List[User]) extends AnyVal

  case class UserMessage(timestamp: Timestamp, content: MessageContent)
  case class UserMessageList(messages: List[UserMessage]) extends AnyVal

  case class UserThreadSummary(userId: UserId, lastMessage: UserMessage)
  case class UserThreadSummaryList(threads: List[UserThreadSummary]) extends AnyVal

  sealed trait ChatCommand
  case class SendMessage(from: UserId, to: UserId, content: MessageContent) extends ChatCommand
  object SendMessage {
    def apply(from: User, to: User, content: MessageContent): SendMessage = new SendMessage(from.userId, to.userId, content)
  }
  case class Join(user: User) extends ChatCommand
  case class Leave(userId: UserId) extends ChatCommand
  case object GetUserList extends ChatCommand
  case class GetUserThreadSummaryList(userId: UserId) extends ChatCommand
  object GetUserThreadSummaryList {
    def apply(user: User): GetUserThreadSummaryList = new GetUserThreadSummaryList(user.userId)
  }
  case class GetUserMessageList(userId: UserId, withUserId: UserId) extends ChatCommand
  object GetUserMessageList {
    def apply(user: User, withUser:User): GetUserMessageList = new GetUserMessageList(user.userId, withUser.userId)
  }

  sealed trait ChatEvent
  case class UserAlreadyLoggedIn(userId: UserId) extends ChatEvent
  case class UserNotLoggedIn(userId: UserId) extends ChatEvent
  case class RecipientNotLoggedIn(userId:UserId) extends ChatEvent

}
