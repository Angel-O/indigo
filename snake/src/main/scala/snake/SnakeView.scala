package snake

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

object SnakeView {

  def coordsToGridPoint(gridPoint: GridPoint, gridSize: GridSize): Point =
    Point(gridPoint.x * gridSize.gridSquareSize, ((gridSize.rows - 1) - gridPoint.y) * gridSize.gridSquareSize)

  def updateView(model: SnakeModel): SceneGraphUpdate[SnakeEvent] =
    SceneGraphUpdate(
      SceneGraphRootNode(
        game = gameLayer(model, if(model.running) model.staticAssets.snakeAlive else model.staticAssets.snakeDead),
        lighting = SceneGraphLightingLayer.empty,
        ui = SceneGraphUiLayer.empty
      ),
      Nil
    )

  def gameLayer(currentState: SnakeModel, snakeAsset: Graphic[SnakeEvent]): SceneGraphGameLayer[SnakeEvent] =
    SceneGraphGameLayer()
      .addChildren {
        currentState.gameMap.findApples.map(a => currentState.staticAssets.apple.moveTo(coordsToGridPoint(a.gridPoint, currentState.gameMap.gridSize)))
      }
      .addChildren(currentState.player1.snake.givePath.map(pt => snakeAsset.moveTo(coordsToGridPoint(pt, currentState.gameMap.gridSize))))
      .addChildren { //TODO: Could be statically pregenerated / loaded from Tiled map
        currentState.gameMap.findWalls.map(w => currentState.staticAssets.wall.moveTo(coordsToGridPoint(w.gridPoint, currentState.gameMap.gridSize)))
      }

}
