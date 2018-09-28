package net.huannguyen.inboxstyleanimation

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.NO_POSITION
import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.Transition
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import kotlinx.android.synthetic.main.email_list.emailList
import kotlinx.android.synthetic.main.email_list.progressBar
import net.huannguyen.inboxstyleanimation.R.layout
import net.huannguyen.inboxstyleanimation.State.InProgress
import net.huannguyen.inboxstyleanimation.State.Success
import kotlin.LazyThreadSafetyMode.NONE

private val transitionInterpolator = FastOutSlowInInterpolator()
private const val TRANSITION_DURATION = 300L
private const val TAP_POSITION = "tap_position"

class EmailListFragment : Fragment() {
  private var tapPosition = NO_POSITION
  val viewRect = Rect()
  private val emailAdapter = EmailAdapter()
  private val viewModel: EmailListViewModel by lazy(NONE) {
    ViewModelProviders.of(this).get(EmailListViewModel::class.java)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.email_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    tapPosition = savedInstanceState?.getInt(TAP_POSITION, NO_POSITION) ?: NO_POSITION

    // May not be the best place to postpone transition. Just an example to demo how reenter transition works.
    postponeEnterTransition()

    with(emailList) {
      layoutManager = LinearLayoutManager(view.context)
      addItemDecoration(DividerItemDecoration(view.context, LinearLayoutManager.VERTICAL))
      adapter = emailAdapter
    }

    if (viewModel.emails.value == null) viewModel.getEmails()

    viewModel.emails.observe(this,
                             Observer<State> { state -> state?.let { render(state) } })
  }

  private fun render(state: State) {
    when (state) {
      is InProgress -> {
        emailList.visibility = GONE
        progressBar.visibility = VISIBLE
        startPostponedEnterTransition()
      }

      is Success -> {
        emailList.visibility = VISIBLE
        progressBar.visibility = GONE
        emailAdapter.setData(state.data)
        (view?.parent as? ViewGroup)?.doOnPreDraw {
          if (exitTransition == null) {
            exitTransition = SlideExplode().apply {
              duration = TRANSITION_DURATION
              interpolator = transitionInterpolator
            }
          }

          val layoutManager = emailList.layoutManager as LinearLayoutManager
          layoutManager.findViewByPosition(tapPosition)?.let { view ->
            view.getGlobalVisibleRect(viewRect)
            (exitTransition as Transition).epicenterCallback =
                object : Transition.EpicenterCallback() {
                  override fun onGetEpicenter(transition: Transition) = viewRect
                }
          }

          startPostponedEnterTransition()
        }
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(TAP_POSITION, tapPosition)
  }

  private inner class EmailAdapter : RecyclerView.Adapter<EmailViewHolder>() {
    private var emails: List<String> = emptyList()

    fun setData(emails: List<String>) {
      this.emails = emails
      notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder =
        EmailViewHolder(LayoutInflater.from(parent.context).inflate(layout.email_item, parent, false))

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
      fun onViewClick() {
        tapPosition = position
        holder.itemView.getGlobalVisibleRect(viewRect)

        (this@EmailListFragment.exitTransition as Transition).epicenterCallback =
            object : Transition.EpicenterCallback() {
              override fun onGetEpicenter(transition: Transition) = viewRect
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
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .addSharedElement(holder.itemView, getString(R.string.transition_name))
            .commit()
      }

      holder.bindData(emails[position], ::onViewClick)
    }

    override fun getItemCount() = emails.size
  }
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
