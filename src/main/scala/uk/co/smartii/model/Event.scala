package uk.co.smartii.model

/**
  * Created by jimbo on 06/01/17.
  */
sealed trait Event {
  def order: Int
}

case class Sleep(seconds: Int, order: Int) extends Event

/**
  *
  * @param method GET, POST etc.
  * @param path
  */
case class HttpCall(method: String, path: String, order: Int) extends Event
