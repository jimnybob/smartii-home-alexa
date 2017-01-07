package uk.co.smartii.alexa.daos

import javax.inject.Inject

import uk.co.smartii.model
import uk.co.smartii.model.{Room => _, _}
import Tables._
import Tables.profile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by jimbo on 07/01/17.
  */
trait SmartiiApplianceDao {
  def appliances: Future[Seq[ApplianceMapping]]
}


class SmartiiApplianceDaoImpl @Inject()(tables: Tables,
                                    db: Tables.profile.backend.DatabaseDef) {

  type DatabaseId = Int

  def rooms: Future[Seq[(Int, model.Room)]] = {
    db.run(Room.result.map(_.map(row => row.id -> model.Room(name = row.name))))
  }

  def appliance(applianceId: String): Future[Option[ApplianceMapping]] = {
    val futureRooms = rooms
    val futureAppliance = db.run(Appliancemapping.filter(_.applianceid === applianceId).result.map(_.headOption))

    for {
      roomsMap <- futureRooms.map(_.toMap)
      applianceOption <- futureAppliance
      appEvents <- applianceOption.fold[Future[Seq[ApplianceAction]]](Future.successful(Seq.empty))(appliance => applianceEvents(appliance.id))
    } yield {
      applianceOption.map { row => ApplianceMapping(
        applianceId = row.applianceid,
        room = roomsMap.getOrElse(row.roomid, throw new IllegalStateException(s"Couldn't find any room with id '${row.roomid}'")).name,
        actions = appEvents)
      }
    }
  }

  def appliances: Future[Seq[ApplianceMapping]] = {
    val futureRooms = rooms
    val futureAppliances = db.run(Appliancemapping.result)
    for {
      roomsMap <- futureRooms.map(_.toMap)
      appliances <- futureAppliances
      appEventsMap <- Future.sequence(appliances.map { appliance => applianceEvents(appliance.id).map(events => appliance.id -> events) }).map(_.toMap)
    } yield {
      appliances.map { row => ApplianceMapping(
      applianceId = row.applianceid,
      room = roomsMap.getOrElse(row.roomid, throw new IllegalStateException(s"Couldn't find any room with id '${row.roomid}'")).name,
      actions = appEventsMap.getOrElse(row.id, Seq.empty))}
    }
  }

  def applianceEvents(applianceMappingId: DatabaseId): Future[Seq[ApplianceAction]] = {

    val futureSleepEvents = db.run(Sleepevent.filter(_.appliancemappingid === applianceMappingId).map(ev => ev.appliancemappingeventsid -> ev).result)
    val futureHttpCallEvents = db.run(Httpcallevent.filter(_.appliancemappingid === applianceMappingId).map(ev => ev.appliancemappingeventsid -> ev).result)

    val futureApplianceEvents = db.run(Appliancemappingevents.filter(_.appliancemappingid === applianceMappingId).result)

    for {
      applianceEvents <- futureApplianceEvents
      applianceEventOrderMap = applianceEvents.map(row => row.id -> row.eventorder).toMap
      sleepEvents <- futureSleepEvents
      sleepEventsMap = sleepEvents.map { case (appMapEventsId, row) =>
        appMapEventsId -> Sleep(
          seconds = row.seconds,
          order = applianceEventOrderMap.getOrElse(row.appliancemappingeventsid, throw new IllegalStateException(s"No event order defined for appliance sleep event '${row.appliancemappingeventsid}'")))
      }.toMap
      httpEvents <- futureHttpCallEvents
      httpEventsMap = httpEvents.map { case (appMapEventsId, row) =>
        appMapEventsId -> HttpCall(
          method = row.method,
          path = row.path,
          order = applianceEventOrderMap.getOrElse(row.appliancemappingeventsid, throw new IllegalStateException(s"No event order defined for appliance hhtp event '${row.appliancemappingeventsid}'")))
      }.toMap
    } yield {
      applianceEvents.groupBy(_.action).map{ case(action, appEventsAgain) =>
        ApplianceAction(action = action, events = appEventsAgain.map(_.id).map( appEventId =>
          sleepEventsMap.get(appEventId).orElse(httpEventsMap.get(appEventId)).getOrElse(throw new IllegalStateException(s"Couldn't find any appliance event with id '$appEventId'"))
        ).sortBy(_.order)
        )
      }.toSeq
    }
  }
}
