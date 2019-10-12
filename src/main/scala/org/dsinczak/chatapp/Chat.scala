package org.dsinczak.chatapp

object Chat {

  type UserName = String
  type UserId = String
  type MessageContent = String

  case class User(name: UserName, id: UserId)

  sealed trait ChatMessage
  case class Message(id:UserId, content: MessageContent)
  case class Join(user: User)
  case class Leave(userId: UserId)

}
