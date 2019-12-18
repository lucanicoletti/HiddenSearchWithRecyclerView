package com.nicolettilu.hiddensearchwithrecyclerview

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nicolettilu.hiddensearchwithrecyclerview.utils.Utils
import com.nicolettilu.scrolldowntosearchrecyclerview.utils.Movement
import kotlin.math.abs

/**
 * Created by Luca Nicoletti
 * © 28/07/2018
 * All rights reserved.
 */

class HiddenSearchWithRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val ANIM_DURATION = 300L
        private const val MIN_SCROLL_TO_ANIM = 35f
        private const val MIN_TAP_MOVEMENT = 5f
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBarLinearLayout: LinearLayout
    private lateinit var searchBarSearchView: SearchView
    private var lastYDrag: Float = 0F
    private var startYDrag: Float = 0F
    private var movementDirection: Movement = Movement.UP
    private var searchHeight = 0

    var hideAtScroll = true
    var scrollToTopBeforeShow = false
    var scrollToBottomBeforeHide = false
    var filterWhileTyping = true
    var visibleAtInit = false
        set(value) {
            isSearchBarVisible = value
            searchBarCanMoveUp = value
            searchBarCanMoveDown = !value
            field = value
        }

    private var isSearchBarVisible = false
    private var searchBarCanMoveUp = false
    private var searchBarCanMoveDown = true

    init {
        parseStyleAttrs(attrs)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        when (childCount) {
            0 -> {
                throw IllegalArgumentException("Need to add recycler view in the view!")
            }
            1 -> {
                (getChildAt(0) as? RecyclerView)?.let { view ->
                    recyclerView = view
                    (View.inflate(context, R.layout.search_bar, null) as? LinearLayout)?.let {
                        searchBarLinearLayout = it
                        setupViews()
                    }
                }
            }
            2 -> {
                initLayoutAndListeners()
            }
        }
    }

    private fun setupViews() {
        searchBarSearchView = searchBarLinearLayout.findViewById(R.id.searchBarSearchView)

        val recyclerViewLayoutParams = recyclerView.layoutParams as? LayoutParams
        val searchBarLayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        searchBarLayoutParams.apply {
            topToTop = id
            leftToLeft = id
            rightToRight = id
        }
        searchBarLinearLayout.layoutParams = searchBarLayoutParams

        searchBarLinearLayout.measure(
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        searchHeight = searchBarLinearLayout.measuredHeight

        addView(searchBarLinearLayout, 1, searchBarLayoutParams)

        recyclerViewLayoutParams?.apply {
            topToBottom = searchBarLinearLayout.id
            bottomToBottom = id
            leftToLeft = id
            rightToRight = id
        }
        recyclerView.layoutParams = recyclerViewLayoutParams
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    @SuppressLint("ClickableViewAccessibility") // done inside onActionUp(event) function
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                onActionDown(event)
            }
            MotionEvent.ACTION_MOVE -> {
                onActionMove(event)
            }
            MotionEvent.ACTION_UP -> {
                onActionUp(event)
            }
        }
        return true
    }

    private fun onActionDown(event: MotionEvent) {
        lastYDrag = event.rawY
        startYDrag = event.rawY
        dispatchEventToChildView(event)
        movementDirection = Movement.NONE
    }

    private fun onActionUp(event: MotionEvent) {
        if (abs(startYDrag - lastYDrag) >
            Utils.convertDpToPixel(context, MIN_SCROLL_TO_ANIM)
        ) {
            if (movementDirection == Movement.DOWN) {
                onDownWithMinScrollDone(event)
            } else {
                onUpWithMinScrollDone(event)
            }
        } else {
            if (abs(startYDrag - lastYDrag) > Utils.convertDpToPixel(
                    context,
                    MIN_TAP_MOVEMENT
                ) && !searchBarSearchView.hasFocus()
            ) {
                if (movementDirection == Movement.DOWN) {
                    moveSearchToVisible()
                } else {
                    moveSearchBarToHide()
                }
            }
            performClick()
            dispatchEventToChildView(event)
        }
        startYDrag = 0F
        lastYDrag = 0F
        movementDirection = Movement.NONE
    }

    private fun onDownWithMinScrollDone(event: MotionEvent) {
        if (searchBarCanMoveDown && checkRecyclerNeedToScroll(movementDirection) && !searchBarSearchView.hasFocus()) {
            moveSearchToVisible()
        } else {
            dispatchEventToChildView(event)
        }
    }

    private fun onUpWithMinScrollDone(event: MotionEvent) {
        if (searchBarCanMoveUp && checkRecyclerNeedToScroll(movementDirection) && hideAtScroll && !searchBarSearchView.hasFocus()) {
            moveSearchBarToHide()
        } else {
            dispatchEventToChildView(event)
        }
    }

    private fun onActionMove(event: MotionEvent) {
        val delta = event.rawY - lastYDrag
        movementDirection = if (delta >= 0) Movement.DOWN else Movement.UP
        lastYDrag = event.rawY

        if (delta > 0) {
            if (
                searchBarCanMoveDown &&
                checkRecyclerNeedToScroll(movementDirection)
            ) {
                dragSearch(delta)
            } else {
                dispatchEventToChildView(event)
            }
        } else {
            if (
                searchBarCanMoveUp &&
                checkRecyclerNeedToScroll(movementDirection) &&
                hideAtScroll
            ) {
                dragSearch(delta)
            } else {
                dispatchEventToChildView(event)
            }
        }
    }


    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        super.onInterceptTouchEvent(event)
        return true
    }

    private fun dispatchEventToChildView(event: MotionEvent) {
        if (event.y <= searchBarLinearLayout.y + searchBarLinearLayout.height
            && event.y > 0
        ) {
            searchBarSearchView.dispatchTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                searchBarSearchView.isIconified = false
                searchBarSearchView.performClick()
            }
        } else {
            when {
                isSearchBarVisible -> {
                    val eventToDispatch = MotionEvent.obtain(
                        event.downTime,
                        event.eventTime,
                        event.action,
                        event.x,
                        event.y - searchBarLinearLayout.height,
                        event.metaState
                    )
                    recyclerView.dispatchTouchEvent(eventToDispatch)
                    eventToDispatch.recycle()
                }
                else -> {
                    recyclerView.dispatchTouchEvent(event)
                }
            }
        }
    }

    private fun initLayoutAndListeners() {

        if (!visibleAtInit) {
            searchBarLinearLayout.y = -searchHeight.toFloat()
        } else {
            searchBarLinearLayout.y = 0f
            val layoutParams = recyclerView.layoutParams as? LayoutParams
            layoutParams?.apply {
                topMargin += searchHeight
            }
            recyclerView.layoutParams = layoutParams
        }

        searchBarSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(value: String?): Boolean {
                if (filterWhileTyping) {
                    if (recyclerView.adapter is Filterable) {
                        (recyclerView.adapter as? Filterable)?.filter?.filter(value)
                    }
                }
                searchBarSearchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(value: String?): Boolean {
                if (recyclerView.adapter is Filterable) {
                    (recyclerView.adapter as? Filterable)?.filter?.filter(value)
                }
                return true
            }

        })
        requestLayout()
    }

    private fun checkRecyclerNeedToScroll(movementDirection: Movement): Boolean {
        return when (movementDirection) {
            Movement.DOWN -> {
                val firstElementVisibleInRecycler =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition()
                checkRecyclerNeedToScrollOnDownMovement(firstElementVisibleInRecycler) && !searchBarSearchView.hasFocus()
            }
            Movement.UP -> {
                val lastElementVisibleInRecycler =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findLastCompletelyVisibleItemPosition()
                checkRecyclerNeedToScrollOnUpMovement(lastElementVisibleInRecycler) && !searchBarSearchView.hasFocus()
            }
            else -> false
        }
    }

    private fun checkRecyclerNeedToScrollOnDownMovement(firstElement: Int?): Boolean {
        return if (scrollToTopBeforeShow) {
            firstElement == 0
        } else {
            true
        }
    }

    private fun checkRecyclerNeedToScrollOnUpMovement(lastElement: Int?): Boolean {
        return if (scrollToBottomBeforeHide && lastElement != null) {
            lastElement + 1 == recyclerView.adapter?.itemCount
        } else {
            true
        }
    }

    private fun dragSearch(delta: Float) {
        val recyclerViewLayoutParams = recyclerView.layoutParams as? LayoutParams
        recyclerView.layoutParams = if (delta > 0) {
            if (searchBarLinearLayout.y >= 0) {
                dragSearchPositiveDeltaSearchBarVisible(recyclerViewLayoutParams)
            } else {
                dragSearchPositiveDeltaSearchBarNotVisible(recyclerViewLayoutParams, delta)
            }
        } else if (delta < 0) {
            if (-searchHeight.toFloat() <= searchBarLinearLayout.y) {
                dragSearchNegativeDeltaSearchBarVisible(recyclerViewLayoutParams, delta)
            } else {
                dragSearchNegativeDeltaSearchBarNotVisible(recyclerViewLayoutParams)
            }
        } else recyclerView.layoutParams
        recyclerView.layoutParams = recyclerViewLayoutParams
    }

    private fun dragSearchPositiveDeltaSearchBarVisible(params: LayoutParams?): LayoutParams? {
        searchBarLinearLayout.y = 0f
        isSearchBarVisible = true
        searchBarCanMoveDown = false
        searchBarCanMoveUp = true
        return params?.apply {
            topMargin = searchBarLinearLayout.height
        }
    }

    private fun dragSearchPositiveDeltaSearchBarNotVisible(
        params: LayoutParams?,
        delta: Float
    ): LayoutParams? {
        searchBarLinearLayout.y += delta.toInt()
        isSearchBarVisible = true
        searchBarCanMoveDown = true
        searchBarCanMoveUp = true
        return params?.apply {
            topMargin += delta.toInt()
        }
    }

    private fun dragSearchNegativeDeltaSearchBarVisible(
        params: LayoutParams?,
        delta: Float
    ): LayoutParams? {
        searchBarLinearLayout.y += delta.toInt()
        isSearchBarVisible = true
        searchBarCanMoveDown = true
        searchBarCanMoveUp = true
        return params?.apply {
            topMargin += delta.toInt()
        }
    }

    private fun dragSearchNegativeDeltaSearchBarNotVisible(params: LayoutParams?): LayoutParams? {
        searchBarLinearLayout.y = -searchHeight.toFloat()
        isSearchBarVisible = false
        searchBarCanMoveDown = true
        searchBarCanMoveUp = false
        return params?.apply {
            topMargin = 0
        }
    }

    private fun moveSearchToVisible() {
        val recyclerViewLayoutParams = recyclerView.layoutParams as? LayoutParams

        val startY = searchBarLinearLayout.y
        val endY = 0f

        val modalAnimator =
            ObjectAnimator.ofFloat(searchBarLinearLayout, "translationY", startY, endY)

        val totalMovement = searchBarLinearLayout.height
        val remainingMovement = -searchBarLinearLayout.y.toInt()

        val duration = if(totalMovement!=0) (ANIM_DURATION / totalMovement * remainingMovement) else 0

        modalAnimator.duration = if (duration > 0) duration else 0
        modalAnimator.addUpdateListener {
            val topMargin = searchBarLinearLayout.y.toInt() + searchBarLinearLayout.height
            recyclerViewLayoutParams?.topMargin = topMargin
            recyclerView.layoutParams = recyclerViewLayoutParams
        }
        modalAnimator.start()
    }

    private fun moveSearchBarToHide() {
        val recyclerViewLayoutParams = recyclerView.layoutParams as? LayoutParams

        val startY = searchBarLinearLayout.y
        val endY = -searchBarLinearLayout.height.toFloat()

        val modalAnimator =
            ObjectAnimator.ofFloat(searchBarLinearLayout, "translationY", startY, endY)

        val totalMovement = searchBarLinearLayout.height
        val remainingMovement = searchBarLinearLayout.height - searchBarLinearLayout.y.toInt()

        val duration = ANIM_DURATION / totalMovement * remainingMovement

        modalAnimator.duration = if (duration > 0) duration else 0
        modalAnimator.addUpdateListener {
            val topMargin = searchBarLinearLayout.y.toInt() + searchBarLinearLayout.height
            recyclerViewLayoutParams?.topMargin = topMargin
            recyclerView.layoutParams = recyclerViewLayoutParams
        }
        modalAnimator.start()
    }

    private fun parseStyleAttrs(attrs: AttributeSet?) {
        val a = context.theme
            .obtainStyledAttributes(attrs, R.styleable.HiddenSearchWithRecyclerView, 0, 0)

        try {
            visibleAtInit =
                a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_visibleAtInit, false)
            hideAtScroll = a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_hideAtScroll, true)
            scrollToBottomBeforeHide = a.getBoolean(
                R.styleable.HiddenSearchWithRecyclerView_scrollToBottomBeforeHide,
                false
            )
            scrollToTopBeforeShow =
                a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_scrollToTopBeforeShow, false)
            filterWhileTyping =
                a.getBoolean(R.styleable.HiddenSearchWithRecyclerView_filterWhileTyping, true)
        } finally {
            a.recycle()
        }
    }

}

