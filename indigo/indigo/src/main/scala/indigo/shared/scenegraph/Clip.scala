package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Vector2
import indigo.shared.materials.Material
import indigo.shared.materials.ShaderData
import indigo.shared.shader.ShaderPrimitive.float
import indigo.shared.shader.StandardShaders
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.time.Seconds

final case class Clip[M <: Material](
    size: Size,
    sheet: ClipSheet,
    playMode: ClipPlayMode,
    material: M,
    position: Point,
    rotation: Radians,
    scale: Vector2,
    depth: Depth,
    ref: Point,
    flip: Flip
) extends EntityNode
    with Cloneable
    with SpatialModifiers[Clip[M]]
    derives CanEqual:

  def bounds: Rectangle =
    BoundaryLocator.findBounds(this, position, size, ref)

  lazy val x: Int = position.x
  lazy val y: Int = position.y

  def withMaterial[MB <: Material](newMaterial: MB): Clip[MB] =
    this.copy(material = newMaterial)

  def modifyMaterial[MB <: Material](alter: M => MB): Clip[MB] =
    this.copy(material = alter(material))

  def withSize(newSize: Size): Clip[M] =
    this.copy(size = newSize)
  def withSize(width: Int, height: Int): Clip[M] =
    withSize(Size(width, height))

  def withSheet(newSheet: ClipSheet): Clip[M] =
    this.copy(sheet = newSheet)

  def withPlayMode(newPlayMode: ClipPlayMode): Clip[M] =
    this.copy(playMode = newPlayMode)

  def moveTo(pt: Point): Clip[M] =
    this.copy(position = pt)
  def moveTo(x: Int, y: Int): Clip[M] =
    moveTo(Point(x, y))
  def withPosition(newPosition: Point): Clip[M] =
    moveTo(newPosition)

  def moveBy(pt: Point): Clip[M] =
    this.copy(position = position + pt)
  def moveBy(x: Int, y: Int): Clip[M] =
    moveBy(Point(x, y))

  def rotateTo(angle: Radians): Clip[M] =
    this.copy(rotation = angle)
  def rotateBy(angle: Radians): Clip[M] =
    rotateTo(rotation + angle)
  def withRotation(newRotation: Radians): Clip[M] =
    rotateTo(newRotation)

  def scaleBy(amount: Vector2): Clip[M] =
    this.copy(scale = scale * amount)
  def scaleBy(x: Double, y: Double): Clip[M] =
    scaleBy(Vector2(x, y))
  def withScale(newScale: Vector2): Clip[M] =
    this.copy(scale = newScale)

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Clip[M] =
    this.copy(position = newPosition, rotation = newRotation, scale = newScale)

  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Clip[M] =
    transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

  def withDepth(newDepth: Depth): Clip[M] =
    this.copy(depth = newDepth)

  def flipHorizontal(isFlipped: Boolean): Clip[M] =
    this.copy(flip = flip.withHorizontalFlip(isFlipped))
  def flipVertical(isFlipped: Boolean): Clip[M] =
    this.copy(flip = flip.withVerticalFlip(isFlipped))
  def withFlip(newFlip: Flip): Clip[M] =
    this.copy(flip = newFlip)

  def withRef(newRef: Point): Clip[M] =
    this.copy(ref = newRef)
  def withRef(x: Int, y: Int): Clip[M] =
    withRef(Point(x, y))

  def toShaderData: ShaderData =
    val data = material.toShaderData
    data
      .withShaderId(StandardShaders.shaderIdToClipShaderId(data.shaderId))
      .addUniformBlock(
        UniformBlock(
          "IndigoClipData",
          List(
            Uniform("CLIP_FRAME_COUNT")    -> float(sheet.frameCount),
            Uniform("CLIP_FRAME_DURATION") -> float.fromSeconds(sheet.frameDuration),
            Uniform("CLIP_WRAP_AT")        -> float(sheet.wrapAt)
          )
        )
      )

object Clip:

  def apply[M <: Material](
      width: Int,
      height: Int,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = playMode,
      material = material,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      width: Int,
      height: Int,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = playMode,
      material = material,
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      x: Int,
      y: Int,
      width: Int,
      height: Int,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = Size(width, height),
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      position = Point(x, y),
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      size: Size,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = playMode,
      material = material,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      size: Size,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      position = Point.zero,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      position: Point,
      size: Size,
      sheet: ClipSheet,
      playMode: ClipPlayMode,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = playMode,
      material = material,
      position = position,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

  def apply[M <: Material](
      position: Point,
      size: Size,
      sheet: ClipSheet,
      material: M
  ): Clip[M] =
    Clip(
      size = size,
      sheet = sheet,
      playMode = ClipPlayMode.default,
      material = material,
      position = position,
      rotation = Radians.zero,
      scale = Vector2.one,
      depth = Depth.zero,
      ref = Point.zero,
      flip = Flip.default
    )

enum ClipSheetArrangement:
  case Horizontal, Vertical

object ClipSheetArrangement:
  val default: ClipSheetArrangement =
    ClipSheetArrangement.Horizontal

final case class ClipSheet(
    frameCount: Int,
    frameDuration: Seconds,
    wrapAt: Int,
    arrangement: ClipSheetArrangement,
    startOffset: Int
):
  def withFrameCount(newFrameCount: Int): ClipSheet =
    this.copy(frameCount = newFrameCount)

  def withFrameDuration(newFrameDuration: Seconds): ClipSheet =
    this.copy(frameDuration = newFrameDuration)

  def withWrapAt(newWrapAt: Int): ClipSheet =
    this.copy(wrapAt = newWrapAt)

  def withArrangement(newArrangement: ClipSheetArrangement): ClipSheet =
    this.copy(arrangement = newArrangement)

  def withStartOffset(newStartOffset: Int): ClipSheet =
    this.copy(startOffset = newStartOffset)

object ClipSheet:

  def apply(frameCount: Int, frameDuration: Seconds): ClipSheet =
    ClipSheet(frameCount, frameDuration, frameCount, ClipSheetArrangement.default, 0)

  def apply(frameCount: Int, frameDuration: Seconds, wrapAt: Int): ClipSheet =
    ClipSheet(frameCount, frameDuration, wrapAt, ClipSheetArrangement.default, 0)

  def apply(frameCount: Int, frameDuration: Seconds, wrapAt: Int, arrangement: ClipSheetArrangement): ClipSheet =
    ClipSheet(frameCount, frameDuration, wrapAt, arrangement, 0)

enum ClipPlayDirection:
  case Forward, Backward, PingPong

object ClipPlayDirection:
  val default: ClipPlayDirection =
    ClipPlayDirection.Forward

enum ClipPlayMode:
  val direction: ClipPlayDirection

  case Loop(direction: ClipPlayDirection) extends ClipPlayMode
  case PlayOnce(direction: ClipPlayDirection, startTime: Seconds) extends ClipPlayMode
  case PlayCount(direction: ClipPlayDirection, startTime: Seconds, times: Int) extends ClipPlayMode

object ClipPlayMode:
  val default: ClipPlayMode =
    ClipPlayMode.Loop(ClipPlayDirection.default)