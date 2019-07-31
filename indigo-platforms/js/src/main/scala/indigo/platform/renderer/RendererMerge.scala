package indigo.platform.renderer

import indigo.shared.display.DisplayObject
import scala.scalajs.js.typedarray.Float32Array
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.metrics.Metrics
import indigo.facades.WebGL2RenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import indigo.platform.shaders.StandardMergeVert
import indigo.platform.shaders.StandardMergeFrag
import org.scalajs.dom.raw.WebGLTexture
import indigo.shared.ClearColor
import indigo.shared.display.SpriteSheetFrame
import scala.scalajs.js.JSConverters._
import indigo.shared.datatypes.Tint

class RendererMerge(gl2: WebGL2RenderingContext) {

  private def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
      rotation = 0,
      scaleX = 1,
      scaleY = 1,
      imageRef = "",
      alpha = 1,
      tintR = 1,
      tintG = 1,
      tintB = 1,
      tintA = 1,
      flipHorizontal = false,
      flipVertical = false,
      frame = SpriteSheetFrame.defaultOffset
    )

  private val mergeShaderProgram: WebGLProgram =
    RendererFunctions.shaderProgramSetup(gl2, "Merge", StandardMergeVert.shader, StandardMergeFrag.shader)

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  def drawLayer(
      gameFrameBuffer: FrameBufferComponents,
      lightingFrameBuffer: FrameBufferComponents,
      uiFrameBuffer: FrameBufferComponents,
      width: Int,
      height: Int,
      clearColor: ClearColor,
      gameLayerOverlay: Tint,
      lightingLayerOverlay: Tint,
      uiLayerOverlay: Tint,
      gameLayerTint: Tint,
      lightingLayerTint: Tint,
      uiLayerTint: Tint,
      gameLayerSaturation: Double,
      lightingLayerSaturation: Double,
      uiLayerSaturation: Double,
      metrics: Metrics
  ): Unit = {

    metrics.record(CurrentDrawLayer.Merge.metricStart)

    FrameBufferFunctions.switchToCanvas(gl2, clearColor)

    gl2.useProgram(mergeShaderProgram)

    RendererMerge.updateUBOData(
      screenDisplayObject(width, height),
      gameLayerOverlay,
      lightingLayerOverlay,
      uiLayerOverlay,
      gameLayerTint,
      lightingLayerTint,
      uiLayerTint,
      gameLayerSaturation,
      lightingLayerSaturation,
      uiLayerSaturation
    )

    // UBO data
    gl2.bindBuffer(ARRAY_BUFFER, displayObjectUBOBuffer)
    gl2.bindBufferRange(
      gl2.UNIFORM_BUFFER,
      0,
      displayObjectUBOBuffer,
      0,
      RendererMerge.uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl2.bufferData(
      ARRAY_BUFFER,
      new Float32Array(RendererFunctions.orthographicProjectionMatrixNoMag ++ RendererMerge.uboData),
      STATIC_DRAW
    )

    setupMergeFragmentShaderState(
      gameFrameBuffer.texture,
      lightingFrameBuffer.texture,
      uiFrameBuffer.texture
    )

    gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

    metrics.record(CurrentDrawLayer.Merge.metricDraw)

    metrics.record(CurrentDrawLayer.Merge.metricEnd)

  }

  def setupMergeFragmentShaderState(textureGame: WebGLTexture, textureLighting: WebGLTexture, textureUi: WebGLTexture): Unit = {
    val u_texture_game = gl2.getUniformLocation(mergeShaderProgram, "u_texture_game")
    gl2.uniform1i(u_texture_game, 1)
    gl2.activeTexture(TEXTURE1)
    gl2.bindTexture(TEXTURE_2D, textureGame)

    val u_texture_lighting = gl2.getUniformLocation(mergeShaderProgram, "u_texture_lighting")
    gl2.uniform1i(u_texture_lighting, 2)
    gl2.activeTexture(TEXTURE2)
    gl2.bindTexture(TEXTURE_2D, textureLighting)

    val u_texture_ui = gl2.getUniformLocation(mergeShaderProgram, "u_texture_ui")
    gl2.uniform1i(u_texture_ui, 3)
    gl2.activeTexture(TEXTURE3)
    gl2.bindTexture(TEXTURE_2D, textureUi)

    // Reset to TEXTURE0 before the next round of rendering happens.
    gl2.activeTexture(TEXTURE0)
  }

}

object RendererMerge {

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  val projectionMatrixUBODataSize: Int = 16
  val displayObjectUBODataSize: Int    = 16 * 3
  val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

  val uboData: scalajs.js.Array[Double] =
    List.fill(displayObjectUBODataSize)(0.0d).toJSArray

  def updateUBOData(
      displayObject: DisplayObject,
      gameLayerOverlay: Tint,
      lightingLayerOverlay: Tint,
      uiLayerOverlay: Tint,
      gameLayerTint: Tint,
      lightingLayerTint: Tint,
      uiLayerTint: Tint,
      gameLayerSaturation: Double,
      lightingLayerSaturation: Double,
      uiLayerSaturation: Double
  ): Unit = {
    uboData(0) = displayObject.x.toDouble
    uboData(1) = displayObject.y.toDouble
    uboData(2) = displayObject.width.toDouble * displayObject.scaleX
    uboData(3) = displayObject.height.toDouble * displayObject.scaleY

    uboData(4) = displayObject.frameX
    uboData(5) = displayObject.frameY
    uboData(6) = displayObject.frameScaleX
    uboData(7) = displayObject.frameScaleY

    uboData(8) = gameLayerOverlay.r
    uboData(9) = gameLayerOverlay.g
    uboData(10) = gameLayerOverlay.b
    uboData(11) = gameLayerOverlay.a

    uboData(12) = lightingLayerOverlay.r
    uboData(13) = lightingLayerOverlay.g
    uboData(14) = lightingLayerOverlay.b
    uboData(15) = lightingLayerOverlay.a

    uboData(16) = uiLayerOverlay.r
    uboData(17) = uiLayerOverlay.g
    uboData(18) = uiLayerOverlay.b
    uboData(19) = uiLayerOverlay.a

    uboData(20) = gameLayerTint.r
    uboData(21) = gameLayerTint.g
    uboData(22) = gameLayerTint.b
    uboData(23) = gameLayerTint.a

    uboData(24) = lightingLayerTint.r
    uboData(25) = lightingLayerTint.g
    uboData(26) = lightingLayerTint.b
    uboData(27) = lightingLayerTint.a

    uboData(28) = uiLayerTint.r
    uboData(29) = uiLayerTint.g
    uboData(30) = uiLayerTint.b
    uboData(31) = uiLayerTint.a

    uboData(32) = gameLayerSaturation
    uboData(33) = lightingLayerSaturation
    uboData(34) = uiLayerSaturation
    // uboData(35) = 0d
  }
}
