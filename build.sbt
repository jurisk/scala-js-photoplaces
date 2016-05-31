lazy val buildSettings = Seq(
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8"),
  organization := "com.evolutiongaming",
  version := "1.0.0-SNAPSHOT"
)

lazy val strictCompilerSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-unchecked",
    "-Xfatal-warnings",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Xfuture"
  )
)

lazy val sourceSettings = buildSettings ++ strictCompilerSettings

lazy val circeVersion = "0.4.1"

lazy val root = (project in file("."))
  .aggregate(backend, frontend, sharedJVM, sharedJS)

lazy val backend = (project in file("backend"))
  .enablePlugins(PlayScala)
  .settings(
    name := "backend",
    scalaJSProjects := Seq(frontend),
    pipelineStages := Seq(scalaJSProd)

  )
  .settings(sourceSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "com.vmunier" %% "play-scalajs-scripts" % "0.5.0",
    ws,
    specs2 % Test
  ))
  .aggregate(frontend)
  .dependsOn(sharedJVM)

lazy val frontend = project.in(file("frontend"))
  .enablePlugins(ScalaJSPlugin, ScalaJSPlay)
  .settings(sourceSettings: _*)
  .settings(
    name := "frontend",
    test in Test := {}, // disable because of strange loadesTestFrameworks bug
    (emitSourceMaps in fullOptJS) := true,
    persistLauncher in Compile := true,
    persistLauncher in Test := false,
    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "0.11.1",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.1",
      "com.github.japgolly.scalacss" %%% "core" % "0.4.1",
      "com.github.japgolly.scalacss" %%% "ext-react" % "0.4.1",
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion
    ),
    jsDependencies ++= Seq(
      "org.webjars.bower" % "react" % "15.0.1" / "react-with-addons.js"
        minified "react-with-addons.min.js"
        commonJSName "React",

      "org.webjars.bower" % "react" % "15.0.1" / "react-dom.js"
        minified "react-dom.min.js"
        dependsOn "react-with-addons.js"
        commonJSName "ReactDOM"
    )
  )
  .dependsOn(sharedJS)

lazy val shared = (crossProject in file("shared"))
  .settings(sourceSettings: _*)
  .settings(
    name := "shared",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion
    ))
  .jsSettings(
    scalaJSStage in Test := FastOptStage
  )
  .jsConfigure(_ enablePlugins ScalaJSPlay)

lazy val sharedJVM = shared.jvm
lazy val sharedJS = shared.js

onLoad in Global := (Command.process("project backend", _: State)) compose (onLoad in Global).value
