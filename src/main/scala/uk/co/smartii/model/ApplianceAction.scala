package uk.co.smartii.model

/**
  *
  * @param action as defined by Amazon (turnOn, turnOff etc.)
  * @param events turning off a hi-fi may require a web service called followed by a delay followed by another web call
  */
case class ApplianceAction(action: String, events: Seq[Event])
