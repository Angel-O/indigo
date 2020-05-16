package indigogame.entry

import utest._
import indigo.shared.time.GameTime
import indigo.shared.events.InputState
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister

object StandardFrameProcessorTests extends TestSuite {

  import TestFixtures._

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, new FontRegister)

  val tests: Tests =
    Tests {

      val outcome = standardFrameProcessor.run(
        model,
        viewModel,
        GameTime.zero,
        List(EventsOnlyEvent.Increment),
        InputState.default,
        Dice.loaded(0),
        boundaryLocator
      )

      val outModel     = outcome.state._1
      val outViewModel = outcome.state._2
      val outView      = outcome.state._3.get

      assert(
        outModel.count == 1,
        outViewModel == 10,
        outView.globalEvents.length == SceneUpdateFragment.empty.globalEvents.length,
        outcome.globalEvents.length == 2,
        outcome.globalEvents.contains(EventsOnlyEvent.Increment) == true,
        outcome.globalEvents.contains(EventsOnlyEvent.Total(1)) == true,
        outcome.globalEvents == List(EventsOnlyEvent.Total(1), EventsOnlyEvent.Increment)
      )

    }

}

object TestFixtures {

  val model =
    GameModel(0)

  val viewModel: Int =
    0

  val modelUpdate: (GameTime, GameModel, InputState, Dice) => GlobalEvent => Outcome[GameModel] =
    (_, m, _, _) => {
      case EventsOnlyEvent.Increment =>
        val newCount = m.count + 1
        Outcome(m.copy(count = newCount)).addGlobalEvents(EventsOnlyEvent.Total(newCount))

      case EventsOnlyEvent.Decrement =>
        Outcome(m)

      case _ =>
        Outcome(m)
    }

  val viewModelUpdate: (GameTime, GameModel, Int, InputState, Dice, BoundaryLocator) => Outcome[Int] =
    (_, _, vm, _, _, _) => {
      Outcome(vm + 10).addGlobalEvents(EventsOnlyEvent.Increment)
    }

  val viewUpdate: (GameTime, GameModel, Int, InputState, BoundaryLocator) => SceneUpdateFragment =
    (_, _, _, _, _) => SceneUpdateFragment.empty

  val standardFrameProcessor: StandardFrameProcessor[GameModel, Int] =
    new StandardFrameProcessor(modelUpdate, viewModelUpdate, viewUpdate)

  final case class GameModel(count: Int)

  sealed trait EventsOnlyEvent extends GlobalEvent
  object EventsOnlyEvent {
    case object Increment              extends EventsOnlyEvent
    case object Decrement              extends EventsOnlyEvent
    final case class Total(count: Int) extends EventsOnlyEvent
  }
}
