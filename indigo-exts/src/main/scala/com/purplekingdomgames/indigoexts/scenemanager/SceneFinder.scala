package com.purplekingdomgames.indigoexts.scenemanager

import com.purplekingdomgames.indigo.runtime.Logger
import com.purplekingdomgames.indigoexts.collections.NonEmptyList
case class SceneFinder(previous: List[ScenePosition], current: ScenePosition, next: List[ScenePosition]) {

  val sceneCount: Int =
    toList.length

  def toList: List[ScenePosition] =
    previous ++ List(current) ++ next

  def toNel: NonEmptyList[ScenePosition] =
    previous match {
      case Nil =>
        NonEmptyList(current, next)

      case x :: xs =>
        NonEmptyList(x, (xs :+ current) ++ next)
    }

  def giveCurrent: ScenePosition =
    current

  def forward: SceneFinder =
    next match {
      case Nil =>
        this

      case x :: xs =>
        this.copy(previous :+ current, x, xs)
    }

  def backward: SceneFinder =
    previous.reverse match {
      case Nil =>
        this

      case x :: xs =>
        this.copy(xs.reverse, x, current :: next)
    }

  def jumpToSceneByPosition(index: Int): SceneFinder =
    index match {
      case i if i < 0 =>
        this

      case i if i > sceneCount =>
        this

      case i if i == current.index =>
        this

      case i if i < current.index =>
        this.backward.jumpToSceneByPosition(index)

      case i if i > current.index =>
        this.forward.jumpToSceneByPosition(index)

      case _ =>
        this
    }

  def jumpToSceneByName(name: SceneName): SceneFinder =
    this.toList
      .find(p => p.name == name)
      .map(p => jumpToSceneByPosition(p.index)) match {
      case Some(sf) =>
        sf

      case None =>
        Logger.errorOnce("Failed to find scene called: " + name.name)
        this
    }

}

object SceneFinder {

  def fromScenes[GameModel, ViewModel](
      scenesList: ScenesList[GameModel, ViewModel, _, _]
  ): SceneFinder = {
    val a = scenesList.listSceneNames.zipWithIndex.map(p => ScenePosition(p._2, p._1))

    SceneFinder(Nil, a.head, a.tail)
  }

}

case class ScenePosition(index: Int, name: SceneName)
