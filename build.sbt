name := "FastParsers"

scalaVersion := "2.11.2"

scalacOptions ++= Seq(
  "-optimize"
)

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
