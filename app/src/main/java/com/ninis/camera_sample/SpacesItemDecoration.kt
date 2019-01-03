package com.ninis.camera_sample

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class SpacesItemDecoration: RecyclerView.ItemDecoration() {
    private val space: Int = 10

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect?.left = space
        outRect?.right = space
        outRect?.bottom = space

        // Add top margin only for the first item to avoid double space between items
        if (parent?.getChildLayoutPosition(view) == 0) {
            outRect?.top = space
        } else {
            outRect?.top = 0
        }
    }
}