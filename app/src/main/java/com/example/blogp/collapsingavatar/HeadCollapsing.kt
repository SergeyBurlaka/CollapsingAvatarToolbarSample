package com.example.blogp.collapsingavatar


import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.design.widget.AppBarLayout
import android.support.v4.util.Pair
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView


class HeadCollapsing(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs), AppBarLayout.OnOffsetChangedListener {

    private lateinit var avatar: ImageView
    private val expandedImageSize: Float
    private val collapsedImageSize: Float
    private val expandedTextSize: Float
    private val collapsedTextSize: Float
    private val activityMargin: Float
    private var valuesCalculatedAlready = false
    private lateinit var toolbar: Toolbar
    private lateinit var appBarLayout: AppBarLayout
    private var collapsedHeight: Float = 0.toFloat()
    private var expandedHeight: Float = 0.toFloat()
    private var maxOffset: Float = 0.toFloat()
    private var lastOffset = -1
    private var decorationMargin: Float
    //toolbar elements
    private var title: TextView? = null

    companion object {
        const val ABROAD = 0.95f
        const val EXPANDED_STATE = 0
        const val COLLAPSED_STATE = 1

        const val WAIT_FOR_SWITCH = 0
        const val SWITCHED = 1
    }

    constructor(context: Context) : this(context, null) {
        init()
    }

    init {
        init()
        val resources = resources
        collapsedImageSize = resources.getDimension(R.dimen.default_collapsed_image_size)
        expandedImageSize = resources.getDimension(R.dimen.default_expanded_image_size)
        collapsedTextSize = resources.getDimension(R.dimen.default_collapsed_text_size)
        expandedTextSize = resources.getDimension(R.dimen.default_expanded_text_size)
        activityMargin = resources.getDimension(R.dimen.activity_margin)
        decorationMargin = resources.getDimension(R.dimen.item_decoration)
    }

    private fun init() {
        //sdo nothing
    }

    private fun findParentAppBarLayout(): AppBarLayout {
        val parent = this.parent
        return parent as? AppBarLayout ?: if (parent.parent is AppBarLayout) {
            parent.parent as AppBarLayout
        } else {
            throw IllegalStateException("Must be inside an AppBarLayout")
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViews()
        appBarLayout.addOnOffsetChangedListener(this)
    }

    private fun findViews() {
        appBarLayout = findParentAppBarLayout()
        toolbar = findSiblingToolbar()
        avatar = findImage()

        title = findTitle()
    }

    private fun findImage(): ImageView {
        return findViewById(R.id.imgb_avatar)
                ?: throw IllegalStateException("View with id imgb_avatar not found")
    }

    private fun findTitle(): TextView {
        return findViewById(R.id.title)
                ?: throw IllegalStateException("View with id title not found")
    }

    private fun findSiblingToolbar(): Toolbar {
        val parent = this.parent as ViewGroup
        var i = 0
        val c = parent.childCount
        while (i < c) {
            val child = parent.getChildAt(i)
            if (child is Toolbar) {
                return child
            }
            i++
        }
        throw IllegalStateException("No toolbar found as sibling")
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, offset: Int) {
        if (lastOffset == offset) {
            return
        }

        lastOffset = offset
        if (!valuesCalculatedAlready) {
            calculateValues()
            valuesCalculatedAlready = true
        }
        val expandedPercentage = 1 - -offset / maxOffset
        Handler(Looper.getMainLooper()).post {
            updateViews(expandedPercentage)
        }
    }

    private fun calculateValues() {
        collapsedHeight = toolbar.height.toFloat()
        expandedHeight = (appBarLayout.height - toolbar.height).toFloat()
        maxOffset = expandedHeight
    }

    private var cashCollapseState: Pair<Int, Int>? = null

    private fun updateViews(updatePercentage: Float) {
        //calculate params
        val inversePercentage = 1 - updatePercentage
        var translationY = 0f //\* expandedPercentage
        var currHeight = 0f
        var animateState = Pair(0, 0)
        var transitionX = 0f
        var currentImageSize = 0

        //PUT collapsing avatar transparent
        when {
            inversePercentage > ABROAD * 0.75 && inversePercentage < ABROAD -> handler.post {
                avatar.alpha = updatePercentage * 1.8f
            }
            else -> handler.post {
                avatar.alpha = 1f
            }
        }

        when {
            inversePercentage > ABROAD -> title?.visibility = View.VISIBLE
            else -> title?.visibility = View.INVISIBLE
        }

        when {
            inversePercentage < ABROAD -> {
                animateState = Pair(EXPANDED_STATE, cashCollapseState?.second ?: WAIT_FOR_SWITCH)
            }

            inversePercentage > ABROAD -> {
                animateState = Pair(COLLAPSED_STATE, cashCollapseState?.second ?: WAIT_FOR_SWITCH)
            }
        }

        when {
            cashCollapseState != null && cashCollapseState != animateState -> {
                when (animateState.first) {
                    EXPANDED_STATE -> {
                        translationY = toolbar.height.toFloat()
                        //  translation = -currentOffset.toFloat() + toolbar.height.toFloat() //\* expandedPercentage
                        currHeight = expandedHeight
                        transitionX = 0f
                        currentImageSize = expandedImageSize.toInt()
                    }

                    COLLAPSED_STATE -> {
                        currentImageSize = collapsedImageSize.toInt()
                        translationY = (appBarLayout.height - toolbar.height).toFloat()
                        currHeight = collapsedHeight
                        transitionX = -appBarLayout.width / 2f + collapsedImageSize / 2 + activityMargin
                    }
                }

                //SWITCH STATE CASE
                cashCollapseState = Pair(animateState.first, SWITCHED)

                this.translationY = translationY
                this.layoutParams.height = currHeight.toInt()

                ValueAnimator.ofFloat(avatar.translationX, transitionX).apply {
                    addUpdateListener { avatar.translationX = it.animatedValue as Float }
                    duration = 500
                    interpolator = OvershootInterpolator()
                    start()
                }
                avatar.layoutParams.height = currentImageSize
                avatar.layoutParams.width = currentImageSize
            }

            else -> {
                cashCollapseState = Pair(animateState.first, WAIT_FOR_SWITCH)

            }
        }
        this.requestLayout()
    }

}