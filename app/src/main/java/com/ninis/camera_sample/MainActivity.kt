package com.ninis.camera_sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.github.florent37.camerafragment.CameraFragment
import com.github.florent37.camerafragment.configuration.Configuration
import com.github.florent37.camerafragment.listeners.CameraFragmentResultAdapter
import com.github.florent37.camerafragment.listeners.CameraFragmentStateListener
import com.jaeger.library.StatusBarUtil
import com.ninis.camera_sample.data.SavePicItem
import com.ninis.camera_sample.fragment.PicCalendarFragment
import com.ninis.camera_sample.fragment.PicGalleryFragment
import com.ninis.camera_sample.fragment.PicListFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = MainActivity::class.java.simpleName

    private val rxPermission: RxPermissions by lazy {
        RxPermissions(this)
    }

    private val mRealm = Realm.getDefaultInstance()

    private lateinit var cameraFragment: CameraFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StatusBarUtil.setColor(this@MainActivity, Color.TRANSPARENT)

        showCameraView()
        checkBeforePicture()
    }

    @SuppressLint("MissingPermission")
    private fun showCameraView() {
        rxPermission
                .requestEach(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .subscribe {
                    if (it.granted) {
                        cameraFragment = CameraFragment.newInstance(Configuration.Builder().build())
                        cameraFragment.setStateListener(cameraStateListener)
                        supportFragmentManager.beginTransaction()
                                .replace(R.id.rl_fragment_content, cameraFragment, CameraFragment::class.java.name)
                                .commitAllowingStateLoss()

                        initLayout()
                    } else {

                    }
                }
    }

    private fun initLayout() {
        record_button.setOnClickListener(this)
        iv_gallery_show.setOnClickListener(this)
        iv_calendar_show.setOnClickListener(this)
        iv_pic_load_show.setOnClickListener(this)
        iv_pic_list_show.setOnClickListener(this)

        switch_toggle_layer.setOnCheckedChangeListener { _, isOn ->
            if (isOn) {
                iv_transparent_layer.visibility = View.VISIBLE
                seekbar.visibility = View.VISIBLE
                iv_pic_load_show.visibility = View.VISIBLE
            } else {
                iv_transparent_layer.visibility = View.GONE
                seekbar.visibility = View.GONE
                iv_pic_load_show.visibility = View.GONE
            }
        }

        seekbar.progress = 40
        seekbar.setOnProgressChangeListener(object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar?, value: Int, fromUser: Boolean) {
                iv_transparent_layer.alpha = (0.01 * value).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar?) {

            }
        })
    }

    private fun checkBeforePicture() {
        val dir = File(DEF_DIR_PATH)
        if (dir.isDirectory) {
            val fileList = dir.listFiles()

            if (fileList != null && fileList.isNotEmpty()) {
                for (file in fileList) {
                    Log.d(TAG, "file : " + file.absoluteFile)
                }

                val latestFile = fileList.last()

                Glide
                        .with(this@MainActivity)
                        .load(latestFile)
                        .into(iv_transparent_layer)

                switch_toggle_layer.isChecked = true

                checkRealmDB(ArrayList(fileList.toList()))
            }
        } else {
            dir.mkdir()
            switch_toggle_layer.isChecked = false
        }
    }

    private fun checkRealmDB(fileList: ArrayList<File>) {
        if (fileList.isNotEmpty()) {
            for (file in fileList) {
                val findItem = mRealm.where<SavePicItem>().equalTo("date", file.lastModified()).findFirst()
                if (findItem == null) {
                    mRealm.executeTransaction {
                        val picItem = it.createObject<SavePicItem>()
                        picItem.date = file.lastModified()
                        picItem.path = file.path

                        Log.d("DBCheck", "insert DB : " + picItem.toString())
                    }
                }
            }
        }

        // DB Check
        mRealm.where<SavePicItem>().findAll().forEach {
            Log.d("DBCheck", it.toString())
        }
    }

    private fun showPicListFragment() {
        val fragment = PicListFragment()
        supportFragmentManager.beginTransaction()
                .addToBackStack(PicListFragment::class.java.name)
                .replace(R.id.rl_fragment_stack, fragment, PicListFragment::class.java.name)
                .commitAllowingStateLoss()
    }

    private fun showPicGalleryFragment() {
        val fragment = PicGalleryFragment()
        supportFragmentManager.beginTransaction()
                .addToBackStack(PicGalleryFragment::class.java.name)
                .replace(R.id.rl_fragment_stack, fragment, PicGalleryFragment::class.java.name)
                .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        val ft = supportFragmentManager.beginTransaction()

        if (fm.backStackEntryCount > 0) {
            fm.popBackStack()
            ft.commitAllowingStateLoss()
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.record_button -> {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                val imageFileName = "InBodyPic_" + timeStamp

                cameraFragment.takePhotoOrCaptureVideo(object : CameraFragmentResultAdapter() {
                    override fun onVideoRecorded(filePath: String?) {
                    }

                    override fun onPhotoTaken(bytes: ByteArray?, filePath: String?) {
                        Toast.makeText(this@MainActivity, "onPhotoTaken " + filePath, Toast.LENGTH_SHORT).show()
                        Log.d("InBody", "filePath\n" + filePath)

                        galleryAddPic(imageFileName, filePath)
                    }
                },
                        DEF_DIR_PATH,
                        imageFileName)
            }
            R.id.iv_gallery_show -> {
                showPicGalleryFragment()

            }
            R.id.iv_calendar_show -> {
                showCalendarFragment()
            }
            R.id.iv_pic_load_show -> {
                showPhotoSelector()
            }
            R.id.iv_pic_list_show -> {
                showPicListFragment()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this@MainActivity, object : DefaultCallback() {
            override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                Glide.with(this@MainActivity)
                        .load(imageFile)
                        .into(iv_transparent_layer)
            }
        })
    }

    private fun showPhotoSelector() {
        EasyImage.openGallery(this@MainActivity, 0)
    }

    private fun showCalendarFragment() {
        val fragment = PicCalendarFragment()
        supportFragmentManager.beginTransaction()
                .addToBackStack(PicCalendarFragment::class.java.name)
                .replace(R.id.rl_fragment_stack, fragment, PicCalendarFragment::class.java.name)
                .commitAllowingStateLoss()
    }

    private fun galleryAddPic(fileName: String, filePath: String?) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"

        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(filePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        this.sendBroadcast(mediaScanIntent)
    }

    internal inner class SavePhotoTask : AsyncTask<ByteArray, String, String>() {
        override fun doInBackground(vararg jpeg: ByteArray): String? {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"

            val photo = File(Environment.getExternalStorageDirectory(), String.format("%s.jpg", imageFileName))

            if (photo.exists()) {
                photo.delete()
            }

            try {
                val fos = FileOutputStream(photo.path)

                fos.write(jpeg[0])
                fos.close()
            } catch (e: java.io.IOException) {
                Log.e("PictureDemo", "Exception in photoCallback", e)
            }

            return null
        }
    }

    object cameraStateListener : CameraFragmentStateListener {
        override fun onFlashAuto() {

        }

        override fun onFlashOff() {

        }

        override fun onCameraSetupForVideo() {

        }

        override fun onCurrentCameraFront() {

        }

        override fun onRecordStateVideoReadyForRecord() {

        }

        override fun onStopVideoRecord() {

        }

        override fun onFlashOn() {

        }

        override fun onCameraSetupForPhoto() {

        }

        override fun onCurrentCameraBack() {

        }

        override fun onStartVideoRecord(outputFile: File?) {

        }

        override fun shouldRotateControls(degrees: Int) {

        }

        override fun onRecordStateVideoInProgress() {

        }

        override fun onRecordStatePhoto() {

        }

    }
}
