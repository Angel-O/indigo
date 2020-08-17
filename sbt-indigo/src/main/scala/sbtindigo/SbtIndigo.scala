package sbtindigo

import sbt.plugins.JvmPlugin
import sbt._

object SbtIndigo extends sbt.AutoPlugin {

  override def requires: JvmPlugin.type = plugins.JvmPlugin
  override def trigger: PluginTrigger   = allRequirements

  object autoImport {
    val indigoBuild: TaskKey[Unit]     = taskKey[Unit]("Build an Indigo game.")
    val indigoBuildFull: TaskKey[Unit] = taskKey[Unit]("Publish an Indigo game.")
    val gameAssetsDirectory: SettingKey[String] =
      settingKey[String]("Project relative path to a directory that contains all of the assets the game needs to load.")
    val showCursor: SettingKey[Boolean] = settingKey[Boolean]("Show the cursor? True by default.")
    val title: SettingKey[String]       = settingKey[String]("Title of your game. Defaults to 'Made with Indigo'.")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    indigoBuild := indigoBuildTask.value,
    indigoBuildFull := indigoBuildFullTask.value,
    showCursor := true,
    title := "Made with Indigo",
    gameAssetsDirectory := "."
  )

  lazy val indigoBuildTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String      = Keys.baseDirectory.value.getCanonicalPath
      val scalaVersion: String = Keys.scalaVersion.value
      val projectName: String  = Keys.projectID.value.name

      val scriptPathBase =
        s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName"

      println(scriptPathBase)

      IndigoBuild.build(
        baseDir,
        TemplateOptions(
          title = title.value,
          showCursor = showCursor.value,
          scriptPathBase = scriptPathBase,
          gameAssetsDirectoryPath =
            if (gameAssetsDirectory.value.startsWith("/"))
              gameAssetsDirectory.value
            else baseDir.replace("/.js", "") + "/" + gameAssetsDirectory.value
        ),
        "indigoBuild",
        false
      )
    }

  lazy val indigoBuildFullTask: Def.Initialize[Task[Unit]] =
    Def.task {
      val baseDir: String      = Keys.baseDirectory.value.getCanonicalPath
      val scalaVersion: String = Keys.scalaVersion.value
      val projectName: String  = Keys.projectID.value.name

      val scriptPathBase =
        s"$baseDir/target/scala-${scalaVersion.split('.').reverse.tail.reverse.mkString(".")}/$projectName"

      println(scriptPathBase)

      IndigoBuild.build(
        baseDir,
        TemplateOptions(
          title = title.value,
          showCursor = showCursor.value,
          scriptPathBase = scriptPathBase,
          gameAssetsDirectoryPath =
            if (gameAssetsDirectory.value.startsWith("/"))
              gameAssetsDirectory.value
            else baseDir + "/" + gameAssetsDirectory.value
        ),
        "indigoBuildFull",
        true
      )
    }
}
