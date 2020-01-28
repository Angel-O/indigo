package pirate

import indigo._
import indigoexts.subsystems.automata._
import indigoexts.geometry.Bezier
import indigoexts.geometry.Vertex

object CloudsAutomata {

  val signal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
    (seed, node) =>
      node match {
        case sprite: Sprite =>
          Bezier(Vertex.fromPoint(seed.spawnedAt), Vertex(-100, seed.spawnedAt.y.toDouble))
            .toSignal(seed.lifeSpan)
            .map { vertex =>
              sprite
                .moveTo(vertex.x.toInt, seed.spawnedAt.y)
                .withAnimationKey(AnimationKey("cloud " + ((seed.randomSeed % 3) + 1)))
                .play()
            }
            .map(s => AutomatonUpdate(List(s), Nil))

        case _ =>
          Signal.fixed(AutomatonUpdate.empty)
      }

  val automaton: Automaton =
    Automaton.create(
      Sprite(BindingKey("small clouds"), 0, 0, 140, 39, 45, Assets.Clouds.animationKey1),
      Millis.zero,
      signal,
      _ => Nil
    )

  val poolKey: AutomataPoolKey = AutomataPoolKey("cloud")

  val automata: Automata =
    Automata(
      poolKey,
      automaton,
      Automata.Layer.Game
    ).withMaxPoolSize(15)

}
