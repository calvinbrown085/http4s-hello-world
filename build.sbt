

val Http4sVersion = "0.18.2"
val Specs2Version = "4.0.3"
val LogbackVersion = "1.2.3"


lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin)
  .settings(
    organization := "mustang0168",
    name := "http4s-hello-world",
    scalaVersion := "2.12.4",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.specs2"     %% "specs2-core"          % Specs2Version % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
    )
  )


  dockerfile in docker := {
    val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
    val classpath = (managedClasspath in Compile).value
    val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
    val jarTarget = s"/app/${jarFile.getName}"
    // Make a colon separated classpath with the JAR file
    val classpathString = classpath.files.map("/app/" + _.getName)
      .mkString(":") + ":" + jarTarget

    new Dockerfile {
      // Base image
      from("openjdk:8-jre")
      // Add all files on the classpath
      add(classpath.files, "/app/")
      // Add the JAR file
      add(jarFile, jarTarget)
      // On launch run Java with the classpath and the main class
      entryPoint("java", "-cp", classpathString, mainclass)
    }
  }

  imageNames in docker := Seq(
  // Sets the latest tag

  // Sets a name with a tag that contains the project version
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some(version.value)
  )
)
