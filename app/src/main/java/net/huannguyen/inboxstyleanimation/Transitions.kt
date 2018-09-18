package net.huannguyen.inboxstyleanimation

import android.transition.TransitionSet
import android.view.animation.Interpolator

fun TransitionSet.setCommonInterpolator(interpolator: Interpolator): TransitionSet {
  (0 until transitionCount)
      .map { index -> getTransitionAt(index) }
      .forEach { transition -> transition.interpolator = interpolator }

  return this
}
