package snake

import com.purplekingdomgames.indigo.Indigo
import com.purplekingdomgames.indigoexts.grid.GridSize
import com.purplekingdomgames.shared.{ClearColor, GameConfig, GameViewport}

import scala.scalajs.js.annotation.JSExportTopLevel

object SnakeGame {

  private val gridSize: GridSize = GridSize(
    columns = 40,
    rows = 28,
    gridSquareSize = 16
  )

  private val magnificationLevel: Int = 1
  private val viewportWidth: Int = (gridSize.gridSquareSize * gridSize.columns) * magnificationLevel
  private val viewportHeight: Int = (gridSize.gridSquareSize * gridSize.rows) * magnificationLevel

  val config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor.Black,
    magnification = magnificationLevel
  )

  @JSExportTopLevel("Snake.main")
  def main(args: Array[String]): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(SnakeAssets.assets)
      .withFonts(Set(SnakeAssets.fontInfo))
      .withAnimations(Set())
      .startUpGameWith(SnakeInit.initialise(config.viewport, gridSize))
      .usingInitialModel(SnakeModel.initialModel)
      .updateModelUsing(SnakeModel.modelUpdate)
      .presentUsing(SnakeView.viewUpdate)
      .start()

}