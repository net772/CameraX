package com.example.camerax

import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.camerax.adapter.ImageViewPagerAdapter
import com.example.camerax.databinding.ActivityImageListBinding
import com.example.camerax.databinding.ActivityMainBinding
import com.example.camerax.util.PathUtil
import java.io.File
import java.io.FileNotFoundException

class ImageListActivity  : AppCompatActivity() {
    companion object {
        const val URI_LIST_KEY = "uriList"

        const val IMAGE_LIST_REQUEST_CODE = 100

        fun newIntent(activity: Activity, uriList: List<Uri>) =
            Intent(activity, ImageListActivity::class.java).apply {
                putExtra(URI_LIST_KEY, ArrayList<Uri>().apply { uriList.forEach { add(it) } })
            }
    }

    private var currentUri: Uri? = null
    private lateinit var imageViewPagerAdapter: ImageViewPagerAdapter
    private val uriList by lazy<List<Uri>> { intent.getParcelableArrayListExtra(URI_LIST_KEY)!! }

    private var mBinding: ActivityImageListBinding? = null

    //매번 null 체크를 할 필요 없이 편의성을 위해 바인딩 변수 재 선언
    private val binding get() = mBinding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 자동 생성된 뷰 바인딩 클래스에서의 inflate라는 메서드를 활용해서
        // 액티비티에서 사용할 바인딩 클래스의 인스턴스 생성
        mBinding = ActivityImageListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        setSupportActionBar(binding.toolbar)
        setupImageList()
    }

    private fun setupImageList() = with(binding) {
        if (::imageViewPagerAdapter.isInitialized.not()) {
            imageViewPagerAdapter = ImageViewPagerAdapter(uriList.toMutableList())
        }
        imageViewPager.adapter = imageViewPagerAdapter
        indicator.setViewPager(imageViewPager)
        imageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                toolbar.title = if (imageViewPagerAdapter.uriList.isNotEmpty()) {
                    currentUri = imageViewPagerAdapter.uriList[position]
                    getString(R.string.images_page, position + 1, imageViewPagerAdapter.uriList.size)
                } else {
                    currentUri = null
                    ""
                }
            }
        })
        deleteButton.setOnClickListener {
            currentUri?.let { uri ->
                removeImage(uri)
            }
        }
    }

    private fun removeImage(uri: Uri) {
      //  try {
            val file = File(PathUtil.getPath(this, uri) ?: throw FileNotFoundException())
           // file.delete()
            val removedIndex = imageViewPagerAdapter.uriList.indexOf(uri)
            imageViewPagerAdapter.uriList.removeAt(removedIndex)
            imageViewPagerAdapter.notifyItemRemoved(removedIndex)
            binding.indicator.setViewPager(binding.imageViewPager)

            if (imageViewPagerAdapter.uriList.isNotEmpty()) {
                currentUri = if (removedIndex == 0) {
                    imageViewPagerAdapter.uriList[removedIndex]
                } else {
                    imageViewPagerAdapter.uriList[removedIndex - 1]
                }
            }
            MediaScannerConnection.scanFile(this, arrayOf(file.path), arrayOf("image/jpeg"), null)

            binding.indicator.setViewPager(binding.imageViewPager)

            if (imageViewPagerAdapter.uriList.isEmpty()) {
                Toast.makeText(this, "삭제할 수 있는 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
        }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(URI_LIST_KEY, ArrayList<Uri>().apply { imageViewPagerAdapter.uriList.forEach { add(it) } })
        })
        finish()
    }
}