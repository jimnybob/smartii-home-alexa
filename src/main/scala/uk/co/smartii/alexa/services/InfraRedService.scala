package uk.co.smartii.alexa.services

import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.json._

/**
  * Created by jimbo on 03/01/17.
  */
object InfraRedService {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  val wsClient = NingWSClient()

  def send(room: String, device: String, command: String): String = {

    // TODO: SERVICE_URL is simplistic for testing (e.g. http://ip:9000/ir/sony/KEY_POWER)
    val futureResponse = wsClient.url(System.getenv("KITCHEN_HIFI_IR_SERVICE_URL")).get()
    // Has to be synchronous :(
    /*val response = */Await.result(futureResponse, 5 seconds)

//    response.json.as[String]
  }
}
