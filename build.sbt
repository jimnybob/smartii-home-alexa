enablePlugins(AwsLambdaPlugin)

name := "smartii-home-alexa"

version := "1.0.0"

scalaVersion := "2.11.8"

//val alexaSmarthomeModel = ProjectRef(uri("git://github.com/jimnybob/alexa-smarthome-model.git"), "alexa-smarthome-model")

//lazy val root = (project in file(".")).aggregate(alexaSmarthomeModel).dependsOn(alexaSmarthomeModel)

libraryDependencies ++= Seq(
  "uk.co.smartii.alexa"          %% "alexa-smarthome-model" % "1.0.0-SNAPSHOT",
  "com.typesafe.play"            %% "play-ws" % "2.5.10",
  "com.typesafe.akka"            %% "akka-actor" % "2.4.12",
  "com.typesafe.akka"            %% "akka-stream" % "2.4.12",
  "com.amazon.alexa"             % "alexa-skills-kit" % "1.1.3",
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.7.5",
  "org.apache.logging.log4j"     % "log4j-core" % "2.6.2",
  "org.slf4j"                    % "slf4j-api"      % "1.7.21",
  "org.apache.commons"           % "commons-lang3" % "3.4",
  "commons-io"                   % "commons-io" % "2.5",
  "com.amazonaws"                % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws"                % "aws-java-sdk-dynamodb" % "1.11.31",
  "org.scalatest"                %% "scalatest" % "3.0.1" % Test
//  "org.scalamock"                %% "scalamock-scalatest-support" % "3.2.2" % Test
)

lambdaName := Some("smartii-home")
handlerName := Some("uk.co.smartii.alexa.SmartiiAlexaHandler")
region := Some("eu-west-1")

// Exclude commons-logging because it conflicts with the jcl-over-slf4j
libraryDependencies ~= { _ map {
  case m => m.exclude("commons-logging", "commons-logging")
}}

// Take the first ServerWithStop because it's packaged into two jars
assemblyMergeStrategy in assembly := {
  case PathList("play", "core", "server", "ServerWithStop.class") => MergeStrategy.first
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case other => (assemblyMergeStrategy in assembly).value(other)
}