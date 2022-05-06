name := "oerworldmap"

version := "0.1"

lazy val specs2core = "org.specs2" %% "specs2-core" % "2.4.14"

lazy val root = (project in file(".")).
  enablePlugins(PlayJava).
  configs(IntegrationTest).
  settings(Defaults.itSettings: _*).
  settings(
    libraryDependencies += specs2core % "it,test"
  )

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  cache,
  javaWs,
  filters,
  "commons-validator" % "commons-validator" % "1.5.1",
  "com.github.fge" % "jackson-coreutils" % "1.8",
  "com.github.fge" % "json-schema-validator" % "2.2.6",
  "org.apache.commons" % "commons-email" % "1.3.3",
  "commons-io" % "commons-io" % "2.5",
  "org.elasticsearch" % "elasticsearch" % "6.2.1",
  "org.elasticsearch.client" % "elasticsearch-rest-high-level-client" % "6.2.1",
  "org.apache.jena" % "apache-jena-libs" % "3.1.1",
  "com.github.jsonld-java" % "jsonld-java" % "0.12.3",
  "org.python" % "jython-standalone" % "2.7.1b2",
  "org.apache.httpcomponents" % "httpclient" % "4.5.5",
  "org.mnode.ical4j" % "ical4j" % "3.0.7"
)

javaOptions in Test += "-Dconfig.file=conf/test.conf"
javaOptions in Test += "-Xmx3G"
javaOptions in Test += "-Dlogback.configurationFile=conf/logback-test.xml"
javaOptions in Test += "-Duser.timezone=UTC"
