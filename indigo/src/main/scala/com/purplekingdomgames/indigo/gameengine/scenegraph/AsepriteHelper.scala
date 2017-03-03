package com.purplekingdomgames.indigo.gameengine.scenegraph

import upickle.default._

case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta)

case class AsepriteFrame(filename: String,
                         frame: AsepriteRectangle,
                         rotated: Boolean,
                         trimmed: Boolean,
                         spriteSourceSize: AsepriteRectangle,
                         sourceSize: AsepriteSize,
                         duration: Int)

case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int)

case class AsepriteMeta(app: String,
                        version: String,
                        image: String,
                        format: String,
                        size: AsepriteSize,
                        scale: String,
                        frameTags: List[AsepriteFrameTag])

case class AsepriteSize(w: Int, h: Int)

case class AsepriteFrameTag(name: String, from: Int, to: Int, direction: String)

object AsepriteHelper {

  def fromJson(json: String): Option[Aseprite] = {
    try {
      Option(read[Aseprite](json))
    } catch {
      case e: Throwable =>
        println("Failed to deserialise json into Aseprite: " + e.getMessage)
        None
    }
  }

  private def extractFrames(frameTag: AsepriteFrameTag, asepriteFrames: List[AsepriteFrame]): List[Frame] = {
    asepriteFrames.slice(frameTag.from, frameTag.to + 1).map { aseFrame =>
      Frame(
        bounds = Rectangle(
          position = Point(aseFrame.frame.x, aseFrame.frame.y),
          size = Point(aseFrame.frame.w, aseFrame.frame.h)
        ),
        current = false
      )
    }
  }

  private def extractCycles(aseprite: Aseprite): List[Cycle] = {
    aseprite.meta.frameTags.map { frameTag =>
      extractFrames(frameTag, aseprite.frames) match {
        case Nil =>
          println("Failed to extract cycle with frameTag: " + frameTag)
          None
        case x :: xs =>
          Option(
            Cycle(
              label = frameTag.name,
              playheadPosition = 0,
              frame = x.copy(current = true),
              frames = xs,
              current = false
            )
          )
      }
    }.collect { case Some(s) => s }
  }

  def toSprite(aseprite: Aseprite, depth: Depth, imageAssetRef: String): Option[Sprite] = {
    extractCycles(aseprite) match {
      case Nil =>
        println("No animation frames found in Aseprit: " + aseprite)
        None
      case x :: xs =>
        val animations: Animations =
          Animations(
            spriteSheetSize = Point(aseprite.meta.size.w, aseprite.meta.size.h),
            cycle = x.copy(current = true),
            cycles = xs
          )
        Option(
          Sprite(
            bindingKey = BindingKey.generate,
            bounds = Rectangle(
              position = Point(0, 0),
              size = Point(x.frame.bounds.size.x, x.frame.bounds.size.y)
            ),
            depth = depth,
            imageAssetRef = imageAssetRef,
            animations = animations,
            ref = Point(0, 0),
            effects = Effects.default
          )
        )
    }
  }

}

/*
import upickle.default._

write(1)                          ==> "1"

write(Seq(1, 2, 3))               ==> "[1,2,3]"

read[Seq[Int]]("[1, 2, 3]")       ==> List(1, 2, 3)

write((1, "omg", true))           ==> """[1,"omg",true]"""

type Tup = (Int, String, Boolean)

read[Tup]("""[1, "omg", true]""") ==> (1, "omg", true)
 */
