package indigoframework

import indigo._
import indigoexts.entry.Indigo
import indigo.shared.{GameDefinition}

import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

import indigo.shared.EqualTo._

@JSExportTopLevel("Indigo")
object Framework {

  val config: GameConfig =
    GameConfig.default

  def configAsync: Future[Option[GameConfig]] =
    GameConfigHelper.load

  val assets: Set[AssetType] =
    AssetsHelper.assets

  def assetsAsync: Future[Set[AssetType]] =
    AssetsHelper.assetsAsync

  val initialise: AssetCollection => Startup[StartupErrorReport, StartupData] = assetCollection =>
    assetCollection.texts
      .find(p => p.name === "indigoJson")
      .flatMap(json => GameDefinitionHelper.fromJson(json.contents)) match {
      case Some(gd) =>
        Startup.Success(StartupData(gd))

      case None =>
        Startup.Failure(StartupErrorReport("Game definition could not be loaded"))
    }

  val initialModel: StartupData => GameModel = startupData => GameModelHelper.initialModel(startupData)

  val updateModel: (GameTime, GameModel, Dice) => GlobalEvent => Outcome[GameModel] = (_, gameModel, _) => GameModelHelper.updateModel(gameModel)

  val initialViewModel: (StartupData, GameModel) => Unit = (_, _) => ()

  val updateViewModel: (GameTime, GameModel, Unit, FrameInputEvents, Dice) => Outcome[Unit] = (_, _, _, _, _) => Outcome(())

  val updateView: (GameTime, GameModel, Unit, FrameInputEvents) => SceneUpdateFragment = (_, gameModel, _, _) => GameViewHelper.updateView(gameModel)

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  @JSExport
  def startLocal(): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .noFonts
      .noAnimations
      .startUpGameWith(initialise)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .initialiseViewModelUsing(initialViewModel)
      .updateViewModelUsing(updateViewModel)
      .presentUsing(updateView)
      .start()

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  @JSExport
  def startRemote(): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .noFonts
      .noAnimations
      .startUpGameWith(initialise)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .initialiseViewModelUsing(initialViewModel)
      .updateViewModelUsing(updateViewModel)
      .presentUsing(updateView)
      .start()

}

final case class StartupErrorReport(message: String)
object StartupErrorReport {
  implicit val toErrorReport: ToReportable[StartupErrorReport] =
    ToReportable.createToReportable(r => r.message)
}

final case class StartupData(gameDefinition: GameDefinition)
