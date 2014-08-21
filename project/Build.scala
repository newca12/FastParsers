import sbt._
import Keys._

object FastParsersBuild extends Build {

   def commonSettings = Seq(
	   scalaVersion := "2.11.2" ,
           scalacOptions := Seq("-optimize"),
	   libraryDependencies ++=  Seq(
		"org.scala-lang" % "scala-compiler"  % scalaVersion.value % "provided",
		"org.scala-lang" % "scala-reflect" % scalaVersion.value 
	)
   )
	
   lazy val FastParsersCore = Project(
	id = "Fastparsers-core",
	base = file("FastParsersCore"),
	settings = commonSettings
   )

   lazy val Examples = Project(
	id = "Examples",
	base = file("Examples"),	
	dependencies = Seq(FastParsersCore),
	settings = commonSettings ++ Seq (
	   // include the macro classes and resources in the main jar
	   mappings in (Compile, packageBin) ++= mappings.in(FastParsersCore, Compile, packageBin).value,
	   // include the macro sources in the main source jar
	   mappings in (Compile, packageSrc) ++= mappings.in(FastParsersCore, Compile, packageSrc).value
	)
   )
	
   lazy val FastParsersTest = Project(
	id = "Fastparsers-test",
	base = file("FastParsersTest"),
	dependencies = Seq(FastParsersCore),
	settings = commonSettings ++ Seq (
		resolvers ++= Seq(
		  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
		),
		//addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.98.0"),
			
		libraryDependencies += "org.scalatest"  % "scalatest_2.11"   % "2.2.1" % "test",
		
		libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
		
		libraryDependencies += "com.storm-enroute" % "scalameter_2.11" % "0.6" % "test"

		
		//testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
	)
   )

}
