package uk.co.smartii.alexa.model

import java.util.concurrent.TimeUnit

import play.api.libs.json._

import scala.concurrent.duration.FiniteDuration
import scala.util.Try


/**
  * Created by jimbo on 06/01/17.
  */
sealed trait Event {
  def order: Int
}

object Event {
  implicit val writeJson: Writes[Event] = new Writes[Event]{
    override def writes(o: Event): JsValue = o match { case h: HttpCall => Json.toJson(h)(HttpCall.formatJson) }
  }
}

//case class Sleep(seconds: Int, order: Int) extends Event

case class Delay(time: Long, units: TimeUnit) {
  def asDuration = FiniteDuration(time, units)
}

object Delay {
  implicit val timeUnitReads: Reads[TimeUnit] = new Reads[TimeUnit] {
    def reads(json: JsValue): JsResult[TimeUnit] = json match {
      case JsString(s) => Try(JsSuccess(TimeUnit.valueOf(s)))
        .getOrElse(JsError(s"Enumeration expected of type: 'TimeUnit', but it does not appear to contain the value: '$s'"))
      case _ => JsError("String value expected")
    }
  }

  implicit val timeUnitWrites: Writes[TimeUnit] = Writes(v => JsString(v.toString))

  implicit val timeUnitFormat: Format[TimeUnit] = Format(timeUnitReads, timeUnitWrites)

  implicit val formatJson = Json.format[Delay]
}

/**
  *
  * @param method GET, POST etc.
  * @param path
  */
case class HttpCall(method: String, path: String, order: Int, delay: Option[Delay]= None) extends Event

object HttpCall {
  implicit val formatTimeUnit = Delay.timeUnitFormat
  implicit val formatDelayJson = Json.format[Delay]
  implicit val formatJson = Json.format[HttpCall]
}