package snake

import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.Graphic
import com.purplekingdomgames.indigo.gameengine.{Startup, ToReportable}

object SnakeInit {

  def initialise(gridSize: GridSize): AssetCollection => Startup[ErrorReport, SnakeStartupData] = _ => {
    val wall: Graphic[SnakeEvent] =
      Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture)

    SnakeStartupData(
      gridSize = gridSize,
      staticAssets = StaticAssets(
        wall = wall,
        apple = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(16, 0, 16, 16),
//        snakeHead = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(0, 16, 16, 16),
//        snakeBody = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(16, 16, 16, 16),
//        snakeTail = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(32, 16, 16, 16),
        snakeAlive = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(48, 48, 16, 16),
        snakeDead = Graphic(0, 0, 16, 16, 1, SnakeAssets.snakeTexture).withCrop(32, 48, 16, 16)
      )
    )
  }

}

case class SnakeStartupData(gridSize: GridSize, staticAssets: StaticAssets)

case class StaticAssets(wall: Graphic[SnakeEvent], apple: Graphic[SnakeEvent], snakeAlive: Graphic[SnakeEvent], snakeDead: Graphic[SnakeEvent])

case class ErrorReport(errors: List[String])
object ErrorReport {

  implicit val toErrorReport: ToReportable[ErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): ErrorReport = ErrorReport(message.toList)

}
