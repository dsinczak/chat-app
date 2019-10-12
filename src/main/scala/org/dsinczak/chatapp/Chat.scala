package org.dsinczak.chatapp

import java.time.LocalDateTime

object Chat {

  type UserName = String
  type UserId = String
  type MessageContent = String
  type Timestamp = LocalDateTime

  case class User(id: UserId, name: UserName)
  case class UserList(users: List[User])

  case class UserMessage(timestamp: Timestamp, content: MessageContent)
  case class UserThread(userId: UserId, userMessage: UserMessage)

  case class UserThreadList(threads: List[UserThread])
  case class UserMessageList(messages: List[UserMessage])

  sealed trait ChatCommand
  case class SendMessage(from: UserId, to: UserId, content: MessageContent) extends ChatCommand
  case class Join(user: User) extends ChatCommand
  case class Leave(userId: UserId) extends ChatCommand
  case object GetUserList extends ChatCommand
  case class GetUserThreadList(userId: UserId) extends ChatCommand
  case class GetUserMessageList(userId: UserId, withUserId: UserId) extends ChatCommand

  sealed trait ChatEvent
  case class UserAlreadyLoggedIn(userId: UserId) extends ChatEvent
  case class UserNotLoggedIn(userId: UserId) extends ChatEvent
  case class RecipientNotLoggedIn(userId:UserId) extends ChatEvent

}
