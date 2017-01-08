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
import slick.jdbc.JdbcBackend

/**
  * Created by jimbo on 06/01/17.
  */
class SmartiiGuiceModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {


    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
//    bind(classOf[ActorSystem]).toInstance(ActorSystem())
    bind(classOf[WSClient]).toInstance(NingWSClient())
    bind(classOf[Tables]).toInstance(Tables)
    bind(classOf[Tables.profile.backend.DatabaseDef]).toInstance(Database.forURL(url = RealAppConfig.getDbUrl, user = RealAppConfig.getDbUser, password = RealAppConfig.getDbPassword))
    bind(classOf[AppConfig]).toInstance(RealAppConfig)
  }
}
