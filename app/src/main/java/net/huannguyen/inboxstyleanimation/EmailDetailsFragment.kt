package net.huannguyen.inboxstyleanimation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class EmailDetailsFragment : Fragment() {
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.email_details, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val content = view.findViewById<View>(R.id.content)
    content.alpha = 0f

    val animator = ObjectAnimator.ofFloat(content, View.ALPHA, 0f, 1f)
    animator.startDelay = 50
    animator.duration = 150
    animator.start()
  }
}
