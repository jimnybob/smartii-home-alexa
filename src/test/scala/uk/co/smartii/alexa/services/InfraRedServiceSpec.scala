package uk.co.smartii.alexa.services


import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.amazon.alexa.smarthome.model.SmartHomeAction
import com.amazonaws.services.lambda.runtime.{Context, LambdaLogger}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, FlatSpecLike, Matchers}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.mvc.Http.Status
import uk.co.smartii.alexa.daos.SmartiiApplianceDao
import uk.co.smartii.alexa.model._

import scala.concurrent.Future
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActors, TestKit}
import com.typesafe.config.ConfigFactory
import play.api.http.Writeable
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration._

/**
  * Created by jimbo on 04/01/17.
  */
object InfraRedServiceSpec {
  // Define your test specific configuration here
  val config =
    """
    akka {
      loglevel = "WARNING"
    }
    """
}

class InfraRedServiceSpec extends TestKit(ActorSystem("InfraRedServiceSpec", ConfigFactory.parseString(InfraRedServiceSpec.config))) with FlatSpecLike with Matchers with MockFactory {


  "The infrared service" should "handle requests to turn on a device" in {

    val config = mock[AppConfig]
    (config.getHomeUrl _).expects().returns("http://myhouse").anyNumberOfTimes()
    (config.getAuthenticationToken _).expects().returns("asfasdfsda").anyNumberOfTimes()

    val ws = mock[WSClient]
    val dao = mock[SmartiiApplianceDao]
    val appliance = Appliance(applianceId = "", name = "", description = "", room = Room("kitchen", 9000), actions = Seq(ApplianceAction(action = SmartHomeAction.turnOn, events = Seq(HttpCall("get", "/test/path", 0)))))

    (dao.appliance _).expects("kitchenHifi").returns(Future.successful(Some(appliance)))
    val mockRequest = mock[WSRequest]
    val mockResponse = mock[WSResponse]
    (mockResponse.status _).expects().returns(Status.OK).anyNumberOfTimes()
    (mockRequest.withHeaders _).expects(Seq(("authToken", "asfasdfsda"))).returns(mockRequest)
    (mockRequest.post[JsValue](_: JsValue)(_: Writeable[JsValue])).expects(Json.toJson(appliance.actions.head.events), *).returns(Future.successful(mockResponse))
    (ws.url _).expects("http://myhouse:9000/irSequence").returns(mockRequest)
    val irService = new InfraRedService(config, null, null, ws, dao)

    val awsContext = mock[Context]
    val lambdaLogger = mock[LambdaLogger]
    (lambdaLogger.log _).expects(*).anyNumberOfTimes()
    (awsContext.getLogger _).expects().returns(lambdaLogger).anyNumberOfTimes()

    irService.change("kitchenHifi", SmartHomeAction.turnOn).run(awsContext) should be(ActionOutcome.SUCCESS)
  }

  it should "handle requests to turn off a device" in {

    val config = mock[AppConfig]
    (config.getHomeUrl _).expects().returns("http://myhouse").anyNumberOfTimes()
    (config.getAuthenticationToken _).expects().returns("asfasdfsda").anyNumberOfTimes()

    val ws = mock[WSClient]
    val dao = mock[SmartiiApplianceDao]
    val appliance = Appliance(applianceId = "", name = "", description = "", room = Room("kitchen", 9000),
      actions = Seq(ApplianceAction(action = SmartHomeAction.turnOff,
        events = Seq(
          HttpCall("get", "/test/path/aux", 0),
          HttpCall("get", "/test/path/off", 1, Some(Delay(10, TimeUnit.SECONDS)))))))

    (dao.appliance _).expects("kitchenHifi").returns(Future.successful(Some(appliance)))
    val mockRequest = mock[WSRequest]
    val mockResponse = mock[WSResponse]
    (mockResponse.status _).expects().returns(Status.OK).anyNumberOfTimes()
    (mockRequest.withHeaders _).expects(Seq(("authToken", "asfasdfsda"))).returns(mockRequest).anyNumberOfTimes()
    (mockRequest.post[JsValue](_: JsValue)(_: Writeable[JsValue])).expects(Json.toJson(appliance.actions.head.events), *).returns(Future.successful(mockResponse)).anyNumberOfTimes()
    (ws.url _).expects("http://myhouse:9000/irSequence").returns(mockRequest)
//    (ws.url _).expects("http://myhouse:9000/test/path/off").returns(mockRequest)

    val irService = new InfraRedService(config, system, null, ws, dao)

    val awsContext = mock[Context]
    val lambdaLogger = mock[LambdaLogger]
    (lambdaLogger.log _).expects(*).anyNumberOfTimes()
    (awsContext.getLogger _).expects().returns(lambdaLogger).anyNumberOfTimes()

    val start = System.currentTimeMillis()
    within(1 seconds) {
      val start = System.currentTimeMillis()
      irService.change("kitchenHifi", SmartHomeAction.turnOff).run(awsContext) should be(ActionOutcome.SUCCESS)
//      (System.currentTimeMillis() - start).toInt should be > 10000
    }

  }
}
