package uk.co.smartii.alexa.model

import com.amazon.alexa.smarthome.model.DiscoveredAppliance
import com.amazon.alexa.smarthome.model.Builders._

/**
  *
  * @param applianceId as defined in the DiscoveredAppliance
  * @param actions the actions that can be performed against the appliance (e.g. turn it on)
  * @param room
  */
case class Appliance(applianceId: String, name: String, description: String, room: Room, actions: Seq[ApplianceAction]) {

  def toDiscoveredAppliance: DiscoveredAppliance = {
    discoveredAppliance(actions = actions.map(_.action), applianceId = applianceId, friendlyName = name,
      friendlyDescription = description)
  }
}

