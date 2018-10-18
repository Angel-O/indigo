package indigo.gameengine

import indigo.gameengine.assets.AnimationsRegister
import indigo.gameengine.audio.IAudioPlayer
import indigo.gameengine.events._
import indigo.gameengine.scenegraph.datatypes.AmbientLight
import indigo.gameengine.scenegraph.{SceneAudio, SceneGraphRootNode, SceneGraphRootNodeFlat, SceneUpdateFragment}
import indigo.renderer.{AssetMapping, DisplayLayer, Displayable, IRenderer}
import indigo.runtime.IIO
import indigo.runtime.metrics._
import indigo.shared.GameConfig
import org.scalajs.dom

class GameLoop[GameModel, ViewModel](
    gameConfig: GameConfig,
    assetMapping: AssetMapping,
    renderer: IRenderer,
    audioPlayer: IAudioPlayer,
    initialModel: GameModel,
    updateModel: (GameTime, GameModel) => GameEvent => GameModel,
    initialViewModel: ViewModel,
    updateViewModel: (GameTime, GameModel, ViewModel, FrameInputEvents) => ViewModel,
    updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment
)(implicit metrics: IMetrics) {

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var gameModelState: Option[GameModel] = None
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var viewModelState: Option[ViewModel] = None

  def loop(lastUpdateTime: Double): Double => Int = { time =>
    val timeDelta = time - lastUpdateTime

    // PUT NOTHING ABOVE THIS LINE!! Major performance penalties!!
    if (timeDelta > gameConfig.frameRateDeltaMillis) {

      metrics.record(FrameStartMetric)

      // Model updates cut off
      if (gameConfig.advanced.disableSkipModelUpdates || timeDelta < gameConfig.haltModelUpdatesAt) {

        metrics.record(UpdateStartMetric)

        val gameTime: GameTime = GameTime(time, timeDelta, gameConfig.frameRateDeltaMillis.toDouble)

        val collectedEvents: List[GameEvent]   = GlobalEventStream.collect
        val frameInputEvents: FrameInputEvents = events.FrameInputEvents(collectedEvents.filter(_.isGameEvent))

        GlobalSignalsManager.update(collectedEvents)

        metrics.record(CallUpdateGameModelStartMetric)

        val model: GameModel = gameModelState match {
          case None =>
            initialModel

          case Some(previousModel) =>
            GameLoop.processModelUpdateEvents(gameTime, previousModel, collectedEvents, updateModel)
        }

        gameModelState = Some(model)

        metrics.record(CallUpdateGameModelEndMetric)
        metrics.record(CallUpdateViewModelStartMetric)

        val viewModel: ViewModel = viewModelState match {
          case None =>
            initialViewModel

          case Some(previousModel) =>
            updateViewModel(gameTime, model, previousModel, frameInputEvents)
        }

        viewModelState = Some(viewModel)

        metrics.record(CallUpdateViewModelEndMetric)
        metrics.record(UpdateEndMetric)

        // View updates cut off
        if (gameConfig.advanced.disableSkipViewUpdates || timeDelta < gameConfig.haltViewUpdatesAt) {

          val x = for {
            view          <- GameLoop.updateGameView(updateView, gameTime, model, viewModel, frameInputEvents)
            processedView <- GameLoop.processUpdatedView(view, audioPlayer, collectedEvents)
            displayable   <- GameLoop.viewToDisplayable(gameTime, processedView, assetMapping, view.ambientLight)
            _             <- GameLoop.persistAnimationStates()
            _             <- GameLoop.drawScene(renderer, displayable)
            _             <- GameLoop.playAudio(audioPlayer, view.audio)
          } yield ()

          x.unsafeRun()

        } else {
          metrics.record(SkippedViewUpdateMetric)
        }

      } else {
        metrics.record(SkippedModelUpdateMetric)
      }

      metrics.record(FrameEndMetric)

      dom.window.requestAnimationFrame(loop(time))
    } else {
      dom.window.requestAnimationFrame(loop(lastUpdateTime))
    }
  }

}

object GameLoop {

  def updateGameView[GameModel, ViewModel](
      updateView: (GameTime, GameModel, ViewModel, FrameInputEvents) => SceneUpdateFragment,
      gameTime: GameTime,
      model: GameModel,
      viewModel: ViewModel,
      frameInputEvents: FrameInputEvents
  )(implicit metrics: IMetrics): IIO[SceneUpdateFragment] =
    IIO.delay {
      metrics.record(CallUpdateViewStartMetric)

      val view: SceneUpdateFragment = updateView(
        gameTime,
        model,
        viewModel,
        frameInputEvents
      )

      metrics.record(CallUpdateViewEndMetric)

      view
    }

  def processUpdatedView(view: SceneUpdateFragment, audioPlayer: IAudioPlayer, collectedEvents: List[GameEvent])(
      implicit metrics: IMetrics
  ): IIO[SceneGraphRootNodeFlat] =
    IIO.delay {
      metrics.record(ProcessViewStartMetric)

      val processUpdatedView: SceneUpdateFragment => SceneGraphRootNodeFlat =
        GameLoop.persistGlobalViewEvents(audioPlayer)(metrics) andThen
          GameLoop.flattenNodes andThen
          GameLoop.persistNodeViewEvents(metrics)(collectedEvents)

      val processedView: SceneGraphRootNodeFlat = processUpdatedView(view)

      metrics.record(ProcessViewEndMetric)

      processedView
    }

  def viewToDisplayable(gameTime: GameTime, processedView: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight)(implicit metrics: IMetrics): IIO[Displayable] =
    IIO.delay {
      metrics.record(ToDisplayableStartMetric)

      val displayable: Displayable =
        GameLoop.convertSceneGraphToDisplayable(gameTime, processedView, assetMapping, ambientLight)

      metrics.record(ToDisplayableEndMetric)

      displayable
    }

  def persistAnimationStates()(implicit metrics: IMetrics): IIO[Unit] =
    IIO.delay {
      metrics.record(PersistAnimationStatesStartMetric)

      AnimationsRegister.persistAnimationStates()

      metrics.record(PersistAnimationStatesEndMetric)
    }

  def processModelUpdateEvents[GameModel](gameTime: GameTime, previousModel: GameModel, remaining: List[GameEvent], updateModel: (GameTime, GameModel) => GameEvent => GameModel): GameModel =
    remaining match {
      case Nil =>
        updateModel(gameTime, previousModel)(FrameTick)

      case x :: xs =>
        processModelUpdateEvents(gameTime, updateModel(gameTime, previousModel)(x), xs, updateModel)
    }

  val persistGlobalViewEvents: IAudioPlayer => IMetrics => SceneUpdateFragment => SceneGraphRootNode = audioPlayer =>
    metrics =>
      update => {
        metrics.record(PersistGlobalViewEventsStartMetric)
        update.viewEvents.foreach(e => GlobalEventStream.pushViewEvent(audioPlayer, e))
        metrics.record(PersistGlobalViewEventsEndMetric)
        SceneGraphRootNode.fromFragment(update)
  }

  val flattenNodes: SceneGraphRootNode => SceneGraphRootNodeFlat = root => root.flatten

  val persistNodeViewEvents: IMetrics => List[GameEvent] => SceneGraphRootNodeFlat => SceneGraphRootNodeFlat = metrics =>
    gameEvents =>
      rootNode => {
        metrics.record(PersistNodeViewEventsStartMetric)
        rootNode.collectViewEvents(gameEvents).foreach(GlobalEventStream.pushGameEvent)
        metrics.record(PersistNodeViewEventsEndMetric)
        rootNode
  }

  def convertSceneGraphToDisplayable(gameTime: GameTime, rootNode: SceneGraphRootNodeFlat, assetMapping: AssetMapping, ambientLight: AmbientLight)(implicit metrics: IMetrics): Displayable =
    Displayable(
      DisplayLayer(
        rootNode.game.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping))
      ),
      DisplayLayer(
        rootNode.lighting.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping))
      ),
      DisplayLayer(
        rootNode.ui.nodes.flatMap(DisplayObjectConversions.leafToDisplayObject(gameTime, assetMapping))
      ),
      ambientLight
    )

  def drawScene(renderer: IRenderer, displayable: Displayable)(implicit metrics: IMetrics): IIO[Unit] =
    IIO.delay {
      metrics.record(RenderStartMetric)

      renderer.drawScene(displayable)

      metrics.record(RenderEndMetric)
    }

  def playAudio(audioPlayer: IAudioPlayer, sceneAudio: SceneAudio)(implicit metrics: IMetrics): IIO[Unit] =
    IIO.delay {
      metrics.record(AudioStartMetric)

      audioPlayer.playAudio(sceneAudio)

      metrics.record(AudioEndMetric)
    }

}
