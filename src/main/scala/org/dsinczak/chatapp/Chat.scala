package org.dsinczak.chatapp

object Chat {

  type UserName = String
  type UserId = String
  type MessageContent = String

  case class User(id: UserId, name: UserName)
  case class UserList(users: List[User])

  sealed trait ChatCommand
  case class SendMessage(to: UserId, content: MessageContent) extends ChatCommand
  case class Join(user: User) extends ChatCommand
  case class Leave(userId: UserId) extends ChatCommand
  case object GetUserList extends ChatCommand

  sealed trait ChatEvent
  case class UserAlreadyLoggedIn(userId: UserId) extends ChatEvent

}
