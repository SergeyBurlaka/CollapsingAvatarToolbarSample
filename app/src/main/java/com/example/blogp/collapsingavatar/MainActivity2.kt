package com.example.blogp.collapsingavatar


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import timber.log.Timber


class MainActivity2 : AppCompatActivity() {
    private lateinit var ivUserAvatar: ImageView
    private var EXPAND_AVATAR_SIZE: Float = 0F
    private var COLLAPSE_IMAGE_SIZE: Float = 0F
    private var margin: Float = 0F
    private lateinit var toolbar: Toolbar
    private lateinit var appBarLayout: AppBarLayout
    private var cashCollapseState: Pair<Int, Int>? = null
    private lateinit var titleToolbarText: AppCompatTextView
    private lateinit var titleToolbarTextSingle: AppCompatTextView
    /*   private lateinit var collapsingAvatarContainer: FrameLayout*/
    private lateinit var background: FrameLayout

    private var startAvatarAnimatePointY: Float = 0F
    private var animateWeigt: Float = 0F

    private var isCalculated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**/
        EXPAND_AVATAR_SIZE = resources.getDimension(R.dimen.default_expanded_image_size)
        COLLAPSE_IMAGE_SIZE = resources.getDimension(R.dimen.default_collapsed_image_size)
        margin = resources.getDimension(R.dimen.item_decoration)
        /* collapsingAvatarContainer = findViewById(R.id.stuff_container)*/
        appBarLayout = findViewById(R.id.app_bar_layout)
        toolbar = findViewById(R.id.anim_toolbar)
        toolbar.visibility = View.INVISIBLE
        ivUserAvatar = findViewById(R.id.imgb_avatar_wrap)
        titleToolbarText = findViewById(R.id.tv_profile_name)
        titleToolbarTextSingle = findViewById(R.id.tv_profile_name_single)
        background = findViewById(R.id.fl_background)
        /**/
        appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, i ->
                    if (isCalculated.not()) {
                        startAvatarAnimatePointY = Math.abs((appBarLayout.height - EXPAND_AVATAR_SIZE) / appBarLayout.totalScrollRange)
                        animateWeigt = 1 / (1 - startAvatarAnimatePointY)
                        isCalculated = true
                    }
                    /**/
                    updateViews(Math.abs(i / appBarLayout.totalScrollRange.toFloat()))
                })
    }

    private fun updateViews(offset: Float) {
        /* Swith transparent*/
        when {
            offset > mUpperLimitTransparently && offset < ABROAD -> {
                titleToolbarText.alpha = 1 - offset
            }
            offset > mLowerLimitTransparently && offset < mUpperLimitTransparently -> {
                titleToolbarText.alpha = 1f
            }
            else -> ivUserAvatar.alpha = 1f
        }
        /** collapse -expand switch*/
        val result: Pair<Int, Int> = when {
            offset < ABROAD -> {
                Pair(TO_EXPANDED, cashCollapseState?.second
                        ?: WAIT_FOR_SWITCH)
            }
            else -> {
                Pair(TO_COLLAPSED, cashCollapseState?.second ?: WAIT_FOR_SWITCH)
            }
        }
        result.apply {
            var translationY = 0f
            var headContainerHeight = 0f
            when {
                cashCollapseState != null && cashCollapseState != this -> {
                    when (first) {
                        TO_EXPANDED -> {
                            translationY = toolbar.height.toFloat()
                            headContainerHeight = appBarLayout.totalScrollRange.toFloat()
                            /* set avatar on start position (center of parent frame layout)*/
                            ivUserAvatar.translationX = 0F
                            /**/
                            background.setBackgroundColor(ContextCompat.getColor(this@MainActivity2, R.color.color_transparent))
                            /* hide top titles on toolbar*/
                            titleToolbarText.visibility = View.VISIBLE
                            titleToolbarTextSingle.visibility = View.INVISIBLE
                        }

                        TO_COLLAPSED -> {
                            translationY = appBarLayout.totalScrollRange.toFloat()
                            headContainerHeight = toolbar.height.toFloat()
                            /*set avatar in top left*//*
                            ValueAnimator.ofFloat(ivUserAvatar.translationX,  appBarLayout.width / 2f - collapsedImageSize / 2 - margin * 2).apply {
                                addUpdateListener {
                                    if (cashCollapseState!!.first == TO_COLLAPSED) {
                                        ivUserAvatar.translationX = it.animatedValue as Float//avatar horizontal moving
                                    }
                                }
                                interpolator = AnticipateOvershootInterpolator()
                                startDelay = 75
                                duration = 400
                                start()
                            }*/
                            /**/
                            background.setBackgroundColor(ContextCompat.getColor(this@MainActivity2, R.color.colorPrimary))
                            /* show titles on toolbar with animation*/
                            titleToolbarText.visibility = View.INVISIBLE
                            /*titleToolbarTextSingle.apply {
                                visibility = View.VISIBLE
                                alpha = 0.3f
                                this.translationX = width.toFloat() / 2
                                animate().translationX(0f)
                                        .setInterpolator(AnticipateOvershootInterpolator())
                                        .alpha(1.0f)
                                        .setStartDelay(75)
                                        .setDuration(400)
                                        .setListener(null)
                            }*/
                        }
                    }

                    /*collapsingAvatarContainer.apply {
                        layoutParams.height = headContainerHeight.toInt()
                        this.translationY = translationY
                        requestLayout()
                    }*/

                    /**/
                    cashCollapseState = Pair(first, SWITCHED)
                }
                else -> {
                    cashCollapseState = Pair(first, WAIT_FOR_SWITCH)
                }
            }

                /* Collapse avatar img*/
                ivUserAvatar.apply {
                    if (offset > startAvatarAnimatePointY) {

                        val animateOffset = (offset - startAvatarAnimatePointY) * animateWeigt
                        Timber.d("offset for anim $animateOffset")
                        val avatarSize = EXPAND_AVATAR_SIZE - (EXPAND_AVATAR_SIZE - COLLAPSE_IMAGE_SIZE) * animateOffset

                        this.layoutParams.also {
                            if (it.height != Math.round(avatarSize)) {
                                it.height = Math.round(avatarSize)
                                it.width = Math.round(avatarSize)

                            }
                        }
                        titleToolbarTextSingle.setTextSize(TypedValue.COMPLEX_UNIT_PX, offset)

                        ((appBarLayout.width / 2f - COLLAPSE_IMAGE_SIZE / 2 - margin * 2) * animateOffset).apply {
                            translationX = this
                        }
                    }
            }

            //todo remove
            titleToolbarText.visibility = View.INVISIBLE
            titleToolbarTextSingle.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val ABROAD = 1f
        const val TO_EXPANDED = 0
        const val TO_COLLAPSED = 1
        const val WAIT_FOR_SWITCH = 0
        const val SWITCHED = 1
    }

    private val mLowerLimitTransparently = ABROAD * 0.45
    private val mUpperLimitTransparently = ABROAD * 0.65
}
