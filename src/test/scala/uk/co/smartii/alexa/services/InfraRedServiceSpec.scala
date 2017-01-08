package uk.co.smartii.alexa.services


import com.amazon.alexa.smarthome.model.SmartHomeAction
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.mvc.Http.Status
import uk.co.smartii.alexa.daos.SmartiiApplianceDao
import uk.co.smartii.alexa.model._

import scala.concurrent.Future

/**
  * Created by jimbo on 04/01/17.
  */
class InfraRedServiceSpec extends FlatSpec with Matchers with MockFactory {


  "The infrared service" should "handle requests to turn on a device" in {

    val config = mock[AppConfig]
    (config.getHomeUrl _).expects().returns("http://myhouse").anyNumberOfTimes()
    val ws = mock[WSClient]
    val dao = mock[SmartiiApplianceDao]
    (dao.appliance _).expects("kitchenHifi").returns(Future.successful(Some(
      Appliance(applianceId = "", name = "", description = "", room = Room("kitchen", 9000), actions = Seq(ApplianceAction(action = SmartHomeAction.turnOn, events = Seq(HttpCall("get", "/test/path", 0))))))
    ))
    val mockRequest = mock[WSRequest]
    val mockResponse = mock[WSResponse]
    (mockResponse.status _).expects().returns(Status.OK).anyNumberOfTimes()
    (mockRequest.get: () => Future[WSResponse]).expects().returns(Future.successful(mockResponse))
    (ws.url _).expects("http://myhouse:9000/test/path").returns(mockRequest)
    val irService = new InfraRedService(config, null, null, ws, dao)

    irService.change("kitchenHifi", SmartHomeAction.turnOn) should be(ActionOutcome.SUCCESS)
  }

}
