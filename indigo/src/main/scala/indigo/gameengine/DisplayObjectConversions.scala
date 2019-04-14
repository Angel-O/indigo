package indigo.gameengine

import indigo.gameengine.assets.{AnimationsRegister, FontRegister}
import indigo.gameengine.scenegraph.datatypes._
import indigo.gameengine.scenegraph._
import indigo.gameengine.scenegraph.animation._
import indigo.renderer.SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
import indigo.renderer.{AssetMapping, DisplayObject, SpriteSheetFrame, Vector2}
import indigo.runtime.IndigoLogger
import indigo.runtime.metrics.Metrics
import indigo.time.GameTime

import scala.annotation.tailrec
import scala.collection.mutable

import indigo.shared.EqualTo._

object DisplayObjectConversions {

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val lookupTextureOffsetCache: mutable.Map[String, Vector2] = mutable.Map.empty[String, Vector2]
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val lookupAtlasNameCache: mutable.Map[String, String] = mutable.Map.empty[String, String]
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val lookupAtlasSizeCache: mutable.Map[String, Vector2] = mutable.Map.empty[String, Vector2]
  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val frameOffsetsCache: mutable.Map[String, SpriteSheetFrameCoordinateOffsets] =
    mutable.Map.empty[String, SpriteSheetFrameCoordinateOffsets]

  private val lookupTextureOffset: (AssetMapping, String) => Vector2 = (assetMapping, name) =>
    lookupTextureOffsetCache.getOrElseUpdate(
      name, {
        assetMapping.mappings
          .find(p => p._1 === name)
          .map(_._2.offset)
          .map(pt => Vector2(pt.x.toDouble, pt.y.toDouble))
          .getOrElse {
            IndigoLogger.info("Failed to find atlas offset for texture: " + name)
            Vector2.zero
          }
      }
    )

  private val lookupAtlasName: (AssetMapping, String) => String = (assetMapping, name) =>
    lookupAtlasNameCache.getOrElseUpdate(name, {
      assetMapping.mappings.find(p => p._1 === name).map(_._2.atlasName).getOrElse {
        IndigoLogger.info("Failed to find atlas name for texture: " + name)
        ""
      }
    })

  private val lookupAtlasSize: (AssetMapping, String) => Vector2 = (assetMapping, name) =>
    lookupAtlasSizeCache.getOrElseUpdate(
      name, {
        assetMapping.mappings.find(p => p._1 === name).map(_._2.atlasSize).getOrElse {
          IndigoLogger.info("Failed to find atlas size for texture: " + name)
          Vector2.one
        }
      }
    )

  def leafToDisplayObject(gameTime: GameTime, assetMapping: AssetMapping, metrics: Metrics): Renderable => List[DisplayObject] = {
    case leaf: Graphic =>
      List(
        DisplayObject(
          x = leaf.x,
          y = leaf.y,
          z = leaf.depth.zIndex,
          width = leaf.crop.size.x,
          height = leaf.crop.size.y,
          imageRef = lookupAtlasName(assetMapping, leaf.imageAssetRef),
          alpha = leaf.effects.alpha,
          tintR = leaf.effects.tint.r,
          tintG = leaf.effects.tint.g,
          tintB = leaf.effects.tint.b,
          flipHorizontal = leaf.effects.flip.horizontal,
          flipVertical = leaf.effects.flip.vertical,
          frame = frameOffsetsCache.getOrElseUpdate(
            s"${leaf.crop.hash}_${leaf.imageAssetRef}", {
              SpriteSheetFrame.calculateFrameOffset(
                imageSize = lookupAtlasSize(assetMapping, leaf.imageAssetRef),
                frameSize = Vector2(leaf.crop.size.x.toDouble, leaf.crop.size.y.toDouble),
                framePosition = Vector2(leaf.crop.position.x.toDouble, leaf.crop.position.y.toDouble),
                textureOffset = lookupTextureOffset(assetMapping, leaf.imageAssetRef)
              )
            }
          )
        )
      )

    case leaf: Sprite =>
      val animations: Option[Animation] =
        AnimationsRegister.fetchFromCache(gameTime, leaf.bindingKey, leaf.animationsKey, metrics)

      animations
        .map { anim =>
          List(
            DisplayObject(
              x = leaf.x,
              y = leaf.y,
              z = leaf.depth.zIndex,
              width = leaf.bounds.size.x,
              height = leaf.bounds.size.y,
              imageRef = lookupAtlasName(assetMapping, anim.imageAssetRef.ref),
              alpha = leaf.effects.alpha,
              tintR = leaf.effects.tint.r,
              tintG = leaf.effects.tint.g,
              tintB = leaf.effects.tint.b,
              flipHorizontal = leaf.effects.flip.horizontal,
              flipVertical = leaf.effects.flip.vertical,
              frame = frameOffsetsCache.getOrElseUpdate(
                anim.frameHash, {
                  SpriteSheetFrame.calculateFrameOffset(
                    imageSize = lookupAtlasSize(assetMapping, anim.imageAssetRef.ref),
                    frameSize = Vector2(anim.currentFrame.bounds.size.x.toDouble, anim.currentFrame.bounds.size.y.toDouble),
                    framePosition = Vector2(anim.currentFrame.bounds.position.x.toDouble, anim.currentFrame.bounds.position.y.toDouble),
                    textureOffset = lookupTextureOffset(assetMapping, anim.imageAssetRef.ref)
                  )
                }
              )
            )
          )
        }
        .getOrElse {
          IndigoLogger.errorOnce(s"Cannot render Sprite, missing Animations with key: ${leaf.animationsKey}")
          Nil
        }

    case leaf: Text =>
      val alignmentOffsetX: Rectangle => Int = lineBounds =>
        leaf.alignment match {
          case TextAlignment.Left => 0

          case TextAlignment.Center => -(lineBounds.size.x / 2)

          case TextAlignment.Right => -lineBounds.size.x
        }

      val converterFunc: (TextLine, Int, Int) => List[DisplayObject] =
        DisplayObjectConversions.textLineToDisplayObjects(leaf, assetMapping)

      leaf.lines
        .foldLeft(0 -> List[DisplayObject]()) { (acc, textLine) =>
          (acc._1 + textLine.lineBounds.height, acc._2 ++ converterFunc(textLine, alignmentOffsetX(textLine.lineBounds), acc._1))
        }
        ._2

  }

  def textLineToDisplayObjects(leaf: Text, assetMapping: AssetMapping): (TextLine, Int, Int) => List[DisplayObject] =
    (line, alignmentOffsetX, yOffset) =>
      FontRegister
        .findByFontKey(leaf.fontKey)
        .map { fontInfo =>
          zipWithCharDetails(line.text.toList, fontInfo).map {
            case (fontChar, xPosition) =>
              DisplayObject(
                x = leaf.position.x + xPosition + alignmentOffsetX,
                y = leaf.position.y + yOffset,
                z = leaf.depth.zIndex,
                width = fontChar.bounds.width,
                height = fontChar.bounds.height,
                imageRef = lookupAtlasName(assetMapping, fontInfo.fontSpriteSheet.imageAssetRef),
                alpha = leaf.effects.alpha,
                tintR = leaf.effects.tint.r,
                tintG = leaf.effects.tint.g,
                tintB = leaf.effects.tint.b,
                flipHorizontal = leaf.effects.flip.horizontal,
                flipVertical = leaf.effects.flip.vertical,
                frame = frameOffsetsCache.getOrElseUpdate(
                  fontChar.bounds.hash + "_" + fontInfo.fontSpriteSheet.imageAssetRef, {
                    SpriteSheetFrame.calculateFrameOffset(
                      imageSize = lookupAtlasSize(assetMapping, fontInfo.fontSpriteSheet.imageAssetRef),
                      frameSize = Vector2(fontChar.bounds.width.toDouble, fontChar.bounds.height.toDouble),
                      framePosition = Vector2(fontChar.bounds.x.toDouble, fontChar.bounds.y.toDouble),
                      textureOffset = lookupTextureOffset(assetMapping, fontInfo.fontSpriteSheet.imageAssetRef)
                    )
                  }
                )
              )
          }
        }
        .getOrElse {
          IndigoLogger.errorOnce(s"Cannot render Text, missing Font with key: ${leaf.fontKey}")
          Nil
        }

  private def zipWithCharDetails(charList: List[Char], fontInfo: FontInfo): List[(FontChar, Int)] = {
    @tailrec
    def rec(remaining: List[(Char, FontChar)], nextX: Int, acc: List[(FontChar, Int)]): List[(FontChar, Int)] =
      remaining match {
        case Nil     => acc
        case x :: xs => rec(xs, nextX + x._2.bounds.width, (x._2, nextX) :: acc)
      }

    rec(charList.map(c => (c, fontInfo.findByCharacter(c))), 0, Nil)
  }

}
