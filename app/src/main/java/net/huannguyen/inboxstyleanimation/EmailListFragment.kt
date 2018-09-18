package net.huannguyen.inboxstyleanimation

import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Transition
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import net.huannguyen.inboxstyleanimation.R.layout

private val transitionInterpolator = FastOutSlowInInterpolator()
private const val TRANSITION_DURATION = 300L
private const val TAP_POSITION = "tap_position"

class EmailListFragment : Fragment() {
  private var tapPosition: Int = -1

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.email_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    postponeEnterTransition()
    val recyclerView = view.findViewById<RecyclerView>(R.id.email_list)
    with(recyclerView) {
      layoutManager = LinearLayoutManager(view.context)
      addItemDecoration(DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL))
      adapter = EmailAdapter()
    }

    exitTransition = SlideExplode().apply {
      duration = TRANSITION_DURATION
      interpolator = transitionInterpolator
    }

    (view.parent as? ViewGroup)?.doOnPreDraw {
      tapPosition = savedInstanceState?.getInt(TAP_POSITION, -1) ?: -1
      val viewRect = Rect()
      val layoutManager = recyclerView.layoutManager as LinearLayoutManager
      layoutManager.findViewByPosition(tapPosition)?.getGlobalVisibleRect(viewRect)

      (exitTransition as Transition).epicenterCallback =
          object : Transition.EpicenterCallback() {
            override fun onGetEpicenter(transition: Transition): Rect {
              return viewRect
            }
          }
      startPostponedEnterTransition()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(TAP_POSITION, tapPosition)
  }

  private inner class EmailAdapter : RecyclerView.Adapter<EmailViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder =
        EmailViewHolder(
          LayoutInflater.from(parent.context).inflate(layout.email_item, parent, false))

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
      fun expandHandler() {
        tapPosition = position

        val viewRect = Rect()
        holder.itemView.getGlobalVisibleRect(viewRect)

        (this@EmailListFragment.exitTransition as Transition).epicenterCallback =
            object : Transition.EpicenterCallback() {
              override fun onGetEpicenter(transition: Transition): Rect {
                return viewRect
              }
            }

        val sharedElementTransition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeImageTransform()).apply {
              duration = TRANSITION_DURATION
              setCommonInterpolator(transitionInterpolator)
            }

        val fragment = EmailDetailsFragment().apply {
          sharedElementEnterTransition = sharedElementTransition
          sharedElementReturnTransition = sharedElementTransition
        }

        activity!!.supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .addSharedElement(holder.itemView, getString(R.string.transition_name))
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
      }

      holder.bindData("Email ${position + 1}", ::expandHandler)
    }

    override fun getItemCount() = 17
  }

  private class EmailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var email: String? = null

    fun bindData(email: String, expandHandler: () -> Unit) {
      this.email = email
      itemView.setOnClickListener { expandHandler() }
      itemView.transitionName = email
      (itemView as TextView).text = email
    }
  }
}
