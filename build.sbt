import com.typesafe.sbt.packager.docker
import com.typesafe.sbt.packager.docker.ExecCmd
import scalariform.formatter.preferences._

name := "airlock"
version := "0.1.8"

scalaVersion := "2.12.8"

scalacOptions += "-unchecked"
scalacOptions += "-deprecation"
scalacOptions ++= Seq("-encoding", "utf-8")
scalacOptions += "-target:jvm-1.8"
scalacOptions += "-feature"
scalacOptions += "-Xlint"
scalacOptions += "-Xfatal-warnings"

// Experimental: improved update resolution.
updateOptions := updateOptions.value.withCachedResolution(cachedResoluton = true)

val akkaVersion       = "10.1.5"
val akkaStreamVersion = "2.5.19"
val logbackJson = "0.1.5"

libraryDependencies ++= Seq(
    "com.typesafe.scala-logging"   %% "scala-logging"          % "3.9.0",
    "ch.qos.logback"               %  "logback-classic"        % "1.2.3",
    "ch.qos.logback.contrib"       %  "logback-json-classic"   % logbackJson,
    "ch.qos.logback.contrib"       %  "logback-jackson"        % logbackJson,
    "com.fasterxml.jackson.core"   %  "jackson-databind"       % "2.9.8",
    "com.typesafe.akka"            %% "akka-slf4j"             % akkaStreamVersion,
    "com.typesafe.akka"            %% "akka-http"              % akkaVersion,
    "com.typesafe.akka"            %% "akka-stream"            % akkaStreamVersion,
    "com.typesafe.akka"            %% "akka-http-spray-json"   % akkaVersion,
    "com.typesafe.akka"            %% "akka-http-xml"          % akkaVersion,
    "com.amazonaws"                %  "aws-java-sdk-s3"        % "1.11.437",
    "org.apache.kafka"             %  "kafka-clients"           % "2.0.0",
    "net.manub"                    %% "scalatest-embedded-kafka" % "2.0.0" % IntegrationTest,
    "org.apache.ranger"            %  "ranger-plugins-common"  % "1.1.0" exclude("org.apache.kafka", "kafka_2.11"),
    "io.github.twonote"            %  "radosgw-admin4j"        % "1.0.2",
    "com.typesafe.akka"            %% "akka-http-testkit"      % akkaVersion       % Test,
    "org.scalatest"                %% "scalatest"              % "3.0.5"           % "it,test",
    "com.amazonaws"                %  "aws-java-sdk-sts"       % "1.11.437"        % IntegrationTest
)

// Fix logging dependencies:
//  - Our logging implementation is Logback, via the Slf4j API.
//  - Therefore we suppress the Log4j implentation and re-route its API calls over Slf4j.
libraryDependencies += "org.slf4j" % "log4j-over-slf4j" % "1.7.25" % Runtime
excludeDependencies += "org.slf4j" % "slf4j-log4j12"
excludeDependencies += "log4j" % "log4j"

configs(IntegrationTest)
Defaults.itSettings

parallelExecution in Test:= true
parallelExecution in IntegrationTest := true

enablePlugins(JavaAppPackaging)

fork := true

// Some default options at runtime: the G1 garbage collector, and headless mode.
javaOptions += "-XX:+UseG1GC"
javaOptions += "-Djava.awt.headless=true"

dockerExposedPorts := Seq(8080) // should match PROXY_PORT
dockerCommands     += ExecCmd("ENV", "PROXY_HOST", "0.0.0.0")
dockerBaseImage    := "openjdk:8u171-jre-slim-stretch"
dockerAlias        := docker.DockerAlias(Some("docker.io"),
                                         Some("wbaa"),
                                         "airlock",
                                         Option(System.getenv("DOCKER_TAG")))

scalariformPreferences := scalariformPreferences.value
    .setPreference(AlignSingleLineCaseStatements, true)
    .setPreference(DanglingCloseParenthesis, Preserve)
    .setPreference(DoubleIndentConstructorArguments, true)
    .setPreference(DoubleIndentMethodDeclaration, true)
    .setPreference(NewlineAtEndOfFile, true)
    .setPreference(SingleCasePatternOnNewline, false)

// hack for ranger conf dir - should contain files like ranger-s3-security.xml etc.
scriptClasspath in bashScriptDefines ~= (cp => cp :+ ":/etc/airlock")

//Coverage settings
Compile / coverageMinimum := 70
Compile / coverageFailOnMinimum := false
Compile / coverageHighlighting := true
Compile / coverageEnabled := true
