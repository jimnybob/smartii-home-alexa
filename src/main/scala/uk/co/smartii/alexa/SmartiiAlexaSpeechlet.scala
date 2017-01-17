package uk.co.smartii.alexa

import java.io.{InputStream, OutputStream}
import java.util.UUID
import javax.inject.Inject

import cats.data.Reader
import com.amazon.alexa.smarthome.model.Builders._
import com.amazon.alexa.smarthome.model._
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import org.apache.commons.io.IOUtils
import play.api.libs.json.Json
import uk.co.smartii.alexa.model.ActionOutcome
import uk.co.smartii.alexa.services.InfraRedService

/**
  * Created by jimbo on 02/01/17.
  */
class SmartiiAlexaSpeechlet @Inject()(infraRedService: InfraRedService) extends RequestStreamHandler {

  override def handleRequest(inputStream: InputStream, output: OutputStream, context: Context): Unit = {

    val requestBytes = IOUtils.toByteArray(inputStream)
    val requestString = IOUtils.toString(requestBytes, "utf-8")
    println(requestString)
    val json = Json.parse(requestString)
    (json \ "header" \ "name").as[String] match {
      case "TurnOnRequest" =>             writeResponse(Json.fromJson[TurnOnRequest](json).get, output).run(context)
      case "TurnOffRequest" =>            writeResponse(Json.fromJson[TurnOffRequest](json).get, output).run(context)
      case "DiscoverAppliancesRequest" => writeResponse(Json.fromJson[DiscoverAppliancesRequest](json).get, output)
      case "HealthCheckRequest"=>         writeResponse(Json.fromJson[HealthCheckRequest](json).get, output)
    }
//    json.validate[SmartHomeControlRequest] match {
//      case JsSuccess(shcr, _) => output.write(shcr.header.name.getBytes)
//      case JsError(errors) =>  println("Error parsing JSON: " + errors); throw new IOException("")
//    }
  }

  private def writeResponse(turnOnRequest: TurnOnRequest, outputStream: OutputStream): Reader[Context, Unit] = {

    infraRedService.change(turnOnRequest.payload.appliance.applianceId, SmartHomeAction.turnOn).map { outcome =>
      val response = outcome match {
        case ActionOutcome.SUCCESS => Json.toJson(turnOnConfirmation())
        case ActionOutcome.INVALIDTOKEN => Json.toJson(error("InvalidAccessTokenError"))
        case ActionOutcome.OFFLINE => Json.toJson(error("TargetOfflineError"))
        case ActionOutcome.UNSUPPORTEDACTION => Json.toJson(error("UnsupportedOperationError"))
      }

      outputStream.write(response.toString().getBytes)
    }
  }

  private def writeResponse(turnOffRequest: TurnOffRequest, outputStream: OutputStream): Reader[Context, Unit] = {

    infraRedService.change(turnOffRequest.payload.appliance.applianceId, SmartHomeAction.turnOff).map { outcome =>
      val response = outcome match {
        case ActionOutcome.SUCCESS => Json.toJson(turnOffConfirmation())
        case ActionOutcome.INVALIDTOKEN => Json.toJson(error("InvalidAccessTokenError"))
        case ActionOutcome.OFFLINE => Json.toJson(error("TargetOfflineError"))
        case ActionOutcome.UNSUPPORTEDACTION => Json.toJson(error("UnsupportedOperationError"))
      }

      outputStream.write(response.toString().getBytes)
    }
  }

  private def writeResponse(discoverAppliancesRequest: DiscoverAppliancesRequest, outputStream: OutputStream) {

    val response = DiscoverAppliancesResponse(header = Header(
      messageId = UUID.randomUUID().toString,
      name = "DiscoverAppliancesResponse",
      namespace = "Alexa.ConnectedHome.Discovery",
      payloadVersion = "2"),
      payload = DiscoveredAppliancesPayload(discoveredAppliances = infraRedService.discover.map(_.toDiscoveredAppliance))
    )

    println(Json.prettyPrint(Json.toJson(response)))
    outputStream.write(Json.toJson(response).toString().getBytes)
  }

  private def writeResponse(healthCheckRequest: HealthCheckRequest, outputStream: OutputStream) {

    val response = healthCheck(description = "The system is very healthy", isHealthy = true)

    outputStream.write(Json.toJson(response).toString().getBytes)
  }
}
