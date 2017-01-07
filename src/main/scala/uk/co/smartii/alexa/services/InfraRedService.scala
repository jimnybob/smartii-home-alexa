package uk.co.smartii.alexa.services

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazon.alexa.smarthome.model.DiscoveredAppliance
import play.api.libs.ws.WSClient
import uk.co.smartii.model.{Room => _, _}
import Tables._
import Tables.profile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jimbo on 03/01/17.
  */
class InfraRedService @Inject()(system: ActorSystem, materializer: ActorMaterializer, wsClient: WSClient, tables: Tables,
                                db: Tables.profile.backend.DatabaseDef) {

  implicit val implicitSystem = system
  implicit val implicitMaterializer = materializer

//  implicit val system = ActorSystem()
//  implicit val materializer = ActorMaterializer()
//  val wsClient: WSClient = NingWSClient()

  def discover: Seq[ApplianceMapping] = {

    // TODO: move this logic into DAO
    val futureRooms = db.run((for {
      rooms <- Room
    } yield (rooms)).result)

    val futureAppliances = db.run((for {
      apps <- Appliancemapping
    } yield (apps)).result)

    val futureSleepEvents = db.run((for {
      events <- Sleepevent
    } yield (events.appliancemappingeventsid -> events)).result)

    val futureHttpEvents = db.run((for {
      events <- Httpcallevent
    } yield (events.appliancemappingeventsid -> events)).result)

    val futureApplianceEvents = db.run((for {
      events <- Appliancemappingevents
    } yield (events)).result)

    val allFutures = (for {
      rooms <- futureRooms
      roomsMap = rooms.map(r=> r.id -> uk.co.smartii.model.Room(r.name)).toMap
      appliances <- futureAppliances
      applianceEvents <- futureApplianceEvents
      applianceEventOrderMap = applianceEvents.map(row => row.id -> row.eventorder).toMap
      sleepEvents <- futureSleepEvents
      sleepEventsMap = sleepEvents.map { case (appMapEventsId, row) => appMapEventsId -> Sleep(seconds = row.seconds, order = applianceEventOrderMap.getOrElse(row.appliancemappingeventsid, throw new IllegalStateException(s"No event order defined for appliance event '${row.appliancemappingeventsid}'"))) }.toMap
      httpEvents <- futureHttpEvents
      httpEventsMap = httpEvents.map { case (appMapEventsId, row) => appMapEventsId -> HttpCall(method = row.method, path = row.path, order = applianceEventOrderMap.getOrElse(row.appliancemappingeventsid, throw new IllegalStateException(s"No event order defined for appliance event '${row.appliancemappingeventsid}'"))) }.toMap
    } yield (roomsMap, appliances, sleepEventsMap, httpEventsMap, applianceEvents))

    Await.result(allFutures.map{ case(roomsMap, appliances, sleepEventsMap, httpEventsMap, ae) =>

      val appEventsMap = ae.groupBy(_.appliancemappingid).map { case (appMapId, appEvents) => appMapId -> appEvents.groupBy(_.action).map{ case(action, appEventsAgain) =>
        ApplianceAction(action = action, events = appEventsAgain.map(_.id).map( appEventId =>
          sleepEventsMap.get(appEventId).orElse(httpEventsMap.get(appEventId)).getOrElse(throw new IllegalStateException(s"Couldn't find any appliance event with id '$appEventId'"))
        ).sortBy(_.order)
        )
      }.toSeq}

      appliances.map { row => ApplianceMapping(
        applianceId = row.applianceid,
        room = roomsMap.getOrElse(row.roomid, throw new IllegalStateException(s"Couldn't find any room with id '${row.roomid}'")).name,
        actions = appEventsMap.getOrElse(row.id, Seq.empty))}

    }, 1 minute)

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
