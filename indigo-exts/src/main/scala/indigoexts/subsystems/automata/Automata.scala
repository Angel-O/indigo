package indigoexts.subsystems.automata

import indigo.shared.time.GameTime
import indigo.shared.time.Millis
import indigo.shared.Outcome
import indigo.shared.events.{FrameTick, GlobalEvent}
import indigo.shared.scenegraph._
import indigoexts.subsystems.SubSystem
import indigoexts.subsystems.automata.AutomataEvent._
import indigo.shared.dice.Dice
import indigoexts.subsystems.automata.Automata.Layer
import indigo.shared.EqualTo._

final class Automata(val poolKey: AutomataPoolKey, val automaton: Automaton, val layer: Layer, val paddock: List[SpawnedAutomaton]) extends SubSystem {
  type EventType = AutomataEvent

  def liveAutomataCount: Int =
    paddock.size

  val eventFilter: GlobalEvent => Option[AutomataEvent] = {
    case e: AutomataEvent =>
      Some(e)

    case FrameTick =>
      Some(AutomataEvent.Cull)

    case _ =>
      None
  }

  @SuppressWarnings(Array("org.wartremover.warts.OptionPartial"))
  def update(gameTime: GameTime, dice: Dice): AutomataEvent => Outcome[Automata] = {
    case Spawn(key, position, lifeSpan, payload) if key === poolKey =>
      val spawned =
        SpawnedAutomaton(
          automaton,
          AutomatonSeedValues(
            position,
            gameTime.running,
            lifeSpan.getOrElse(automaton.lifespan),
            Millis.zero,
            dice.roll,
            payload
          )
        )

      Outcome(new Automata(poolKey, automaton, layer, paddock :+ spawned))

    case KillAllInPool(key) if key === poolKey =>
      Outcome(Automata(poolKey, automaton, layer))

    case KillAll =>
      Outcome(Automata(poolKey, automaton, layer))

    case Cull =>
      val (l, r) =
        paddock.toList.partition(_.isAlive(gameTime.running))

      Outcome(new Automata(poolKey, automaton, layer, l.map(_.updateDelta(gameTime.delta))))
        .addGlobalEvents(r.toList.flatMap(sa => sa.automaton.onCull(sa.seedValues)))

    case _ =>
      Outcome(this)
  }

  def render(gameTime: GameTime): SceneUpdateFragment =
    Automata.render(this, gameTime)

  def report: String =
    "Automata farm"

}
object Automata {

  sealed trait Layer {
    def emptyScene(automatonUpdate: AutomatonUpdate): SceneUpdateFragment =
      this match {
        case Layer.Game =>
          SceneUpdateFragment.empty
            .addGameLayerNodes(automatonUpdate.nodes)
            .addGlobalEvents(automatonUpdate.events)

        case Layer.Lighting =>
          SceneUpdateFragment.empty
            .addLightingLayerNodes(automatonUpdate.nodes)
            .addGlobalEvents(automatonUpdate.events)

        case Layer.UI =>
          SceneUpdateFragment.empty
            .addUiLayerNodes(automatonUpdate.nodes)
            .addGlobalEvents(automatonUpdate.events)
      }

  }
  object Layer {
    case object Game     extends Layer
    case object Lighting extends Layer
    case object UI       extends Layer
  }

  def apply(poolKey: AutomataPoolKey, automaton: Automaton, layer: Layer): Automata =
    new Automata(poolKey, automaton, layer, Nil)

  def render(farm: Automata, gameTime: GameTime): SceneUpdateFragment =
    farm.layer.emptyScene(
      renderNoLayer(farm, gameTime).foldLeft(AutomatonUpdate.empty)(_ |+| _)
    )

  def renderNoLayer(farm: Automata, gameTime: GameTime): List[AutomatonUpdate] =
    farm.paddock.toList.map { sa =>
      sa.automaton.modifier(sa.seedValues, sa.automaton.sceneGraphNode).at(gameTime.running - sa.seedValues.createdAt)
    }
}
