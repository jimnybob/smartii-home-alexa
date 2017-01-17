package uk.co.smartii.alexa.model

import scala.util.{Failure, Properties, Success, Try}

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
  override def getHomeUrl: String = Try { System.getenv("HOME_URL") } match {
    case Failure(err) =>  throw error("HOME_URL")
    case Success(homeUrl) => homeUrl
  }

  override def getAuthenticationToken: String = Try { System.getenv("AUTH_TOKEN") } match {
    case Failure(err) =>  throw error("AUTH_TOKEN")
    case Success(authToken) => authToken
  }

  override def getDbUrl: String = Try { System.getenv("DB_URL") } match {
    case Failure(err) =>  throw error("DB_URL")
    case Success(dbUrl) => dbUrl
  }

  override def getDbUser: String = Try { System.getenv("DB_USER") } match {
    case Failure(err) =>  throw error("DB_USER")
    case Success(dbUser) => dbUser
  }

  override def getDbPassword: String = Try { System.getenv("DB_PASSWORD") } match {
    case Failure(err) =>  throw error("DB_PASSWORD")
    case Success(dbPword) => dbPword
  }

  private def error(variable: String) = new IllegalStateException(s"$variable environment variable has not been set")
}