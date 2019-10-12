package org.dsinczak.chatapp

import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.dsinczak.chatapp.Chat._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class SessionManagerSpec extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val bigMzUser = User("bigMZ", "Mark Zuckerberg")
  val moonMan = User("moonMan", "Elon Musk")

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SessionManager" when {
    "authentication is requested" must {
      "log-in user when it does not exist" in {
        // Given
        val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))

        // When
        sm ! Join(bigMzUser)

        // Then
        expectMsg(Success)

        // And user list must contain logged-in user
        sm ! GetUserList
        val userList = expectMsgClass(classOf[UserList])
        assert(userList.users.contains(bigMzUser))
      }


      "fail to login already logged in user" in {
        // Given
        val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))
        sm ! Join(bigMzUser)

        // When
        sm ! Join(bigMzUser)

        // Then
        expectMsg(Success)
        expectMsg(UserAlreadyLoggedIn("bigMZ"))
      }

      "log-out user" in {
        // Given
        val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))
        sm ! Join(bigMzUser)

        // When
        sm ! Leave(bigMzUser.id)

        // Then
        expectMsg(Success)

        // And user list must be empty
        sm ! GetUserList
        expectMsg(Success)
        val userList = expectMsgClass(classOf[UserList])
        assert(userList.users.isEmpty)
      }

      "does not allow sending message from not logged-in user" in {
        // Given
        val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))

        // When
        sm ! SendMessage("unknownSender", "unknownRecipient", "doIt!")

        // Then
        expectMsg(UserNotLoggedIn("unknownSender"))
      }

      "does not allow sending message to not logged-in user" in {
        // Given
        val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))
        sm ! Join(bigMzUser)
        expectMsg(Success)

        // When
        sm ! SendMessage(bigMzUser.id, "unknownRecipient", "doIt!")

        // Then
        expectMsg(RecipientNotLoggedIn("unknownRecipient"))
      }
    }

    "message is send" must {

    }
  }

}
