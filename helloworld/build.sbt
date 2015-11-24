name := "helloworld"

organization := "com.github.dnvriend"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.4.0"
  val activitiVersion = "5.19.0"
  val activemqVersion = "5.10.0"
  val camelVersion = "2.13.1"
  Seq(
    "org.activiti" % "activiti-engine" % activitiVersion withSources() withJavadoc(),
    "org.activiti" % "activiti-camel" % activitiVersion withSources() withJavadoc(),
    "org.apache.activemq" % "activemq-camel" % activemqVersion,
    "org.apache.activemq" % "activemq-pool" % activemqVersion,
    "org.json4s" %% "json4s-native" % "3.3.0",
    "org.postgresql" % "postgresql" % "9.4-1205-jdbc42",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.h2database" % "h2" % "1.4.190" % "test",
    "org.codehaus.groovy" % "groovy-all" % "2.2.0" % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.apache.camel" % "camel-test" % camelVersion % "test"
  )
}

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

parallelExecution := false

fork in Test := true

// enable scala code formatting //
import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
  "scala" -> Apache2_0("2015", "Dennis Vriend"),
  "conf" -> Apache2_0("2015", "Dennis Vriend", "#")
)

//enable sbt-dependency-graph
net.virtualvoid.sbt.graph.Plugin.graphSettings

enablePlugins(AutomateHeaderPlugin)

enablePlugins(WarPlugin)