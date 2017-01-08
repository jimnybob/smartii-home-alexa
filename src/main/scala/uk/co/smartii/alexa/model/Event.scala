package uk.co.smartii.alexa.model

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration


/**
  * Created by jimbo on 06/01/17.
  */
sealed trait Event {
  def order: Int
}

//case class Sleep(seconds: Int, order: Int) extends Event

case class Delay(time: Long, units: TimeUnit) {
  def asDuration = FiniteDuration(time, units)
}

/**
  *
  * @param method GET, POST etc.
  * @param path
  */
case class HttpCall(method: String, path: String, order: Int, delay: Option[Delay]= None) extends Event
