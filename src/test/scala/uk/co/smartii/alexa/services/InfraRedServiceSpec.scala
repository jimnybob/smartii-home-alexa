package uk.co.smartii.alexa.services


import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.ws.WSClient
import uk.co.smartii.alexa.daos.SmartiiApplianceDao

/**
  * Created by jimbo on 04/01/17.
  */
class InfraRedServiceSpec extends FlatSpec with Matchers with MockFactory {


  "The infrared service" should "handle Amazon requests to discover devices" in {

    fail()
  }

  it should "handle requests to turn on a device" in {

    val ws = mock[WSClient]
    val dao = mock[SmartiiApplianceDao]
    val irService = new InfraRedService(null, null, ws, dao)

    irService.turnOn("kitchenHifi") should be(true)
  }

  it should "convert item to JSON" in {
    fail()
  }

}
