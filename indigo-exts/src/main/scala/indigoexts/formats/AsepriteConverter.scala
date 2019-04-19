package indigoexts.formats

import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.animation._
import indigo.gameengine.scenegraph.datatypes._
import indigo.shared.IndigoLogger
import indigo.collections.NonEmptyList
import indigo.shared.AsString
import indigo.shared.formats.{Aseprite, AsepriteFrameTag, AsepriteFrame}

final case class SpriteAndAnimations(sprite: Sprite, animations: Animation)
object AsepriteConverter {

  @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
  def toSpriteAndAnimations(aseprite: Aseprite, depth: Depth, imageAssetRef: String): Option[SpriteAndAnimations] =
    extractCycles(aseprite) match {
      case Nil =>
        IndigoLogger.info("No animation frames found in Aseprite")
        None
      case x :: xs =>
        val animations: Animation =
          Animation(
            animationsKey = AnimationKey(BindingKey.generate.value),
            imageAssetRef = ImageAssetRef(imageAssetRef),
            spriteSheetSize = Point(aseprite.meta.size.w, aseprite.meta.size.h),
            currentCycleLabel = x.label,
            cycles = NonEmptyList.pure(x, xs),
            actions = Nil
          )
        Option(
          SpriteAndAnimations(
            Sprite(
              bindingKey = BindingKey.generate,
              bounds = Rectangle(
                position = Point(0, 0),
                size = Point(x.frames.head.bounds.size.x, x.frames.head.bounds.size.y)
              ),
              depth = depth,
              animationsKey = animations.animationsKey,
              ref = Point(0, 0),
              effects = Effects.default,
              eventHandler = (_: (Rectangle, GlobalEvent)) => None
            ),
            animations
          )
        )
    }

  def extractCycles(aseprite: Aseprite): List[Cycle] =
    aseprite.meta.frameTags
      .map { frameTag =>
        extractFrames(frameTag, aseprite.frames) match {
          case Nil =>
            IndigoLogger.info("Failed to extract cycle with frameTag: " + implicitly[AsString[AsepriteFrameTag]].show(frameTag))
            None
          case x :: xs =>
            Option(
              Cycle.create(frameTag.name, NonEmptyList.pure(x, xs))
            )
        }
      }
      .collect { case Some(s) => s }

  private def extractFrames(frameTag: AsepriteFrameTag, asepriteFrames: List[AsepriteFrame]): List[Frame] =
    asepriteFrames.slice(frameTag.from, frameTag.to + 1).map { aseFrame =>
      Frame(
        bounds = Rectangle(
          position = Point(aseFrame.frame.x, aseFrame.frame.y),
          size = Point(aseFrame.frame.w, aseFrame.frame.h)
        ),
        duration = aseFrame.duration
      )
    }

}
