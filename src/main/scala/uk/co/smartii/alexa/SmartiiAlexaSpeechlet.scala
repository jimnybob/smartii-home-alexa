package uk.co.smartii.alexa

import java.io.{IOException, InputStream, OutputStream}
import java.util.UUID
import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazon.alexa.smarthome.model._
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.io.IOUtils
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient
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
      case "TurnOnRequest" =>             writeResponse(Json.fromJson[TurnOnRequest](json).get, output)
      case "TurnOffRequest" =>            writeResponse(Json.fromJson[TurnOffRequest](json).get, output)
      case "DiscoverAppliancesRequest" => writeResponse(Json.fromJson[DiscoverAppliancesRequest](json).get, output)
      case "HealthCheckRequest"=>         writeResponse(Json.fromJson[HealthCheckRequest](json).get, output)
    }
//    json.validate[SmartHomeControlRequest] match {
//      case JsSuccess(shcr, _) => output.write(shcr.header.name.getBytes)
//      case JsError(errors) =>  println("Error parsing JSON: " + errors); throw new IOException("")
//    }
  }

  private def writeResponse(turnOnRequest: TurnOnRequest, outputStream: OutputStream) {

    infraRedService.send("", "", "")
    val response = TurnOnConfirmation(header = Header(
      messageId = UUID.randomUUID().toString,
      name = "TurnOnConfirmation",
      namespace = "Alexa.ConnectedHome.Control",
      payloadVersion = "2"),payload = EmptyPayload())

    outputStream.write(Json.toJson(response).toString().getBytes)
  }

  private def writeResponse(turnOffRequest: TurnOffRequest, outputStream: OutputStream) {

    infraRedService.send("", "", "")
    val response = TurnOffConfirmation(header = Header(
      messageId = UUID.randomUUID().toString,
      name = "TurnOffConfirmation",
      namespace = "Alexa.ConnectedHome.Control",
      payloadVersion = "2"),payload = EmptyPayload())

    outputStream.write(Json.toJson(response).toString().getBytes)
  }

  private def writeResponse(discoverAppliancesRequest: DiscoverAppliancesRequest, outputStream: OutputStream) {

    val response = DiscoverAppliancesResponse(header = Header(
      messageId = UUID.randomUUID().toString,
      name = "DiscoverAppliancesResponse",
      namespace = "Alexa.ConnectedHome.Discovery",
      payloadVersion = "2"),
      payload = DiscoveredAppliancesPayload(discoveredAppliances = infraRedService.discover.map(_.toDiscoveredAppliance))
    )

    outputStream.write(Json.toJson(response).toString().getBytes)
  }

  private def writeResponse(healthCheckRequest: HealthCheckRequest, outputStream: OutputStream) {

    val response = HealthCheckResponse(header = Header(
      messageId = UUID.randomUUID().toString,
      name = "HealthCheckResponse",
      namespace = "Alexa.ConnectedHome.System",
      payloadVersion = "2"),
      payload = HealthResponsePayload(description = "The system is very healthy", isHealthy = true))

    outputStream.write(Json.toJson(response).toString().getBytes)
  }
}
