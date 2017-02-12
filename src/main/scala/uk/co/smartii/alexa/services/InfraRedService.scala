package uk.co.smartii.alexa.services

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.data.Reader
import com.amazon.alexa.smarthome.model.{DiscoveredAppliance, SmartHomeAction}
import com.amazonaws.services.lambda.runtime.Context
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}
import uk.co.smartii.alexa.daos.SmartiiApplianceDao
import uk.co.smartii.alexa.model.Tables.Httpcallevent
import uk.co.smartii.alexa.model._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.mvc.Http.Status

/**
  * Created by jimbo on 03/01/17.
  */
class InfraRedService @Inject()(appConfig: AppConfig,
                                system: ActorSystem,
                                materializer: ActorMaterializer,
                                wsClient: WSClient,
                                smartiiApplianceDao: SmartiiApplianceDao) {

  implicit val implicitSystem = system
  implicit val implicitMaterializer = materializer

  private val defaultTimeout = 11 seconds

  private def httpFutureCall(httpPort: Int, httpCall: HttpCall) = {
    val url = wsClient.url(appConfig.getHomeUrl + ":" + httpPort + httpCall.path).withHeaders("authToken" -> appConfig.getAuthenticationToken)
    httpCall.method.toUpperCase match {
      case "GET" => url.get()
      case "POST" => url.post(JsString(""))
    }
  }

  def discover: Seq[Appliance] = {

    Await.result(smartiiApplianceDao.appliances, 15 seconds)
  }

  def change(applianceId: String, smartHomeAction: SmartHomeAction.Value): Reader[Context, ActionOutcome.Value] = Reader { context =>

    Await.result(smartiiApplianceDao.appliance(applianceId), 15 seconds).fold {
      log(context, s"Can't find appliance with id $applianceId"); ActionOutcome.UNSUPPORTEDACTION
    } { appliance =>
      appliance.actions.find(_.action == smartHomeAction).fold(ActionOutcome.UNSUPPORTEDACTION) { action =>

        log(context, s"Changing appliance ${appliance.name} to $smartHomeAction")
        // TODO: move this delayed execution to run from home as this is burning lamdba time (and it's blocking)

        val future = wsClient.url(appConfig.getHomeUrl + ":" + appliance.room.httpPort + "/irSequence")
          .withHeaders("authToken" -> appConfig.getAuthenticationToken)
          .post(Json.toJson(action.events)).map(_.status)
        Await.result(future, defaultTimeout) match {
          case Status.FORBIDDEN => ActionOutcome.INVALIDTOKEN
          case Status.OK => ActionOutcome.SUCCESS
          case _ => ActionOutcome.OFFLINE
        }
      }
    }
  }

  def change2(applianceId: String, smartHomeAction: SmartHomeAction.Value): Reader[Context, ActionOutcome.Value] = Reader { context =>

    Await.result(smartiiApplianceDao.appliance(applianceId), 15 seconds).fold{log(context, s"Can't find appliance with id $applianceId"); ActionOutcome.UNSUPPORTEDACTION}
    { appliance =>
      appliance.actions.find(_.action == smartHomeAction).fold(ActionOutcome.UNSUPPORTEDACTION){ action =>

        log(context, s"Changing appliance ${appliance.name} to $smartHomeAction")
        // TODO: move this delayed execution to run from home as this is burning lamdba time (and it's blocking)
        val futures = action.events.map {
          case delayedHttpCall @ HttpCall(_, _, _, Some(delay)) => log(context, s"Running delayed http call ${delayedHttpCall.path} ");
            akka.pattern.after(delay.asDuration, system.scheduler)(httpFutureCall(appliance.room.httpPort, delayedHttpCall))
          case httpCall: HttpCall => log(context, s"Running http call ${httpCall.path} "); httpFutureCall(appliance.room.httpPort, httpCall)
        }
        // As some events have delays we have to include those in wait time when blocking
        val responseStatii = Await.result(Future.sequence(futures), (defaultTimeout) + action.events.collect {
          case HttpCall(_, _, _, Some(delay)) => delay.asDuration
        }.foldLeft(0 seconds)(_ + _)).map(_.status)

        // Propagate any non-200 responses as sole outcome
        responseStatii.foldLeft(ActionOutcome.SUCCESS) {
          case(overallStatus, status)  if overallStatus != ActionOutcome.SUCCESS => overallStatus
          case(_, Status.OK)  => ActionOutcome.SUCCESS
          case(_, Status.FORBIDDEN)  => ActionOutcome.INVALIDTOKEN
          case(_, status) if status != Status.OK => ActionOutcome.OFFLINE
        }
      }
    }
  }

  def send(room: String, device: String, command: String): String = {

    // TODO: SERVICE_URL is simplistic for testing (e.g. http://ip:9000/ir/sony/KEY_POWER)
    val futureResponse = wsClient.url(System.getenv("KITCHEN_HIFI_IR_SERVICE_URL")).get()
    // Has to be synchronous :(
    /*val response = */Await.result(futureResponse, 5 seconds)
""
//    response.json.as[String]
  }

  private def log(context: Context, message: String) {
    // TODO: Why isn't the lambda logger logging?
    println("Printing: " + message)
    context.getLogger.log(message)
  }
}
