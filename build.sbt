enablePlugins(AwsLambdaPlugin)

name := "smartii-home-alexa"

version := "1.0.0"

scalaVersion := "2.11.8"

//val alexaSmarthomeModel = ProjectRef(uri("git://github.com/jimnybob/alexa-smarthome-model.git"), "alexa-smarthome-model")

//lazy val root = (project in file(".")).aggregate(alexaSmarthomeModel).dependsOn(alexaSmarthomeModel)

resolvers += "Snowplow Repo" at "http://maven.snplow.com/releases/"

resolvers += "JFrog" at "https://dl.bintray.com/zzztimbo/maven/"

val slickVersion = "3.1.1"

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

  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "org.slf4j" % "slf4j-nop" % "1.7.19",
  "com.h2database" % "h2" % "1.4.191",
  "mysql" % "mysql-connector-java" % "5.1.29",

  "org.scalatest"                %% "scalatest" % "3.0.1" % Test,
  "org.scalamock"                %% "scalamock-scalatest-support" % "3.4.2" % Test,
  "com.typesafe.slick"           %% "slick-testkit" % slickVersion % Test
)

lambdaName := Some("smartii-home")
handlerName := Some("uk.co.smartii.alexa.SmartiiAlexaHandler")
region := Some("eu-west-1")

// Exclude commons-logging because it conflicts with the jcl-over-slf4j
libraryDependencies ~= { _ map {
  case m => m.exclude("commons-logging", "commons-logging")//.exclude("org.apache.logging.log4j", "log4j-api")
}}

// Take the first ServerWithStop because it's packaged into two jars
assemblyMergeStrategy in assembly := {
  case PathList("play", "core", "server", "ServerWithStop.class") => MergeStrategy.first
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case other => (assemblyMergeStrategy in assembly).value(other)
}

unmanagedResourceDirectories in Compile += (baseDirectory.value / "src/main/sql")

unmanagedResourceDirectories in Test += (baseDirectory.value / "src/test/sql")

//unmanagedSourceDirectories in Compile += (sourceManaged.value / "slick")

slick <<= slickCodeGenTask // register manual sbt command

sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use


// code generation task
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceDirectory, sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (srcDir, dir, cp, r, s) =>
  val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
val url = s"jdbc:h2:mem:test;INIT=runscript from '${srcDir.getPath}/main/sql/create.sql'" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
val jdbcDriver = "org.h2.Driver"
  val slickDriver = "slick.driver.H2Driver"
  val pkg = "uk.co.smartii.alexa.model"
  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg), s.log))
  val fname = outputDir + "/uk/co/smartii/alexa/model/Tables.scala"
  Seq(file(fname))
}

parallelExecution in Test := false