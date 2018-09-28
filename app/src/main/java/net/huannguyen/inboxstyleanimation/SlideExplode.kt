package net.huannguyen.inboxstyleanimation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.transition.TransitionValues
import android.transition.Visibility
import android.view.View
import android.view.ViewGroup

private const val KEY_SCREEN_BOUNDS = "screenBounds"

/**
 * A simple Transition which allows the views above the epic centre to transition upwards and views
 * below the epic centre to transition downwards.
 */
class SlideExplode : Visibility() {
  private val mTempLoc = IntArray(2)

  private fun captureValues(transitionValues: TransitionValues) {
    val view = transitionValues.view
    view.getLocationOnScreen(mTempLoc)
    val left = mTempLoc[0]
    val top = mTempLoc[1]
    val right = left + view.width
    val bottom = top + view.height
    transitionValues.values[KEY_SCREEN_BOUNDS] = Rect(left, top, right, bottom)
  }

  override fun captureStartValues(transitionValues: TransitionValues) {
    super.captureStartValues(transitionValues)
    captureValues(transitionValues)
  }

  override fun captureEndValues(transitionValues: TransitionValues) {
    super.captureEndValues(transitionValues)
    captureValues(transitionValues)
  }

  override fun onAppear(sceneRoot: ViewGroup, view: View,
                        startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
    if (endValues == null) return null

    val bounds = endValues.values[KEY_SCREEN_BOUNDS] as Rect
    val endY = view.translationY
    val startY = endY + calculateDistance(sceneRoot, bounds)
    return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, startY, endY)
  }

  override fun onDisappear(sceneRoot: ViewGroup, view: View,
                           startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
    if (startValues == null) return null

    val bounds = startValues.values[KEY_SCREEN_BOUNDS] as Rect
    val startY = view.translationY
    val endY = startY + calculateDistance(sceneRoot, bounds)
    return ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, startY, endY)
  }

  private fun calculateDistance(sceneRoot: View, viewBounds: Rect): Int {
    sceneRoot.getLocationOnScreen(mTempLoc)
    val sceneRootY = mTempLoc[1]
    return when {
      epicenter == null -> -sceneRoot.height
      viewBounds.top <= epicenter.top -> sceneRootY - epicenter.top
      else -> sceneRootY + sceneRoot.height - epicenter.bottom
    }
  }
}
