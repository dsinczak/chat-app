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


  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "SessionManager actor" must {

    "log-in user when it does not exist" in {
      // Given
      val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))
      val user = User("bigMZ", "Mark Zuckerberg")

      // When
      sm ! Join(user)

      // Then
      expectMsg(Success)

      // And user list must contain logged-in user
      sm ! GetUserList
      val userList = expectMsgClass(classOf[UserList])
      assert(userList.users.contains(user))
    }


    "fail to login already logged in user" in {
      // Given
      val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))
      val user = User("bigMZ", "Mark Zuckerberg")
      sm ! Join(user)

      // When
      sm ! Join(user)

      // Then
      expectMsg(Success)
      expectMsg(UserAlreadyLoggedIn("bigMZ"))
    }

    "log-out user" in {
      // Given
      val sm = system.actorOf(SessionManager.props((_, _) => new TestProbe(system).ref))
      val user = User("bigMZ", "Mark Zuckerberg")
      sm ! Join(user)

      // When
      sm ! Leave("bigMZ")

      // Then
      expectMsg(Success)

      // And user list must be empty
      sm ! GetUserList
      expectMsg(Success)
      val userList = expectMsgClass(classOf[UserList])
      assert(userList.users.isEmpty)
    }
  }

}
