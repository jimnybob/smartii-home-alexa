package uk.co.smartii.alexa.services


import com.typesafe.slick.testkit.util.{DriverTest, ExternalJdbcTestDB, TestDB, Testkit}
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import play.api.libs.ws.WSClient
import slick.driver.{H2Driver, JdbcDriver}
import uk.co.smartii.model.{HttpCall, Tables}
import Tables._
import Tables.profile.api._

/**
  * Created by jimbo on 04/01/17.
  */
class InfraRedServiceSpec extends FlatSpec with Matchers with MockFactory with BeforeAndAfterAll {

  var db: Tables.profile.backend.DatabaseDef = null
  override protected def beforeAll(): Unit = {
    // Note: 'testdb' not 'test' as this is already defined in build.sbt
    val url = s"jdbc:h2:mem:testdb;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=runscript from '${getClass.getResource("/drop.sql").getPath}'\\;runscript from '${getClass.getResource("/create.sql").getPath}'\\;runscript from '${getClass.getResource("/populate.sql").getPath}'"
    db = Database.forURL(url, driver = "org.h2.Driver")
  }

  "The infrared service" should "handle Amazon requests to discover devices" in {

    val ws = mock[WSClient]
    val irService = new InfraRedService(null, null, ws, Tables, db)

    val outcome = irService.discover
    outcome.length should be (1)
    outcome.head.applianceId should be("kitchenHiFi")
    outcome.head.actions.head.action should be("turnOn")
    outcome.head.actions.head.events.length should be(3)
    outcome.head.actions.head.events.head shouldBe a [HttpCall]
    outcome.head.actions.head.events.head.asInstanceOf[HttpCall].path should be("/hifi/AUX")
    outcome.head.actions.head.events.last shouldBe a [HttpCall]
    outcome.head.actions.head.events.last.asInstanceOf[HttpCall].path should be("/hifi/POWER")
  }

  it should "handle requests to turn on a device" in {

    val ws = mock[WSClient]
    val irService = new InfraRedService(null, null, ws, Tables, db)

    irService.turnOn("kitchenHifi") should be(true)
  }

  it should "convert item to JSON" in {

  }

}
