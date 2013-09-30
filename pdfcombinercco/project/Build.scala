import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "PDFCombinerApp"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "commons-codec" % "commons-codec" % "1.7",
      "org.codehaus.jackson" % "jackson-core-asl" % "1.9.9",
      "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.9",
      "commons-io" % "commons-io" % "2.0.1",
      "com.ning" % "async-http-client" % "1.7.6",
      "org.apache.httpcomponents" % "httpcore" % "4.2",
      "org.apache.httpcomponents" % "httpmime" % "4.1.2",
      "net.sourceforge.dynamicreports" % "dynamicreports-core" % "3.1.2",
      "net.sourceforge.dynamicreports" % "dynamicreports-adhoc" % "3.1.2",
      "xalan" % "xalan" % "2.7.1",
      "org.jdom" % "jdom" % "2.0.2"
    )

    val pdfworker = Project("pdfworker", file("modules/pdfworker")).settings(libraryDependencies ++= appDependencies)

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here
      resolvers += "DevJava repo" at "http://download.java.net/maven/2/"
    ).dependsOn(pdfworker)
}
