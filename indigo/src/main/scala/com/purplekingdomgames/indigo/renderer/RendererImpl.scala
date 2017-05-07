package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.util.metrics._
import org.scalajs.dom.raw.WebGLBuffer
import org.scalajs.dom.raw.WebGLRenderingContext._

import scala.language.implicitConversions

trait IRenderer {
  def init(): Unit
  def drawScene(displayable: Displayable)(implicit metrics: IMetrics): Unit
}

final class RendererImpl(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], cNc: ContextAndCanvas) extends IRenderer {

  import RendererFunctions._

  private def screenDisplayObject(w: Int, h: Int): DisplayObject =
    DisplayObject(
      x = 0,
      y = 0,
      z = 1,
      width = w,
      height = h,
      imageRef = "",
      alpha = 1,
      tintR = 1,
      tintG = 1,
      tintB = 1,
      flipHorizontal = false,
      flipVertical = true,
      frame = SpriteSheetFrame.defaultOffset
    )

  private val textureLocations: List[TextureLookupResult] =
    loadedTextureAssets.map { li =>
      TextureLookupResult(li.name, organiseImage(cNc.context, li.data))
    }

  private val vertexBuffer: WebGLBuffer = createVertexBuffer(cNc.context)
  private val textureBuffer: WebGLBuffer = createVertexBuffer(cNc.context)
  private val effectsBuffer: WebGLBuffer = createVertexBuffer(cNc.context)

  private val shaderProgram = shaderProgramSetup(cNc.context)
  private val lightingShaderProgram = lightingShaderProgramSetup(cNc.context)
  private val mergeShaderProgram = mergeShaderProgramSetup(cNc.context)

  private val gameFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val lightingFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))
  private val uiFrameBuffer: FrameBufferComponents = FrameBufferFunctions.createFrameBuffer(cNc, FrameBufferFunctions.createAndSetupTexture(cNc))

  def init(): Unit = {
    cNc.context.disable(DEPTH_TEST)
    cNc.context.viewport(0, 0, cNc.width, cNc.height)
    cNc.context.blendFunc(ONE, ONE_MINUS_SRC_ALPHA)
    cNc.context.enable(BLEND)
  }

  def drawScene(displayable: Displayable)(implicit metrics: IMetrics): Unit = {

    val projectionMatrix: Matrix4 = resize(cNc.canvas, cNc.canvas.clientWidth, cNc.canvas.clientHeight, cNc.magnification)

    metrics.record(DrawGameLayerStartMetric)
    drawLayerToTexture(displayable.game, gameFrameBuffer, config.clearColor, projectionMatrix, drawBg = false)
    metrics.record(DrawGameLayerEndMetric)

    metrics.record(DrawLightingLayerStartMetric)
    drawLightingLayerToTexture(displayable.lighting, lightingFrameBuffer, displayable.lighting.ambientLight, projectionMatrix)
    metrics.record(DrawLightingLayerEndMetric)

    metrics.record(DrawUiLayerStartMetric)
    drawLayerToTexture(displayable.ui, uiFrameBuffer, ClearColor.Black.forceTransparent, projectionMatrix, drawBg = false)
    metrics.record(DrawUiLayerEndMetric)

    metrics.record(RenderToConvasStartMetric)
    renderToCanvas(screenDisplayObject(cNc.width, cNc.height), projectionMatrix)
    metrics.record(RenderToConvasEndMetric)
  }

  private def drawLightingLayerToTexture[B](displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor, projectionMatrix: Matrix4)(implicit metrics: IMetrics): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Use Program
    cNc.context.useProgram(lightingShaderProgram)

    // Draw as normal
    DisplayObject.sortAndCompress(projectionMatrix)(displayLayer.displayObjects).foreach { displayObject =>

      bindToBuffer(cNc.context, vertexBuffer, displayObject.vertices)
      bindToBuffer(cNc.context, textureBuffer, displayObject.textureCoordinates)
      bindToBuffer(cNc.context, effectsBuffer, displayObject.effectValues)

      // Setup attributes
      bindShaderToBuffer(cNc, lightingShaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>

        // Setup Uniforms
        setupLightingFragmentShader(cNc.context, lightingShaderProgram, textureLookup.texture, displayObject.imageRef)

        // Draw
        cNc.context.drawArrays(displayObject.mode, 0, displayObject.vertexCount)
        metrics.record(LightingDrawCallMetric)
      }

    }

  }

  private def drawLayerToTexture[B](displayLayer: DisplayLayer, frameBufferComponents: FrameBufferComponents, clearColor: ClearColor, projectionMatrix: Matrix4, drawBg: Boolean)(implicit metrics: IMetrics): Unit = {

    // Switch to the frameBuffer
    FrameBufferFunctions.switchToFramebuffer(cNc, frameBufferComponents.frameBuffer, clearColor)

    // Use Program
    cNc.context.useProgram(shaderProgram)

    // Draw as normal
    DisplayObject.sortAndCompress(projectionMatrix)(displayLayer.displayObjects).foreach { displayObject =>

      bindToBuffer(cNc.context, vertexBuffer, displayObject.vertices)
      bindToBuffer(cNc.context, textureBuffer, displayObject.textureCoordinates)
      bindToBuffer(cNc.context, effectsBuffer, displayObject.effectValues)

      // Setup attributes
      bindShaderToBuffer(cNc, shaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

      textureLocations.find(t => t.name == displayObject.imageRef).foreach { textureLookup =>

        // Setup Uniforms
        setupFragmentShader(cNc.context, shaderProgram, textureLookup.texture, displayObject.imageRef)

        // Draw
        cNc.context.drawArrays(displayObject.mode, 0, displayObject.vertexCount)
        metrics.record(NormalLayerDrawCallMetric)

      }
    }

  }

  private def renderToCanvas(displayObject: DisplayObject, projectionMatrix: Matrix4)(implicit metrics: IMetrics): Unit = {

    val compressed = displayObject.toCompressed(projectionMatrix)

    bindToBuffer(cNc.context, vertexBuffer, compressed.vertices)
    bindToBuffer(cNc.context, textureBuffer, compressed.textureCoordinates)
    bindToBuffer(cNc.context, effectsBuffer, compressed.effectValues)

    // Switch to canvas
    FrameBufferFunctions.switchToCanvas(cNc, config.clearColor)

    // Use Program
    cNc.context.useProgram(mergeShaderProgram)

    // Setup attributes
    bindShaderToBuffer(cNc, mergeShaderProgram, vertexBuffer, textureBuffer, effectsBuffer)

    // Setup Uniforms
    setupMergeFragmentShader(cNc.context, mergeShaderProgram, gameFrameBuffer.texture, lightingFrameBuffer.texture, uiFrameBuffer.texture)

    // Draw
    cNc.context.drawArrays(compressed.mode, 0, compressed.vertexCount)

    metrics.record(ToCanvasDrawCallMetric)

  }

}
