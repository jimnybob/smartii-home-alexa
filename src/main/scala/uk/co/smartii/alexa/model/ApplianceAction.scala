package uk.co.smartii.alexa.model

import com.amazon.alexa.smarthome.model.SmartHomeAction

/**
  *
  * @param action as defined by Amazon (turnOn, turnOff etc.)
  * @param events turning off a hi-fi may require a web service called followed by a delay followed by another web call
  */
case class ApplianceAction(action: SmartHomeAction.Value, events: Seq[Event])
