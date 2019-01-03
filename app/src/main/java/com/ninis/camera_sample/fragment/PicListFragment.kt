package com.ninis.camera_sample.fragment

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.transition.Fade
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.ninis.camera_sample.DEF_DIR_PATH
import com.ninis.camera_sample.R
import com.ninis.camera_sample.SpacesItemDecoration
import kotlinx.android.synthetic.main.fragment_pic_list.*
import kotlinx.android.synthetic.main.layout_grid_item.view.*
import java.io.File
import java.util.*

class PicListFragment : Fragment() {

    private val MOVE_DEFAULT_TIME: Long = 1000
    private val FADE_DEFAULT_TIME: Long = 300

    private val listAdapter: PicListAdapter by lazy {
        PicListAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_pic_list, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLayout()
        loadFileList()
    }

    private fun initLayout() {
        rv_list.run {
            val gridLayoutManager = GridLayoutManager(activity, 3)
            gridLayoutManager.orientation = GridLayoutManager.VERTICAL

            val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, 1)
//            staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            staggeredGridLayoutManager.orientation = StaggeredGridLayoutManager.VERTICAL

            layoutManager = staggeredGridLayoutManager
            adapter = listAdapter
            addItemDecoration(SpacesItemDecoration())
        }

        listAdapter.setOnAdapterEvent(object: PicListAdapter.OnAdapterEvent {
            override fun onClickedItem(clickView: View, fileInfo: File) {
                showDetailFragment(clickView, fileInfo)
            }
        })
    }

    private fun loadFileList() {
        val dirInfo = File(DEF_DIR_PATH)
        if( dirInfo.exists() ) {
            listAdapter.setItems(dirInfo.listFiles().toList() as ArrayList<File>)
        }
    }

    fun showDetailFragment(clickView: View, fileInfo: File) {
        val fragment = PicDetailFragment()
        fragment.setSelectedFile(fileInfo)

        val fragmentTransaction = fragmentManager?.beginTransaction()

        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition

            // 1. Exit for Previous Fragment
//            val exitFade = Fade()
//            exitFade.setDuration(FADE_DEFAULT_TIME)
//            setExitTransition(exitFade)
//
//            // 2. Shared Elements Transition
//            val enterTransitionSet = TransitionSet()
//            enterTransitionSet.addTransition(TransitionInflater.from(activity).inflateTransition(android.R.transition.move))
//            enterTransitionSet.setDuration(MOVE_DEFAULT_TIME)
//            enterTransitionSet.setStartDelay(FADE_DEFAULT_TIME)
//            fragment.setSharedElementEnterTransition(enterTransitionSet)
//
//            // 3. Enter Transition for New Fragment
//            val enterFade = Fade()
//            enterFade.startDelay = MOVE_DEFAULT_TIME + FADE_DEFAULT_TIME
//            enterFade.duration = FADE_DEFAULT_TIME
//            fragment.setEnterTransition(enterFade)

            ViewCompat.setTransitionName(clickView, getString(R.string.transition_name_img))
        }

        fragmentTransaction!!
                .addToBackStack(PicDetailFragment::class.java.name)
                .addSharedElement(clickView, getString(R.string.transition_name_img))
                .add(R.id.rl_fragment_stack, fragment, PicDetailFragment::class.java.name)
                .commitAllowingStateLoss()
    }

    private class PicListAdapter : RecyclerView.Adapter<PicItemViewHolder>(), View.OnClickListener {
        interface OnAdapterEvent {
            fun onClickedItem(clickView: View, fileInfo: File)
        }

        private var onAdapterEvent: OnAdapterEvent? = null

        private val itemsPic: ArrayList<File> = ArrayList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicItemViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_grid_item, parent, false)
            itemView.setOnClickListener(this)
            return PicItemViewHolder(itemView)
        }

        override fun getItemCount(): Int {
            return itemsPic.size
        }

        override fun onBindViewHolder(holder: PicItemViewHolder, position: Int) {
            holder.bindView(itemsPic[position])
            holder.itemView.tag = position
        }

        fun setItems(items: ArrayList<File>) {
            if( itemsPic.isNotEmpty() )
                itemsPic.clear()

            itemsPic.addAll(items)
            notifyDataSetChanged()
        }

        fun setOnAdapterEvent(event: OnAdapterEvent) {
            onAdapterEvent = event
        }

        override fun onClick(view: View?) {
            if( view?.tag != null ) {
                val position = view?.getTag()
                val fileInfo = itemsPic[position as Int]
                onAdapterEvent!!.onClickedItem(view, fileInfo)
            }
        }
    }

    private class PicItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

        private var selectedFile: File? = null

        fun bindView(fileInfo: File) {
            selectedFile = fileInfo

            Glide.with(itemView)
                    .load(fileInfo)
                    .into(itemView.iv_gird_item_img)

            val date = Date(fileInfo.lastModified())
            itemView.tv_gird_item_date.text = date.toString()
        }
    }
}