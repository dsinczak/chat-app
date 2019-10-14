package org.dsinczak.chatapp

import java.time.{Clock, LocalDateTime, ZoneId, ZoneOffset}

import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.testkit.{ImplicitSender, TestKit}
import org.dsinczak.chatapp.ChatProtocol._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * This is complex scenario test
 */
class ComplexChatSpec extends TestKit(ActorSystem(classOf[ComplexChatSpec].getSimpleName))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  val faceMan = User("bigMZ", "Mark Zuckerberg")
  val moonMan = User("moonMan", "Elon Musk")
  val awsMan = User("aswRulezz", "Jeff Bezos")

  val clock: Clock = Clock.fixed(LocalDateTime.now().toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
  val clockTime: LocalDateTime = LocalDateTime.now(clock)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Chat" must {
    "perform complex conversation between users" in {
      // Given
      val chat = system.actorOf(SessionManager.props((actorRefFactory, user) => actorRefFactory.actorOf(UserSession.props(user, clock))))
      // And authenticated users
      chat ! Join(faceMan); expectMsg(Success)
      chat ! Join(moonMan); expectMsg(Success)
      chat ! Join(awsMan);  expectMsg(Success)

      // When user send messages to each other
      chat ! SendMessage(faceMan, moonMan, "My tesla just broke!!"); expectMsg(Success)
      chat ! SendMessage(faceMan, moonMan, "And my friend told me it looks silly"); expectMsg(Success)
      chat ! SendMessage(moonMan, faceMan, "NO REFUNDS!"); expectMsg(Success)
      chat ! SendMessage(faceMan, moonMan, "Check you FB profile..."); expectMsg(Success)

      chat ! SendMessage(faceMan, awsMan, "Can we move FB to AWS?"); expectMsg(Success)
      chat ! SendMessage(awsMan, faceMan, "I can smell they invoice already :D"); expectMsg(Success)
      chat ! SendMessage(faceMan, awsMan, "Will I get a discount if we move instagram too?"); expectMsg(Success)
      chat ! SendMessage(awsMan, faceMan, "Its already so cheap i'm thinking you should even pay more!"); expectMsg(Success)

      chat ! SendMessage(moonMan, awsMan, "Wanna buy tesla?"); expectMsg(Success)
      chat ! SendMessage(awsMan, moonMan, "Go away, I already spoke to Mark!"); expectMsg(Success)

      // Then all users threads must be up to date
      chat ! GetUserThreadSummaryList(faceMan)
      expectMsg(UserThreadSummaryList(List(
        UserThreadSummary("moonMan", UserMessage(clockTime, "Check you FB profile...")),
        UserThreadSummary("aswRulezz", UserMessage(clockTime, "Its already so cheap i'm thinking you should even pay more!"))
      )))

      chat ! GetUserThreadSummaryList(awsMan)
      expectMsg(UserThreadSummaryList(List(
       UserThreadSummary("moonMan",UserMessage(clockTime,"Go away, I already spoke to Mark!")),
       UserThreadSummary("bigMZ",UserMessage(clockTime,"Its already so cheap i'm thinking you should even pay more!"))
      )))

      chat ! GetUserThreadSummaryList(moonMan)
      expectMsg(UserThreadSummaryList(List(
        UserThreadSummary("aswRulezz",UserMessage(clockTime,"Go away, I already spoke to Mark!")),
        UserThreadSummary("bigMZ",UserMessage(clockTime,"Check you FB profile..."))
      )))

      // And all users message lists must be up to date
      chat ! GetUserMessageList(faceMan, moonMan)
      val faceManMoonManMessages = UserMessageList(List(
        UserMessage(clockTime,"My tesla just broke!!"),
        UserMessage(clockTime,"And my friend told me it looks silly"),
        UserMessage(clockTime,"NO REFUNDS!"),
        UserMessage(clockTime,"Check you FB profile...")
      ))
      expectMsg(faceManMoonManMessages)
      // Another user message stack must be identical
      chat ! GetUserMessageList(moonMan, faceMan)
      expectMsg(faceManMoonManMessages)

      chat ! GetUserMessageList(moonMan, awsMan)
      val moonManAwsManMessages = UserMessageList(List(
        UserMessage(clockTime,"Wanna buy tesla?"),
        UserMessage(clockTime,"Go away, I already spoke to Mark!")
      ))
      expectMsg(moonManAwsManMessages)
      // Another user message stack must be identical
      chat ! GetUserMessageList(awsMan, moonMan)
      expectMsg(moonManAwsManMessages)

    }
  }
}
