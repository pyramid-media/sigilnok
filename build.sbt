enablePlugins(ScalaJSPlugin)

name := "sigilnok"
organization := "media.pyramid"
version := "0.2.0"

scalaVersion := "2.13.5"

scalaJSUseMainModuleInitializer := true

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0"
libraryDependencies += "com.raquo" %%% "airstream" % "0.12.0"
libraryDependencies += "com.raquo" %%% "laminar" % "0.12.1"


