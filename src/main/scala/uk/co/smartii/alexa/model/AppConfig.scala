package uk.co.smartii.alexa.model

import scala.util.Properties

/**
  * Created by jimbo on 08/01/17.
  */
trait AppConfig {
  def getHomeUrl: String
  def getAuthenticationToken: String
  def getDbUrl: String
  def getDbUser: String
  def getDbPassword: String
}

object RealAppConfig extends AppConfig {
  override def getHomeUrl: String = Properties.envOrElse("HOME_URL", throw error("HOME_URL"))

  override def getAuthenticationToken: String = Properties.envOrElse("AUTH_TOKEN", throw error("AUTH_TOKEN"))

  override def getDbUrl: String = Properties.envOrElse("DB_URL", throw error("DB_URL"))

  override def getDbUser: String = Properties.envOrElse("DB_USER", throw error("DB_USER"))

  override def getDbPassword: String = Properties.envOrElse("DB_PASSWORD", throw error("DB_PASSWORD"))

  private def error(variable: String) = new IllegalStateException(s"$variable environment variable has not been set")
}