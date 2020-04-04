package indigo.platform.renderer

import indigo.shared.display.DisplayObject
import scala.scalajs.js.typedarray.Float32Array
import org.scalajs.dom.raw.WebGLProgram
import indigo.shared.metrics.Metrics
import indigo.facades.WebGL2RenderingContext
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.WebGLBuffer
import indigo.platform.shaders.StandardMerge
import org.scalajs.dom.raw.WebGLTexture
import indigo.shared.ClearColor
import scala.scalajs.js.JSConverters._
import indigo.shared.datatypes.RGBA

class RendererMerge(gl2: WebGL2RenderingContext) {

  private val mergeShaderProgram: WebGLProgram =
    RendererFunctions.shaderProgramSetup(gl2, "Merge", StandardMerge)

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  private val projectionMatrixUBODataSize: Int = 16
  private val displayObjectUBODataSize: Int    = 16 * 2
  private val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

  private val uboData: scalajs.js.Array[Double] =
    List.fill(displayObjectUBODataSize)(0.0d).toJSArray

  def updateUBOData(
      displayObject: DisplayObject,
      gameOverlay: RGBA,
      uiOverlay: RGBA,
      gameLayerTint: RGBA,
      lightingLayerTint: RGBA,
      uiLayerTint: RGBA,
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

    uboData(8) = gameOverlay.r
    uboData(9) = gameOverlay.g
    uboData(10) = gameOverlay.b
    uboData(11) = gameOverlay.a

    uboData(12) = uiOverlay.r
    uboData(13) = uiOverlay.g
    uboData(14) = uiOverlay.b
    uboData(15) = uiOverlay.a

    uboData(16) = gameLayerTint.r
    uboData(17) = gameLayerTint.g
    uboData(18) = gameLayerTint.b
    uboData(19) = gameLayerTint.a

    uboData(20) = lightingLayerTint.r
    uboData(21) = lightingLayerTint.g
    uboData(22) = lightingLayerTint.b
    uboData(23) = lightingLayerTint.a

    uboData(24) = uiLayerTint.r
    uboData(25) = uiLayerTint.g
    uboData(26) = uiLayerTint.b
    uboData(27) = uiLayerTint.a

    uboData(28) = gameLayerSaturation
    uboData(29) = lightingLayerSaturation
    uboData(30) = uiLayerSaturation
    // uboData(31) = 0d
  }

  @SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.Null"))
  def drawLayer(
      projection: scalajs.js.Array[Double],
      gameFrameBuffer: FrameBufferComponents.MultiOutput,
      lightsFrameBuffer: FrameBufferComponents.SingleOutput,
      lightingFrameBuffer: FrameBufferComponents.SingleOutput,
      uiFrameBuffer: FrameBufferComponents.SingleOutput,
      width: Int,
      height: Int,
      clearColor: ClearColor,
      gameOverlay: RGBA,
      uiOverlay: RGBA,
      gameLayerTint: RGBA,
      lightingLayerTint: RGBA,
      uiLayerTint: RGBA,
      gameLayerSaturation: Double,
      lightingLayerSaturation: Double,
      uiLayerSaturation: Double,
      metrics: Metrics
  ): Unit = {

    metrics.record(CurrentDrawLayer.Merge.metricStart)

    FrameBufferFunctions.switchToCanvas(gl2, clearColor)

    gl2.useProgram(mergeShaderProgram)

    updateUBOData(
      RendererHelper.screenDisplayObject(width, height),
      gameOverlay,
      uiOverlay,
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
      uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl2.bufferData(
      ARRAY_BUFFER,
      new Float32Array(projection ++ uboData),
      STATIC_DRAW
    )

    setupMergeFragmentShaderState(
      gameFrameBuffer,
      lightsFrameBuffer,
      lightingFrameBuffer,
      uiFrameBuffer
    )

    gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

    gl2.bindBuffer(gl2.UNIFORM_BUFFER, null);

    metrics.record(CurrentDrawLayer.Merge.metricDraw)

    metrics.record(CurrentDrawLayer.Merge.metricEnd)

  }

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var"))
  def setupMergeFragmentShaderState(
      game: FrameBufferComponents.MultiOutput,
      textureLights: FrameBufferComponents.SingleOutput,
      textureLighting: FrameBufferComponents.SingleOutput,
      textureUi: FrameBufferComponents.SingleOutput
  ): Unit = {

    val uniformTextures: List[(String, WebGLTexture)] =
      List(
        "u_texture_game_albedo"   -> game.albedo,
        "u_texture_game_emissive" -> game.emissive,
        "u_texture_lights"        -> textureLights.diffuse,
        "u_texture_lighting"      -> textureLighting.diffuse,
        "u_texture_ui"            -> textureUi.diffuse
      )

    var i: Int = 0

    while (i < uniformTextures.length) {
      val tex = uniformTextures(i)
      RendererHelper.attach(gl2, mergeShaderProgram, i + 1, tex._1, tex._2)
      i = i + 1
    }

    // Reset to TEXTURE0 before the next round of rendering happens.
    gl2.activeTexture(TEXTURE0)
  }

}
