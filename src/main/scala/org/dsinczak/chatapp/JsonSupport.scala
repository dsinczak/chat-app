package org.dsinczak.chatapp

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.dsinczak.chatapp.ChatProtocol.{User, UserList, UserMessage, UserMessageList, UserThreadSummary, UserThreadSummaryList}
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object LocalDateTimeFormat extends JsonFormat[LocalDateTime] {
    def write(dateTime: LocalDateTime) = JsString(dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    def read(value: JsValue) = value match {
      case JsString(dateTime) => LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
      case _ => throw DeserializationException("Local date time in ISO format expected")
    }
  }

  implicit val userFormat = jsonFormat2(User)
  implicit val usersFormat = jsonFormat1(UserList)
  implicit val userMessageFormat =  jsonFormat2(UserMessage)
  implicit val threadSummaryFormat = jsonFormat2(UserThreadSummary)
  implicit val threadSummaryListFormat = jsonFormat1(UserThreadSummaryList)

}
