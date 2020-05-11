package indigo.shared.animation

import indigo.shared.time.GameTime
import indigo.shared.animation.AnimationAction._
import indigo.shared.datatypes.{BindingKey, Point}
import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo
import indigo.shared.AsString

import indigo.shared.EqualTo._
import indigo.shared.datatypes.Material

final case class Animation(
    animationKey: AnimationKey,
    material: Material,
    spriteSheetSize: Point,
    currentCycleLabel: CycleLabel,
    cycles: NonEmptyList[Cycle]//,
    // actions: List[AnimationAction]
) {

  val frameHash: String =
    currentFrame.bounds.hash + "_" + material.hash

  def currentCycle: Cycle =
    Animation.currentCycle(this)

  def addCycle(cycle: Cycle): Animation =
    Animation.addCycle(this, cycle)

  // def addAction(action: AnimationAction): Animation =
  //   Animation.addAction(this, action)

  def withAnimationKey(animationKey: AnimationKey): Animation =
    Animation.withAnimationKey(this, animationKey)

  def currentCycleName: String =
    currentCycle.label.value

  def currentFrame: Frame =
    currentCycle.currentFrame

  def saveMemento(bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, currentCycleLabel, currentCycle.saveMemento)

  def applyMemento(memento: AnimationMemento): Animation =
    Animation.applyMemento(this, memento)

  def runActions(actions: List[AnimationAction], gameTime: GameTime): Animation =
    Animation.runActions(this, actions, gameTime)

}

object Animation {

  implicit def animationEqualTo(
      implicit eAK: EqualTo[AnimationKey],
      eM: EqualTo[Material],
      eP: EqualTo[Point],
      eCL: EqualTo[CycleLabel],
      eNelC: EqualTo[NonEmptyList[Cycle]]//,
      // eLA: EqualTo[List[AnimationAction]]
  ): EqualTo[Animation] =
    EqualTo.create { (a, b) =>
      eAK.equal(a.animationKey, b.animationKey) &&
      eM.equal(a.material, b.material) &&
      eP.equal(a.spriteSheetSize, b.spriteSheetSize) &&
      eCL.equal(a.currentCycleLabel, b.currentCycleLabel) &&
      eNelC.equal(a.cycles, b.cycles)// &&
      // eLA.equal(a.actions, b.actions)
    }

  implicit def animationAsString(
      implicit sAK: AsString[AnimationKey],
      sM: AsString[Material],
      sP: AsString[Point],
      sCL: AsString[CycleLabel],
      sNelC: AsString[NonEmptyList[Cycle]]//,
      // sLA: AsString[List[AnimationAction]]
  ): AsString[Animation] =
    AsString.create { a =>
      s"Animation(${sAK.show(a.animationKey)}, ${sM.show(a.material)}, ${sP.show(a.spriteSheetSize)}, ${sCL.show(a.currentCycleLabel)}, ${sNelC.show(a.cycles)})"
    }

  def create(animationKey: AnimationKey, material: Material, spriteSheetSize: Point, cycle: Cycle): Animation =
    apply(animationKey, material, spriteSheetSize, cycle.label, NonEmptyList(cycle))

  def currentCycle(animations: Animation): Cycle =
    animations.cycles.find(_.label === animations.currentCycleLabel).getOrElse(animations.cycles.head)

  def addCycle(animations: Animation, cycle: Cycle): Animation =
    animations.copy(cycles = cycle :: animations.cycles)

  // def addAction(animations: Animation, action: AnimationAction): Animation =
  //   animations.copy(actions = animations.actions :+ action)

  def withAnimationKey(animations: Animation, animationKey: AnimationKey): Animation =
    animations.copy(animationKey = animationKey)

  def saveMemento(animations: Animation, bindingKey: BindingKey): AnimationMemento =
    AnimationMemento(bindingKey, animations.currentCycleLabel, animations.currentCycle.saveMemento)

  def applyMemento(animations: Animation, memento: AnimationMemento): Animation =
    animations.copy(
      cycles = animations.cycles.map { c =>
        if (c.label === memento.currentCycleLabel) {
          c.applyMemento(memento.currentCycleMemento)
        } else c
      }
    )

  def runActions(animation: Animation, actions: List[AnimationAction], gameTime: GameTime): Animation =
    actions.foldLeft(animation) { (anim, action) =>
      action match {
        case ChangeCycle(newLabel) if animation.cycles.exists(_.label === newLabel) =>
          anim.copy(currentCycleLabel = newLabel)

        case ChangeCycle(_) =>
          anim

        case _ =>
          anim.copy(
            cycles = anim.cycles.map { c =>
              if (c.label === anim.currentCycleLabel) {
                c.runActions(gameTime, actions)
              } else c
            }
          )
      }
    }
}

final class AnimationMemento(val bindingKey: BindingKey, val currentCycleLabel: CycleLabel, val currentCycleMemento: CycleMemento)
object AnimationMemento {

  implicit def animationMementoAsString(implicit bk: AsString[BindingKey], cl: AsString[CycleLabel], cm: AsString[CycleMemento]): AsString[AnimationMemento] =
    AsString.create { m =>
      s"""AnimationMemento(bindingKey = ${bk.show(m.bindingKey)}, cycleLabel = ${cl.show(m.currentCycleLabel)}, cycleMemento = ${cm.show(m.currentCycleMemento)}, )"""
    }

  def apply(bindingKey: BindingKey, currentCycleLabel: CycleLabel, currentCycleMemento: CycleMemento): AnimationMemento =
    new AnimationMemento(bindingKey, currentCycleLabel, currentCycleMemento)
}
