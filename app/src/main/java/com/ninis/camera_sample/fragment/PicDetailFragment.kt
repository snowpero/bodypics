package com.ninis.camera_sample.fragment

import android.os.Build
import android.os.Bundle
import android.support.transition.TransitionInflater
import android.support.transition.TransitionSet
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ninis.camera_sample.R
import kotlinx.android.synthetic.main.fragment_pic_detail.*
import java.io.File

class PicDetailFragment: Fragment() {
    private var selectedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(activity).inflateTransition(android.R.transition.move))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_pic_detail, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iv_pic_detail_img.transitionName = getString(R.string.transition_name_img)
        }

        loadImage()
    }

    public fun setSelectedFile(file: File) {
        selectedFile = file
    }

    private fun loadImage() {
        if( selectedFile != null ) {
            Glide.with(this)
                    .load(selectedFile)
                    .into(iv_pic_detail_img)
        }
    }
}