package uk.co.smartii.alexa.daos

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

import uk.co.smartii.alexa.model
import uk.co.smartii.alexa.model.Tables.{Appliancemappingevents, Httpcallevent, Appliance => ApplianceTable, Room => RoomTable}
import uk.co.smartii.alexa.model.Tables.profile.api._
import com.amazon.alexa.smarthome.model.SmartHomeAction
import uk.co.smartii.alexa.model.{ApplianceAction, Tables}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jimbo on 07/01/17.
  */
trait SmartiiApplianceDao {
  def appliance(applianceId: String): Future[Option[model.Appliance]]
  def appliances: Future[Seq[model.Appliance]]
}

@Singleton
class SmartiiApplianceDaoImpl @Inject()(tables: Tables,
                                    db: Tables.profile.backend.DatabaseDef) extends SmartiiApplianceDao {

  type DatabaseId = Int

  def rooms: Future[Seq[(Int, model.Room)]] = {
    db.run(RoomTable.result.map(_.map{case row => println(s"Found room ${row.name}"); row.id -> model.Room(name = row.name, httpPort = row.httpport)}))
  }

  def appliance(applianceId: String): Future[Option[model.Appliance]] = {
    val futureRooms = rooms
    val futureAppliance = db.run(ApplianceTable.filter(_.applianceid === applianceId).result.map(_.headOption))

    for {
      roomsMap <- futureRooms.map(_.toMap)
      applianceOption <- futureAppliance
      appEvents <- applianceOption.fold[Future[Seq[ApplianceAction]]](Future.successful(Seq.empty))(appliance => applianceEvents(appliance.id))
    } yield {
      applianceOption.map { row => model.Appliance(
        applianceId = row.applianceid,
        name = row.name,
        description = row.description,
        room = roomsMap.getOrElse(row.roomid, throw new IllegalStateException(s"Couldn't find any room with id '${row.roomid}'")),
        actions = appEvents)
      }
    }
  }

  def appliances: Future[Seq[model.Appliance]] = {
    println(s"Discovering appliances")
    val futureRooms = rooms
    val futureAppliances = db.run(ApplianceTable.result)
    for {
      roomsMap <- futureRooms.map(_.toMap)
      appliances <- futureAppliances
      appEventsMap <- Future.sequence(appliances.map { appliance => applianceEvents(appliance.id).map(events => appliance.id -> events) }).map(_.toMap)
    } yield {
      println(s"Discovered ${appliances.length} appliances")
      appliances.map { row => model.Appliance(
      applianceId = row.applianceid,
        name = row.name,
        description = row.description,
      room = roomsMap.getOrElse(row.roomid, throw new IllegalStateException(s"Couldn't find any room with id '${row.roomid}'")),
      actions = appEventsMap.getOrElse(row.id, Seq.empty))}
    }
  }

  def applianceEvents(applianceMappingId: DatabaseId): Future[Seq[ApplianceAction]] = {

//    val futureSleepEvents = db.run(Sleepevent.filter(_.appliancemappingid === applianceMappingId).map(ev => ev.appliancemappingeventsid -> ev).result)
    val futureHttpCallEvents = db.run(Httpcallevent.filter(_.appliancemappingid === applianceMappingId).map(ev => ev.appliancemappingeventsid -> ev).result)

    val futureApplianceEvents = db.run(Appliancemappingevents.filter(_.appliancemappingid === applianceMappingId).result)

    for {
      applianceEvents <- futureApplianceEvents
      applianceEventOrderMap = applianceEvents.map(row => row.id -> row.eventorder).toMap
      httpEvents <- futureHttpCallEvents
      httpEventsMap = httpEvents.map { case (appMapEventsId, row) =>
        appMapEventsId -> model.HttpCall(
          method = row.method,
          path = row.path,
          order = applianceEventOrderMap.getOrElse(row.appliancemappingeventsid, throw new IllegalStateException(s"No event order defined for appliance hhtp event '${row.appliancemappingeventsid}'")),
          delay = row.delay.map(delay => model.Delay(delay, row.delayunits.fold(throw new IllegalStateException(s"No units have been specified for delay for event ${row.id}")){units => TimeUnit.valueOf(units)}))
        )
      }.toMap
    } yield {
      applianceEvents.groupBy(_.action).map{ case(action, appEventsAgain) =>
        ApplianceAction(action = SmartHomeAction.withName(action), events = appEventsAgain.map(_.id).map( appEventId =>
          httpEventsMap.getOrElse(appEventId,throw new IllegalStateException(s"Couldn't find any appliance event with id '$appEventId'"))
        ).sortBy(_.order)
        )
      }.toSeq
    }
  }
}
