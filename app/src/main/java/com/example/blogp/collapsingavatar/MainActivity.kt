package com.example.blogp.collapsingavatar


import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView


class MainActivity : AppCompatActivity() {
    private lateinit var ivUserAvatar: ImageView
    private var EXPAND_AVATAR_SIZE: Float = 0F
    private var COLLAPSE_IMAGE_SIZE: Float = 0F
    private var margin: Float = 0F
    private lateinit var toolbar: Toolbar
    private lateinit var appBarLayout: AppBarLayout
    private var cashCollapseState: Pair<Int, Int>? = null
    private lateinit var titleToolbarText: AppCompatTextView
    private lateinit var titleToolbarTextSingle: AppCompatTextView
    private lateinit var tvWorkAround: AppCompatTextView
    /*   private lateinit var collapsingAvatarContainer: FrameLayout*/
    private lateinit var background: FrameLayout
    /**/
    private var startPointY: Float = 0F
    private var animWeigt: Float = 0F
    private var isCalculated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**/
        EXPAND_AVATAR_SIZE = resources.getDimension(R.dimen.default_expanded_image_size)
        COLLAPSE_IMAGE_SIZE = resources.getDimension(R.dimen.default_collapsed_image_size)
        margin = resources.getDimension(R.dimen.activity_margin)
        /* collapsingAvatarContainer = findViewById(R.id.stuff_container)*/
        appBarLayout = findViewById(R.id.app_bar_layout)
        toolbar = findViewById(R.id.anim_toolbar)
        toolbar.visibility = View.INVISIBLE
        ivUserAvatar = findViewById(R.id.imgb_avatar_wrap)
        titleToolbarText = findViewById(R.id.tv_profile_name)
        titleToolbarTextSingle = findViewById(R.id.tv_profile_name_single)
        background = findViewById(R.id.fl_background)
        tvWorkAround = findViewById(R.id.tv_workaround)
        /**/
        appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { appBarLayout, i ->
                    if (isCalculated.not()) {
                        startPointY = Math.abs((appBarLayout.height - EXPAND_AVATAR_SIZE) / appBarLayout.totalScrollRange)
                        animWeigt = 1 / (1 - startPointY)
                        isCalculated = true
                    }
                    /**/
                    updateViews(Math.abs(i / appBarLayout.totalScrollRange.toFloat()))
                })
    }

    private fun updateViews(offset: Float) {
        /* Switch transparent*/
        when {
            offset > mUpperLimitTransparently -> {
                titleToolbarText.alpha = (0F)
            }

            else -> {
                titleToolbarText.alpha = (1f)
                ivUserAvatar.alpha = 1f
            }
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
            when {
                cashCollapseState != null && cashCollapseState != this -> {
                    when (first) {
                        TO_EXPANDED -> {
                            /* set avatar on start position (center of parent frame layout)*/
                            ivUserAvatar.translationX = 0F
                            /**/
                            background.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.color_transparent))
                            /* hide top titles on toolbar*/
                            titleToolbarText.visibility = View.VISIBLE
                            titleToolbarTextSingle.visibility = View.INVISIBLE
                        }
                        TO_COLLAPSED -> {
                            /**/
                            background.apply {
                                alpha = 0F
                                setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                                animate().setDuration(1000).alpha(1.0F)
                            }
                            /* show titles on toolbar with animation*/
                            titleToolbarTextSingle.apply {
                                visibility = View.VISIBLE
                                alpha = 0F
                                animate()
                                        .setDuration(500)
                                        .alpha(1.0f)
                            }
                        }
                    }
                    cashCollapseState = Pair(first, SWITCHED)
                }
                else -> {
                    cashCollapseState = Pair(first, WAIT_FOR_SWITCH)
                }
            }

            /* Collapse avatar img*/
            ivUserAvatar.apply {
                when {
                    offset > startPointY -> {
                        val animOffset = (offset - startPointY) * animWeigt
                        val avatarSize = EXPAND_AVATAR_SIZE - (EXPAND_AVATAR_SIZE - COLLAPSE_IMAGE_SIZE) * animOffset
                        this.layoutParams.also {
                            it.height = Math.round(avatarSize)
                            it.width = Math.round(avatarSize)
                        }
                        tvWorkAround.setTextSize(TypedValue.COMPLEX_UNIT_PX, offset)

                        this.translationX = ((appBarLayout.width / 2f - margin * 2 - (EXPAND_AVATAR_SIZE - avatarSize) / 2) * animOffset)
                       // this.translationY = (-(toolbar.height + appBarLayout.totalScrollRange - avatarSize) * (1 - offset) * 0.1).toFloat()
                    }
                    else -> this.layoutParams.also {
                        if (it.height != EXPAND_AVATAR_SIZE.toInt()) {
                            it.height = EXPAND_AVATAR_SIZE.toInt()
                            it.width = EXPAND_AVATAR_SIZE.toInt()
                            this.layoutParams = it
                        }
                        translationX = 0f
                    }
                }
            }
        }
    }

    companion object {
        const val ABROAD = 0.75f
        const val TO_EXPANDED = 0
        const val TO_COLLAPSED = 1
        const val WAIT_FOR_SWITCH = 0
        const val SWITCHED = 1
    }

    private val mUpperLimitTransparently = ABROAD * 0.67
}
