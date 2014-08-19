import sbt._
import Keys._

object MacroBuild extends Build {

   lazy val Examples = project.in(file("Examples")) dependsOn(FastParsers) settings(
	   // include the macro classes and resources in the main jar
	   scalaVersion := "2.11.2" ,
	   	
	   mappings in (Compile, packageBin) ++= mappings.in(FastParsers, Compile, packageBin).value,
	   // include the macro sources in the main source jar
	   mappings in (Compile, packageSrc) ++= mappings.in(FastParsers, Compile, packageSrc).value
	)
	
   lazy val FastParsers = project.in(file("FastParsers")) settings(
		scalaVersion := "2.11.2" ,
		
		resolvers ++= Seq(
		  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
		  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"/*,
		  Classpaths.sbtPluginReleases*/
		),
		//addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.98.0"),
			
		libraryDependencies += "org.scalatest"  % "scalatest_2.11"   % "2.2.1" % "test",
		
		libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
		
		libraryDependencies += "com.storm-enroute" % "scalameter_2.11" % "0.6" % "test",

		libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.2",
		
		libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.2",
		
		//testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
		
		scalacOptions ++= Seq("-optimize")
   )
}
