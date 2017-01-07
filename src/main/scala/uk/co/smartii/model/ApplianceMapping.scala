package uk.co.smartii.model

/**
  *
  * @param applianceId as defined in the DiscoveredAppliance
  * @param actions the actions that can be performed against the appliance (e.g. turn it on)
  * @param room
  */
case class ApplianceMapping(applianceId: String, room: String, actions: Seq[ApplianceAction])
