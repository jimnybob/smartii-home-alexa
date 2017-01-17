package uk.co.smartii.alexa

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.libs.ws.WSClient
import play.api.libs.ws.ning.NingWSClient
import uk.co.smartii.alexa.model.{AppConfig, RealAppConfig, Tables}
import Tables._
import Tables.profile.api._
import uk.co.smartii.alexa.daos.{SmartiiApplianceDao, SmartiiApplianceDaoImpl}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by jimbo on 06/01/17.
  */
class SmartiiGuiceModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {


    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    bind(classOf[ActorSystem]).toInstance(system)
    bind(classOf[ActorMaterializer]).toInstance(materializer)
    bind(classOf[WSClient]).toInstance(NingWSClient())
    bind(classOf[Tables]).toInstance(Tables)
    val db = Database.forURL(
      driver = "com.mysql.jdbc.Driver",
      url = RealAppConfig.getDbUrl,
      user = RealAppConfig.getDbUser,
      password = RealAppConfig.getDbPassword)
    bind(classOf[Tables.profile.backend.DatabaseDef]).toInstance(db)
    bind(classOf[AppConfig]).toInstance(RealAppConfig)
    bind(classOf[SmartiiApplianceDao]).to(classOf[SmartiiApplianceDaoImpl])

    val dbVersion = Await.result(db.run(sql"""select version();""".as[String]), 10 seconds).head
    dbVersion.startsWith("5") match {
      case true => ()
      case false => throw new IllegalStateException(s"Version check of database failed. It is $dbVersion")
    }
  }
}
