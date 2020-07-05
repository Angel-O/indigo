lazy val sbtIndigoVersion = SbtIndigoVersion.getVersion

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"      % "1.0.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"                   % "1.1.1")
addSbtPlugin("io.indigoengine" % "sbt-indigo" % sbtIndigoVersion)
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.9")
