ThisBuild / tlBaseVersion := "0.0"
ThisBuild / developers := List(
  tlGitHubDev("rossabaker", "Ross A. Baker")
)

val Scala213 = "2.13.10"
ThisBuild / crossScalaVersions := Seq("2.12.17", Scala213, "3.2.1")
ThisBuild / scalaVersion := Scala213

// ensure missing timezones don't break tests on JS
ThisBuild / jsEnv := {
  import org.scalajs.jsenv.nodejs.NodeJSEnv
  new NodeJSEnv(NodeJSEnv.Config().withEnv(Map("TZ" -> "UTC")))
}

lazy val root = tlCrossRootProject.aggregate(scalaXml)

val http4sVersion = "0.23.16"
val scalaXmlVersion = "2.1.0"
val fs2DataVersion = "1.6.0"
val munitVersion = "1.0.0-M6"
val munitCatsEffectVersion = "2.0.0-M3"

lazy val scalaXml = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("scala-xml"))
  .settings(
    name := "http4s-scala-xml",
    description := "Provides scala-xml codecs for http4s",
    startYear := Some(2014),
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-core" % http4sVersion,
      "org.scala-lang.modules" %%% "scala-xml" % scalaXmlVersion,
      "org.gnieh" %%% "fs2-data-xml-scala" % fs2DataVersion,
      "org.scalameta" %%% "munit-scalacheck" % munitVersion % Test,
      "org.typelevel" %%% "munit-cats-effect" % munitCatsEffectVersion % Test,
      "org.http4s" %%% "http4s-laws" % http4sVersion % Test,
    ),
  )

lazy val docs = project
  .in(file("site"))
  .dependsOn(scalaXml.jvm)
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-dsl" % http4sVersion,
      "org.http4s" %%% "http4s-circe" % http4sVersion,
      "io.circe" %%% "circe-generic" % "0.14.1",
    )
  )
  .enablePlugins(Http4sOrgSitePlugin)
