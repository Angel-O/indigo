package indigo.gameengine.assets

import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.datatypes.BindingKey

import indigo.EqualTo._

final case class AnimationStates(states: List[AnimationMemento]) extends AnyVal {

  def findStateWithBindingKey(bindingKey: BindingKey): Option[AnimationMemento] =
    states.find(_.bindingKey.value === bindingKey.value)

}
