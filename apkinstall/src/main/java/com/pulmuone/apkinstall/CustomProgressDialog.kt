package com.pulmuone.apkinstall

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.progressindicator.LinearProgressIndicator

class CustomProgressDialog constructor(context: Context, private val message: String, private val title: String? = "업데이트"): Dialog(context) {
    val TAG = "CustomProgressDialog"
    //val progressBar = ProgressBar(context)
    private var progressBar = LinearProgressIndicator(context)
    private val tvText = TextView(context)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)

        setCancelable(false)
        setCanceledOnTouchOutside(false)

        val llPadding = 30
        val ll = LinearLayout(context)
        ll.orientation = LinearLayout.VERTICAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        //타이틀
        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        llParam.bottomMargin = 100
        val tvTitle = TextView(context)
        tvTitle.text = title
        tvTitle.gravity = Gravity.LEFT
        tvTitle.setTextColor(Color.parseColor("#000000"))
        tvTitle.textSize = 20.toFloat()
        tvTitle.layoutParams = llParam
        ll.addView(tvTitle)

        //프로그레스바
        //val progressBar = ProgressBar(context)
        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        llParam.bottomMargin = 10
        progressBar.setIndicatorColor(context.getColor(R.color.pulmuone_light_green)) // 실제 프로그레스바 색상
        progressBar.trackColor = context.getColor(com.google.android.material.R.color.material_grey_300) // 인디케이터 배경색
        progressBar.trackThickness = 20
        progressBar.trackCornerRadius = 100
        progressBar.isIndeterminate = true
        //progressBar.scaleY = 3F
        //progressBar.setPadding(0, 0, 0, 0)
        progressBar.layoutParams = llParam
        progressBar.progress
        ll.addView(progressBar)

        //다운로드 상태 표시
        llParam = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llParam.gravity = Gravity.CENTER
        //llParam.topMargin = 10
        //llParam.bottomMargin = 10
        //val tvText = TextView(context)
        tvText.text = message
        tvText.gravity = Gravity.RIGHT
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 15.toFloat()
        tvText.layoutParams = llParam
        ll.addView(tvText)

        setContentView(ll)

        dialogResize(context, this,0.9f, 0.20f)

        window?.attributes?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//        wmlp?.gravity = Gravity.CENTER
//        window?.attributes = wmlp
    }

    // 디바이스 크기 비율로 조절, 참조: https://ryan94.tistory.com/6
    private fun dialogResize(context: Context, dialog: Dialog, width: Float, height: Float){
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R /*30*/){
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val window = dialog.window
            val x = (size.x * width).toInt()
            val y = (size.y * height).toInt()
            window?.setLayout(x, y)
        }else{
            val rect = windowManager.currentWindowMetrics.bounds
            val window = dialog.window
            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()
            window?.setLayout(x, y)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateProgress(downloaded: Int, totalSize: Int, percent: Int) {
        Log.d(TAG, "downloaded: $downloaded, totalSize: $totalSize, percent: $percent")
        progressBar.setProgressCompat(percent, true)
        tvText.text = "${downloaded/1024/1024}MB / ${totalSize/1024/1024}MB"
    }
}