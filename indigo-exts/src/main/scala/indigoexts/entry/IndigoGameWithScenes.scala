package indigoexts.entry

import indigo._
import indigoexts.scenemanager.{SceneManager, SceneName, Scene}
import indigo.gameengine.GameEngine
import indigo.gameengine.StandardFrameProcessor
import indigoexts.subsystems.SubSystem
import indigoexts.subsystems.SubSystemsRegister

import scala.concurrent.Future

// Using Scala.js, so this is just to make the compiler happy.
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A trait representing a game with scene management baked in
  * @example `object MyGame extends IndigoGameWithScenes[MyStartupData, MyGameModel, MyViewModel]`
  * @tparam StartupData The class type representing your successful startup data
  * @tparam Model The class type representing your game's model
  * @tparam ViewModel The class type representing your game's view model
  */
trait IndigoGameWithScenes[StartupData, Model, ViewModel] {

  /**
    * A non-empty ordered list of scenes
    */
  val scenes: NonEmptyList[Scene[Model, ViewModel]]

  /**
    * Optional name of the first scene. If [[None]] is provided
    * then the first scene is the head of the scenes list.
    */
  val initialScene: Option[SceneName]

  /**
    * Fixed initial config.
    */
  val config: GameConfig

  /**
    * A [[Set]] of assets to be loaded.
    */
  val assets: Set[AssetType]

  /**
    * A [[Set]] of FontInfo's describing the fonts for your game.
    * Please note that more fonts can be added to the `Startup` object
    * resulting from the `setup` method below.
    */
  val fonts: Set[FontInfo]

  /**
    * A [[Set]] of initial, predefined animations for your game.
    * Please note that more animations can be added to the `Startup` object
    * resulting from the `setup` method below.
    */
  val animations: Set[Animation]

  /**
    * A [[Set]] of SubSystems for your game.
    */
  val subSystems: Set[SubSystem]

  /**
    * The `setup` function is your only opportunity to do an initial work
    * to set up your game. For example, perhaps one of your assets was a
    * JSON description of a map or an animation sequence, you could process
    * that now, which is why you have access to the `AssetCollection` object.
    * `setup` is also the only place the game is expected to to potentially
    * fail with error and report any errors.
    * @param assetCollection Access to the Asset collection in order to,
    *                        for example, parse text files.
    * @return Either an `Startup.Success[...your startup data...]` or a
    *         `Startup.Failure[StartupErrors]`.
    */
  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData]

  /**
    * Set up of your initial model
    * @param startupData Access to Startup data in case you need it for the model
    * @return An instance of your game model
    */
  def initialModel(startupData: StartupData): Model

  /**
    * Set up of your initial view model
    * @param startupData Access to Startup data in case you need it for the view model
    * @return An instance of your game's view model
    */
  def initialViewModel(startupData: StartupData): Model => ViewModel

  private def indigoGame: GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel] = {
    val sceneManager: SceneManager[Model, ViewModel] =
      initialScene match {
        case Some(name) =>
          SceneManager(scenes, name)

        case None =>
          SceneManager(scenes, scenes.head.name)
      }

    val frameProcessor: StandardFrameProcessor[GameWithSubSystems[Model], ViewModel] = {
      StandardFrameProcessor(
        GameWithSubSystems.update(sceneManager.updateModel),
        GameWithSubSystems.updateViewModel(sceneManager.updateViewModel),
        GameWithSubSystems.present(sceneManager.updateView)
      )
    }

    new GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel](
      config,
      Future(None),
      assets,
      Future(Set()),
      fonts,
      animations,
      (ac: AssetCollection) => setup(ac),
      (sd: StartupData) => GameWithSubSystems(initialModel(sd), SubSystemsRegister(subSystems.toList)),
      (sd: StartupData) => (m: GameWithSubSystems[Model]) => initialViewModel(sd)(m.model),
      frameProcessor
    )
  }

  def main(args: Array[String]): Unit =
    indigoGame.start()

}
