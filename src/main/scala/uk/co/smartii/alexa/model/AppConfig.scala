package uk.co.smartii.alexa.model

/**
  * Created by jimbo on 08/01/17.
  */
trait AppConfig {
  def getHomeUrl: String
  def getAuthenticationToken: String
}

object RealAppConfig extends AppConfig {
  override def getHomeUrl: String = System.getenv("HOME_URL")

  override def getAuthenticationToken: String = System.getenv("AUTH_TOKEN")
}