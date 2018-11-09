package com.nicolettilu.hiddensearchwithrecyclerview

import android.animation.ObjectAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.nicolettilu.scrolldowntosearchrecyclerview.utils.Movement
import com.nicolettilu.scrolldowntosearchrecyclerview.utils.Utils

/**
 * Created by Luca Nicoletti
 * © 28/07/2018
 * All rights reserved.
 */

class HiddenSearchWithRecyclerView : ConstraintLayout {

    companion object {
        const val ANIM_DURATION = 300L
        const val MIN_SCROLL_TO_ANIM = 35f
        const val MIN_TAP_MOVEMENT = 5f
    }

    public var hideAtScroll = true
    public var scrollToTopBeforeShow = false
    public var scrollToBottomBeforeHide = false
    public var filterWhileTyping = true
    public var visibleAtInit = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBarLinearLayout: LinearLayout
    private lateinit var searchBarSearchView: SearchView
    private var isSearchBarVisible = visibleAtInit
    private var searchBarCanMoveUp = visibleAtInit
    private var searchBarCanMoveDown = !visibleAtInit
    private var lastYDrag: Float = 0F
    private var startYDrag: Float = 0F
    private var movementDirection: Movement = Movement.UP
    private var searchHeight = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        parseStyleAttrs(attrs)

    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        when (childCount) {
            0 -> {
                throw IllegalArgumentException("Need to add recycler view in the view!")
            }
            1 -> {
                recyclerView = getChildAt(0) as RecyclerView
                searchBarLinearLayout = View.inflate(context, R.layout.search_bar, null) as LinearLayout
                searchBarSearchView = searchBarLinearLayout.findViewById(R.id.searchBarSearchView)

                val recyclerViewLayoutParams = recyclerView.layoutParams as ConstraintLayout.LayoutParams
                val searchBarLayoutParams = ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                searchBarLayoutParams.topToTop = id
                searchBarLayoutParams.leftToLeft = id
                searchBarLayoutParams.rightToRight = id
                searchBarLinearLayout.layoutParams = searchBarLayoutParams

                searchBarLinearLayout.measure(View.MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                searchHeight = searchBarLinearLayout.measuredHeight

                addView(searchBarLinearLayout, 1, searchBarLayoutParams)

                recyclerViewLayoutParams.topToBottom = searchBarLinearLayout.id
                recyclerViewLayoutParams.bottomToBottom = id
                recyclerViewLayoutParams.leftToLeft = id
                recyclerViewLayoutParams.rightToRight = id
                recyclerView.layoutParams = recyclerViewLayoutParams
            }
            2 -> {
                initLayoutAndListeners()
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                lastYDrag = event.rawY
                startYDrag = event.rawY
                dispatchEventToChildView(event)
                movementDirection = Movement.NONE
            }
            MotionEvent.ACTION_MOVE -> {
                val delta = event.rawY - lastYDrag
                movementDirection = if (delta >= 0) Movement.DOWN else Movement.UP
                lastYDrag = event.rawY

                if (delta > 0) {
                    if (searchBarCanMoveDown && checkRecyclerNeedToScroll(movementDirection) && !searchBarSearchView.hasFocus())
                        dragSearch(delta)
                    else
                        dispatchEventToChildView(event)
                } else {
                    if (searchBarCanMoveUp && checkRecyclerNeedToScroll(movementDirection) && hideAtScroll && !searchBarSearchView.hasFocus())
                        dragSearch(delta)
                    else
                        dispatchEventToChildView(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (Math.abs(startYDrag - lastYDrag) > Utils.convertDpToPixel(context, MIN_SCROLL_TO_ANIM)) {
                    if (movementDirection == Movement.DOWN) {
                        if (searchBarCanMoveDown && checkRecyclerNeedToScroll(movementDirection) && !searchBarSearchView.hasFocus()) {
                            moveSearchToVisible()
                        } else {
                            dispatchEventToChildView(event)
                        }
                    } else {
                        if (searchBarCanMoveUp && checkRecyclerNeedToScroll(movementDirection) && hideAtScroll && !searchBarSearchView.hasFocus()) {
                            moveSearchBarToHide()
                        } else {
                            dispatchEventToChildView(event)
                        }
                    }
                } else {
                    if (Math.abs(startYDrag - lastYDrag) > Utils.convertDpToPixel(context, MIN_TAP_MOVEMENT) && !searchBarSearchView.hasFocus()) {
                        if (movementDirection == Movement.DOWN) {
                            moveSearchBarToHide()
                        } else {
                            moveSearchToVisible()
                        }
                    }
                    performClick()
                    dispatchEventToChildView(event)
                }
                startYDrag = 0F
                lastYDrag = 0F
                movementDirection = Movement.NONE
            }
        }
        return true
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        super.onInterceptTouchEvent(event)
        return true
    }

    private fun dispatchEventToChildView(event: MotionEvent) {
        if (event.y <= searchBarLinearLayout.y + searchBarLinearLayout.height
                && event.y > 0) {
            searchBarSearchView.dispatchTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                searchBarSearchView.isIconified = false
                searchBarSearchView.performClick()
            }
        } else {
            recyclerView.dispatchTouchEvent(event)
        }
    }

    private fun initLayoutAndListeners() {

        if (!visibleAtInit) {
            searchBarLinearLayout.y = - searchHeight.toFloat()
        } else {
            searchBarLinearLayout.y = 0f
        }

        searchBarSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(value: String?): Boolean {
                if (filterWhileTyping) {
                    if (recyclerView.adapter is Filterable) {
                        (recyclerView.adapter as Filterable).filter.filter(value)
                    }
                }
                searchBarSearchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(value: String?): Boolean {
                if (recyclerView.adapter is Filterable) {
                    (recyclerView.adapter as Filterable).filter.filter(value)
                }
                return true
            }

        })
    }

    private fun checkRecyclerNeedToScroll(movementDirection: Movement): Boolean {
        when (movementDirection) {
            Movement.DOWN -> {
                val firstElementVisibleInRecycler = (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                return firstElementVisibleInRecycler?.let { firstElement ->
                    if (scrollToTopBeforeShow) {
                        firstElement == 0
                    } else {
                        true
                    }
                } ?: false
            }
            Movement.UP -> {
                val lastElementVisibleInRecycler = (recyclerView.layoutManager as? LinearLayoutManager)?.findLastCompletelyVisibleItemPosition()
                return lastElementVisibleInRecycler?.let { lastElement ->
                    if (scrollToBottomBeforeHide) {
                        lastElement + 1 == recyclerView.adapter?.itemCount
                    } else {
                        true
                    }
                } ?: false
            }
            else -> return false
        }
    }

    private fun dragSearch(delta: Float) {
        val recyclerViewLayoutParams = recyclerView.layoutParams as ConstraintLayout.LayoutParams
        if (delta > 0) {
            if (searchBarLinearLayout.y >= 0) {
                searchBarLinearLayout.y = 0f
                recyclerViewLayoutParams.topMargin = searchBarLinearLayout.height
                isSearchBarVisible = true
                searchBarCanMoveDown = false
                searchBarCanMoveUp = true
            } else {
                searchBarLinearLayout.y += delta.toInt()
                recyclerViewLayoutParams.topMargin += delta.toInt()
                isSearchBarVisible = true
                searchBarCanMoveDown = true
                searchBarCanMoveUp = true
            }
        } else {
            if (-searchHeight.toFloat() <= searchBarLinearLayout.y) {
                searchBarLinearLayout.y += delta.toInt()
                recyclerViewLayoutParams.topMargin += delta.toInt()
                isSearchBarVisible = true
                searchBarCanMoveDown = true
                searchBarCanMoveUp = true
            } else {
                searchBarLinearLayout.y = -searchHeight.toFloat()
                recyclerViewLayoutParams.topMargin = 0
                isSearchBarVisible = false
                searchBarCanMoveDown = true
                searchBarCanMoveUp = false
            }
        }
        recyclerView.layoutParams = recyclerViewLayoutParams
    }

    private fun moveSearchToVisible() {
        val recyclerViewLayoutParams = recyclerView.layoutParams as ConstraintLayout.LayoutParams

        val startY = searchBarLinearLayout.y
        val endY = 0f

        val modalAnimator = ObjectAnimator.ofFloat(searchBarLinearLayout, "translationY", startY, endY)

        val totalMovement = searchBarLinearLayout.height
        val remainingMovement = -searchBarLinearLayout.y.toInt()

        val duration = ANIM_DURATION / totalMovement * remainingMovement

        modalAnimator.duration = if (duration > 0) duration else 0
        modalAnimator.addUpdateListener {
            recyclerViewLayoutParams.topMargin = searchBarLinearLayout.y.toInt() + searchBarLinearLayout.height
            recyclerView.layoutParams = recyclerViewLayoutParams
        }
        modalAnimator.start()
    }

    private fun moveSearchBarToHide() {
        val recyclerViewLayoutParams = recyclerView.layoutParams as ConstraintLayout.LayoutParams

        val startY = searchBarLinearLayout.y
        val endY = -searchBarLinearLayout.height.toFloat()

        val modalAnimator = ObjectAnimator.ofFloat(searchBarLinearLayout, "translationY", startY, endY)

        val totalMovement = searchBarLinearLayout.height
        val remainingMovement = searchBarLinearLayout.height - searchBarLinearLayout.y.toInt()

        val duration = ANIM_DURATION / totalMovement * remainingMovement

        modalAnimator.duration = if (duration > 0) duration else 0
        modalAnimator.addUpdateListener {
            recyclerViewLayoutParams.topMargin = searchBarLinearLayout.y.toInt() + searchBarLinearLayout.height
            recyclerView.layoutParams = recyclerViewLayoutParams
        }
        modalAnimator.start()
    }

    private fun parseStyleAttrs(attrs: AttributeSet?) {
        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.HiddenSearchWithRecyclerView,
                0, 0)

        try {
            visibleAtInit = a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_visibleAtInit, false)
            hideAtScroll = a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_hideAtScroll, true)
            scrollToBottomBeforeHide = a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_scrollToBottomBeforeHide, false)
            scrollToTopBeforeShow = a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_scrollToTopBeforeShow, false)
            filterWhileTyping = a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_filterWhileTyping, true)
        } finally {
            a.recycle()
        }
    }

}

