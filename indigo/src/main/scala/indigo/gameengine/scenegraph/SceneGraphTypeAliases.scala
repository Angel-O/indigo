package indigo.gameengine.scenegraph

import indigo.gameengine

trait SceneGraphTypeAliases {

  type SceneUpdateFragment = gameengine.scenegraph.SceneUpdateFragment
  val SceneUpdateFragment: gameengine.scenegraph.SceneUpdateFragment.type = gameengine.scenegraph.SceneUpdateFragment

  type SceneGraphNode = gameengine.scenegraph.SceneGraphNode
  val SceneGraphNode: gameengine.scenegraph.SceneGraphNode.type = gameengine.scenegraph.SceneGraphNode

  // Audio
  type SceneAudio = gameengine.scenegraph.SceneAudio
  val SceneAudio: gameengine.scenegraph.SceneAudio.type = gameengine.scenegraph.SceneAudio

  type Volume = gameengine.scenegraph.Volume
  val Volume: gameengine.scenegraph.Volume.type = gameengine.scenegraph.Volume

  type Track = gameengine.scenegraph.Track
  val Track: gameengine.scenegraph.Track.type = gameengine.scenegraph.Track

  type PlaybackPattern = gameengine.scenegraph.PlaybackPattern
  val PlaybackPattern: gameengine.scenegraph.PlaybackPattern.type = gameengine.scenegraph.PlaybackPattern

  type SceneAudioSource = gameengine.scenegraph.SceneAudioSource
  val SceneAudioSource: gameengine.scenegraph.SceneAudioSource.type = gameengine.scenegraph.SceneAudioSource

  // Animation
  type Animation = gameengine.scenegraph.animation.Animation
  val Animation: gameengine.scenegraph.animation.Animation.type = gameengine.scenegraph.animation.Animation

  type Cycle = gameengine.scenegraph.animation.Cycle
  val Cycle: gameengine.scenegraph.animation.Cycle.type = gameengine.scenegraph.animation.Cycle

  type CycleLabel = gameengine.scenegraph.animation.CycleLabel
  val CycleLabel: gameengine.scenegraph.animation.CycleLabel.type = gameengine.scenegraph.animation.CycleLabel

  type Frame = gameengine.scenegraph.animation.Frame
  val Frame: gameengine.scenegraph.animation.Frame.type = gameengine.scenegraph.animation.Frame

  type AnimationKey = gameengine.scenegraph.animation.AnimationKey
  val AnimationKey: gameengine.scenegraph.animation.AnimationKey.type = gameengine.scenegraph.animation.AnimationKey

  type AnimationAction = gameengine.scenegraph.animation.AnimationAction
  val AnimationAction: gameengine.scenegraph.animation.AnimationAction.type = gameengine.scenegraph.animation.AnimationAction

  // Primitives
  type Sprite = gameengine.scenegraph.Sprite
  val Sprite: gameengine.scenegraph.Sprite.type = gameengine.scenegraph.Sprite

  type Text = gameengine.scenegraph.Text
  val Text: gameengine.scenegraph.Text.type = gameengine.scenegraph.Text

  type Graphic = gameengine.scenegraph.Graphic
  val Graphic: gameengine.scenegraph.Graphic.type = gameengine.scenegraph.Graphic

  type Group = gameengine.scenegraph.Group
  val Group: gameengine.scenegraph.Group.type = gameengine.scenegraph.Group

}
