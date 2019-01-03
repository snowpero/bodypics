package com.ninis.camera_sample.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ninis.camera_sample.DEF_DIR_PATH
import com.ninis.camera_sample.R
import com.veinhorn.scrollgalleryview.MediaInfo
import com.veinhorn.scrollgalleryview.loader.MediaLoader
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_pic_gallery.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PicGalleryFragment: Fragment() {

    private val imgFileList: ArrayList<File> = ArrayList()

    private val mRealm = Realm.getDefaultInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_pic_gallery, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadFileList()
        initGallery()
    }

    private fun loadFileList() {
        val dirInfo = File(DEF_DIR_PATH)
        if( dirInfo.exists() ) {
            if( imgFileList.isNotEmpty() )
                imgFileList.clear()

            for( file in dirInfo.listFiles() ) {
                imgFileList.add(file)
            }
        }
    }

    private fun initGallery() {
        val infos: ArrayList<MediaInfo> = ArrayList(imgFileList.size)
        for( file in imgFileList ) {
            infos.add(MediaInfo.mediaLoader(GlideMediaLoader(file.path)))
        }

        scrollgallerview
                .setThumbnailSize(100)
                .setZoom(true)
                .setFragmentManager(activity?.supportFragmentManager)
                .addMedia(infos)
                .addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {

                    }

                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                    }

                    override fun onPageSelected(position: Int) {
                        val date = Date(imgFileList[position].lastModified())
                        val sdf = SimpleDateFormat(getString(R.string.date_format), Locale.KOREA)

                        tv_gallery_item_date.text = sdf.format(date)
                    }
                })
    }

    private class GlideMediaLoader(val url: String): MediaLoader {
        override fun isImage(): Boolean {
            return true
        }

        override fun loadMedia(context: Context, imageView: ImageView, callback: MediaLoader.SuccessCallback) {
            Glide.with(context)
                    .load(url)
                    .into(imageView)
        }

        override fun loadThumbnail(context: Context, thumbnailView: ImageView, callback: MediaLoader.SuccessCallback) {
            Glide.with(context)
                    .load(url)
                    .thumbnail(0.2f)
                    .into(thumbnailView)
        }
    }
}