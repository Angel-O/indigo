package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.RGBA

final case class SceneUpdateFragment(
    gameLayer: SceneLayer,
    lightingLayer: SceneLayer,
    distortionLayer: SceneLayer,
    uiLayer: SceneLayer,
    ambientLight: RGBA,
    lights: List[Light],
    globalEvents: List[GlobalEvent],
    audio: SceneAudio,
    screenEffects: ScreenEffects,
    cloneBlanks: List[CloneBlank]
) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addGameLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addGameLayerNodes(nodes.toList)

  def addGameLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(gameLayer = gameLayer ++ nodes)

  def addLightingLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addLightingLayerNodes(nodes.toList)

  def addLightingLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(lightingLayer = lightingLayer ++ nodes)

  def addDistortionLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addDistortionLayerNodes(nodes.toList)

  def addDistortionLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(distortionLayer = distortionLayer ++ nodes)

  def addUiLayerNodes(nodes: SceneGraphNode*): SceneUpdateFragment =
    addUiLayerNodes(nodes.toList)

  def addUiLayerNodes(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(uiLayer = uiLayer ++ nodes)

  def withAmbientLight(light: RGBA): SceneUpdateFragment =
    this.copy(ambientLight = light)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    this.copy(ambientLight = ambientLight.withAmount(amount))

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    withAmbientLight(RGBA(r, g, b, 1))

  def noLights: SceneUpdateFragment =
    this.copy(lights = Nil)

  def withLights(newLights: Light*): SceneUpdateFragment =
    withLights(newLights.toList)

  def withLights(newLights: List[Light]): SceneUpdateFragment =
    this.copy(lights = newLights)

  def addLights(newLights: Light*): SceneUpdateFragment =
    addLights(newLights.toList)

  def addLights(newLights: List[Light]): SceneUpdateFragment =
    withLights(lights ++ newLights)

  def addGlobalEvents(events: GlobalEvent*): SceneUpdateFragment =
    addGlobalEvents(events.toList)

  def addGlobalEvents(events: List[GlobalEvent]): SceneUpdateFragment =
    this.copy(globalEvents = globalEvents ++ events)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    this.copy(audio = sceneAudio)

  def addCloneBlanks(blanks: CloneBlank*): SceneUpdateFragment =
    addCloneBlanks(blanks.toList)

  def addCloneBlanks(blanks: List[CloneBlank]): SceneUpdateFragment =
    this.copy(cloneBlanks = cloneBlanks ++ blanks)

  def withSaturationLevel(amount: Double): SceneUpdateFragment =
    this.copy(
      gameLayer = gameLayer.withSaturationLevel(amount),
      lightingLayer = lightingLayer.withSaturationLevel(amount),
      uiLayer = uiLayer.withSaturationLevel(amount)
    )

  def withGameLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    this.copy(gameLayer = gameLayer.withSaturationLevel(amount))

  def withLightingLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    this.copy(lightingLayer = lightingLayer.withSaturationLevel(amount))

  def withUiLayerSaturationLevel(amount: Double): SceneUpdateFragment =
    this.copy(uiLayer = uiLayer.withSaturationLevel(amount))

  def withColorOverlay(overlay: RGBA): SceneUpdateFragment =
    this.copy(screenEffects = ScreenEffects(overlay, overlay))

  def withGameColorOverlay(overlay: RGBA): SceneUpdateFragment =
    this.copy(screenEffects = screenEffects.withGameColorOverlay(overlay))

  def withUiColorOverlay(overlay: RGBA): SceneUpdateFragment =
    this.copy(screenEffects = screenEffects.withUiColorOverlay(overlay))

  def withTint(tint: RGBA): SceneUpdateFragment =
    this.copy(
      gameLayer = gameLayer.withTint(tint),
      lightingLayer = lightingLayer.withTint(tint),
      uiLayer = uiLayer.withTint(tint)
    )

  def withGameLayerTint(tint: RGBA): SceneUpdateFragment =
    this.copy(gameLayer = gameLayer.withTint(tint))

  def withLightingLayerTint(tint: RGBA): SceneUpdateFragment =
    this.copy(lightingLayer = lightingLayer.withTint(tint))

  def withUiLayerTint(tint: RGBA): SceneUpdateFragment =
    this.copy(uiLayer = uiLayer.withTint(tint))

  def withMagnification(level: Int): SceneUpdateFragment =
    this.copy(
      gameLayer = gameLayer.withMagnification(level),
      lightingLayer = lightingLayer.withMagnification(level),
      distortionLayer = distortionLayer.withMagnification(level),
      uiLayer = uiLayer.withMagnification(level)
    )

  def withGameLayerMagnification(level: Int): SceneUpdateFragment =
    this.copy(gameLayer = gameLayer.withMagnification(level))

  def withLightingLayerMagnification(level: Int): SceneUpdateFragment =
    this.copy(lightingLayer = lightingLayer.withMagnification(level))

  def withDistortionLayerMagnification(level: Int): SceneUpdateFragment =
    this.copy(distortionLayer = distortionLayer.withMagnification(level))

  def withUiLayerMagnification(level: Int): SceneUpdateFragment =
    this.copy(uiLayer = uiLayer.withMagnification(level))
}
object SceneUpdateFragment {

  def apply(
      gameLayer: List[SceneGraphNode],
      lightingLayer: List[SceneGraphNode],
      distortionLayer: List[SceneGraphNode],
      uiLayer: List[SceneGraphNode],
      ambientLight: RGBA,
      lights: List[Light],
      globalEvents: List[GlobalEvent],
      audio: SceneAudio,
      screenEffects: ScreenEffects,
      cloneBlanks: List[CloneBlank]
  ): SceneUpdateFragment =
    SceneUpdateFragment(
      SceneLayer(gameLayer),
      SceneLayer(lightingLayer),
      SceneLayer(distortionLayer),
      SceneLayer(uiLayer),
      ambientLight,
      lights,
      globalEvents,
      audio,
      screenEffects,
      cloneBlanks
    )

  def apply(gameLayer: SceneGraphNode*): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.toList, Nil, Nil, Nil, RGBA.None, Nil, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def apply(gameLayer: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(gameLayer.toList, Nil, Nil, Nil, RGBA.None, Nil, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, Nil, Nil, RGBA.None, Nil, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.gameLayer |+| b.gameLayer,
      a.lightingLayer |+| b.lightingLayer,
      a.distortionLayer |+| b.distortionLayer,
      a.uiLayer |+| b.uiLayer,
      a.ambientLight + b.ambientLight,
      a.lights ++ b.lights,
      a.globalEvents ++ b.globalEvents,
      a.audio |+| b.audio,
      a.screenEffects |+| b.screenEffects,
      a.cloneBlanks ++ b.cloneBlanks
    )
}
