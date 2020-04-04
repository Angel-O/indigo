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
import indigo.shared.datatypes.RGBA
import indigo.shared.display.DisplayEffects

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
      diffuseRef = "",
      emissionRef = "",
      normalRef = "",
      specularRef = "",
      frame = SpriteSheetFrame.defaultOffset,
      refX = 0,
      refY = 0,
      effects = DisplayEffects.default
    )

  private val mergeShaderProgram: WebGLProgram =
    RendererFunctions.shaderProgramSetup(gl2, "Merge", StandardMergeVert.shader, StandardMergeFrag.shader)

  private val displayObjectUBOBuffer: WebGLBuffer =
    gl2.createBuffer()

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  def drawLayer(
      projection: scalajs.js.Array[Double],
      gameFrameBuffer: FrameBufferComponents,
      lightingFrameBuffer: FrameBufferComponents,
      uiFrameBuffer: FrameBufferComponents,
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

    RendererMerge.updateUBOData(
      screenDisplayObject(width, height),
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
      RendererMerge.uboDataSize * Float32Array.BYTES_PER_ELEMENT
    )
    gl2.bufferData(
      ARRAY_BUFFER,
      new Float32Array(projection ++ RendererMerge.uboData),
      STATIC_DRAW
    )

    setupMergeFragmentShaderState(
      gameFrameBuffer,
      lightingFrameBuffer.albedo,
      uiFrameBuffer.albedo
    )

    gl2.drawArrays(TRIANGLE_STRIP, 0, 4)

    metrics.record(CurrentDrawLayer.Merge.metricDraw)

    metrics.record(CurrentDrawLayer.Merge.metricEnd)

  }

  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def intToTextureLocation: Int => Int = {
    case 0  => TEXTURE0
    case 1  => TEXTURE1
    case 2  => TEXTURE2
    case 3  => TEXTURE3
    case 4  => TEXTURE4
    case 5  => TEXTURE5
    case 6  => TEXTURE6
    case 7  => TEXTURE7
    case 8  => TEXTURE8
    case 9  => TEXTURE9
    case 10 => TEXTURE10
    case 11 => TEXTURE11
    case 12 => TEXTURE12
    case 13 => TEXTURE13
    case 14 => TEXTURE14
    case 15 => TEXTURE15
    case 16 => TEXTURE16
    case 17 => TEXTURE17
    case 18 => TEXTURE18
    case 19 => TEXTURE19
    case 20 => TEXTURE20
    case 21 => TEXTURE21
    case 22 => TEXTURE22
    case 23 => TEXTURE23
    case 24 => TEXTURE24
    case 25 => TEXTURE25
    case 26 => TEXTURE26
    case 27 => TEXTURE27
    case 28 => TEXTURE28
    case 29 => TEXTURE29
    case 30 => TEXTURE30
    case 31 => TEXTURE31
    case _  => throw new Exception("Cannot assign > 32 texture locations.")
  }

  def attach(location: Int, uniformName: String, texture: WebGLTexture): Unit = {
    gl2.uniform1i(gl2.getUniformLocation(mergeShaderProgram, uniformName), location)
    gl2.activeTexture(intToTextureLocation(location))
    gl2.bindTexture(TEXTURE_2D, texture)
  }

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var"))
  def setupMergeFragmentShaderState(game: FrameBufferComponents, textureLighting: WebGLTexture, textureUi: WebGLTexture): Unit = {

    val uniformTextures: List[(String, WebGLTexture)] =
      List(
        "u_texture_game_albedo" -> game.albedo,
        "u_texture_game_emissive" -> game.emissive,
        "u_texture_game_normal" -> game.normal,
        "u_texture_game_specular" -> game.specular,
        "u_texture_lighting" -> textureLighting,
        "u_texture_ui" -> textureUi,
      )

    var i: Int = 0

    while(i < uniformTextures.length) {
      val tex = uniformTextures(i)
      attach(i + 1, tex._1, tex._2)
      i = i + 1
    }

    // Reset to TEXTURE0 before the next round of rendering happens.
    gl2.activeTexture(TEXTURE0)
  }

}

object RendererMerge {

  // They're all blocks of 16, it's the only block length allowed in WebGL.
  val projectionMatrixUBODataSize: Int = 16
  val displayObjectUBODataSize: Int    = 16 * 2
  val uboDataSize: Int                 = projectionMatrixUBODataSize + displayObjectUBODataSize

  val uboData: scalajs.js.Array[Double] =
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
}
