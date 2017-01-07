package uk.co.smartii.alexa.daos

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import play.api.libs.ws.WSClient
import uk.co.smartii.model.{HttpCall, Tables}
import Tables._
import Tables.profile.api._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by jimbo on 07/01/17.
  */
class SmartiiApplianceDaoSpec extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))


  val url = s"jdbc:h2:mem:testdb;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=runscript from '${getClass.getResource("/drop.sql").getPath}'\\;runscript from '${getClass.getResource("/create.sql").getPath}'\\;runscript from '${getClass.getResource("/populate.sql").getPath}'"
  val db = Database.forURL(url, driver = "org.h2.Driver")

  "The smartii appliance dao" should "return discoverable appliances" in {

    val ws = mock[WSClient]
    val dao = new SmartiiApplianceDaoImpl(Tables, db)

    val outcome = dao.appliances.futureValue
    outcome.length should be(1)
    outcome.head.applianceId should be("kitchenHiFi")
    outcome.head.actions.head.action should be("turnOn")
    outcome.head.actions.head.events.length should be(3)
    outcome.head.actions.head.events.head shouldBe a[HttpCall]
    outcome.head.actions.head.events.head.asInstanceOf[HttpCall].path should be("/hifi/AUX")
    outcome.head.actions.head.events.last shouldBe a[HttpCall]
    outcome.head.actions.head.events.last.asInstanceOf[HttpCall].path should be("/hifi/POWER")
  }

}
