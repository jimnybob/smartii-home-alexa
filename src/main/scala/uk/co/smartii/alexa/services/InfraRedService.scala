package uk.co.smartii.alexa.services

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazon.alexa.smarthome.model.DiscoveredAppliance
import play.api.libs.ws.WSClient
import uk.co.smartii.model.{Room => _, _}
import Tables._
import Tables.profile.api._
import uk.co.smartii.alexa.daos.SmartiiApplianceDao

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jimbo on 03/01/17.
  */
class InfraRedService @Inject()(system: ActorSystem, materializer: ActorMaterializer, wsClient: WSClient,
                                smartiiApplianceDao: SmartiiApplianceDao) {

  implicit val implicitSystem = system
  implicit val implicitMaterializer = materializer

//  implicit val system = ActorSystem()
//  implicit val materializer = ActorMaterializer()
//  val wsClient: WSClient = NingWSClient()

  def discover: Seq[ApplianceMapping] = {

    Await.result(smartiiApplianceDao.appliances, 1 minute)
  }

  def turnOn(applianceId: String): Boolean = {

    false
  }

  def send(room: String, device: String, command: String): String = {

    // TODO: SERVICE_URL is simplistic for testing (e.g. http://ip:9000/ir/sony/KEY_POWER)
    val futureResponse = wsClient.url(System.getenv("KITCHEN_HIFI_IR_SERVICE_URL")).get()
    // Has to be synchronous :(
    /*val response = */Await.result(futureResponse, 5 seconds)
""
//    response.json.as[String]
  }
}
