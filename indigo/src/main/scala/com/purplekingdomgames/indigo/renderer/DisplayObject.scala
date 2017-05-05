package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.AmbientLight
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}

case class Displayable(game: GameDisplayLayer, lighting: LightingDisplayLayer, ui: UiDisplayLayer)
case class GameDisplayLayer(displayObjects: List[DisplayObject]) extends DisplayLayer
case class LightingDisplayLayer(displayObjects: List[DisplayObject], ambientLight: AmbientLight) extends DisplayLayer
case class UiDisplayLayer(displayObjects: List[DisplayObject]) extends DisplayLayer
sealed trait DisplayLayer {
  val displayObjects: List[DisplayObject]
}

// TODO: Once batch rendering is in, almost all these fields can be private
// TODO: Some of this can definitely be optimised, if there's any benefit to that.
case class DisplayObject(x: Int,
                         y: Int,
                         z: Int,
                         width: Int,
                         height: Int,
                         imageRef: String,
                         alpha: Double,
                         tintR: Double,
                         tintG: Double,
                         tintB: Double,
                         flipHorizontal: Boolean,
                         flipVertical: Boolean,
                         frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
                        ) {

  def vertices(contextWidth: Int, contentHeight: Int, magnification: Int): scalajs.js.Array[Double] =
    DisplayObject.convertVertexCoordsToJsArray(
      DisplayObject.transformVertexCoords(
        DisplayObject.vertices,
        Matrix4
          .scale(width, height, 1)
          .translate(x, y, z)
          .orthographic(0, contextWidth / magnification, contentHeight / magnification, 0, -10000, 10000)
          .flip(flipHorizontal, flipVertical)
      )
    )

  def textureCoordinates: scalajs.js.Array[Double] =
    DisplayObject.convertTextureCoordsToJsArray(
      DisplayObject.transformTextureCoords(
        DisplayObject.textureCoordinates,
        frame.translate,
        frame.scale
      )
    )

  val effectValues: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha
  )
  val vertexCount: Int = DisplayObject.vertexCount
  val mode: Int = DisplayObject.mode

}

object DisplayObject {

  val vertices: List[Vector4] = List(
    Vector4.position(0, 0, 0),
    Vector4.position(0, 1, 0),
    Vector4.position(1, 0, 0),

    Vector4.position(0, 1, 0),
    Vector4.position(1, 0, 0),
    Vector4.position(1, 1, 0)
  )

  val vertexCount: Int = 6

  def transformVertexCoords(baseCoords: List[Vector4], matrix4: Matrix4): List[Vector4] = {
    baseCoords.map(_.applyMatrix4(matrix4))
  }

  def convertVertexCoordsToJsArray(coords: List[Vector4]): scalajs.js.Array[Double] =
    coords.map(_.toScalaJSArrayDouble).foldLeft(scalajs.js.Array[Double]())(_.concat(_))

  val textureCoordinates: List[Vector2] = List(
    Vector2(0, 0),
    Vector2(0, 1),
    Vector2(1, 0),

    Vector2(0, 1),
    Vector2(1, 0),
    Vector2(1, 1)
  )

  val mode: Int = TRIANGLES

  def transformTextureCoords(baseCoords: List[Vector2], translate: Vector2, scale: Vector2): List[Vector2] = {
    baseCoords.map(_.scale(scale).translate(translate))
  }

  def convertTextureCoordsToJsArray(coords: List[Vector2]): scalajs.js.Array[Double] =
    coords.map(_.toScalaJSArrayDouble).foldLeft(scalajs.js.Array[Double]())(_.concat(_))

}

case class RenderableThing(displayObject: DisplayObject, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer, textureBuffer: WebGLBuffer)

