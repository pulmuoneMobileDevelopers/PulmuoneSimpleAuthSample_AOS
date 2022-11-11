package com.pulmuone.apkinstall

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*


object ApkInstall {
    val TAG = "ApkInstall"

    /**
     * 원격 APK를 다운로드를 하는 메소드
     * @param activity AppCompatActivity 객체
     * @param downloadUrl 다운로드 할 APK URL
     * @param downloadSuccessToastMesage APK 다운로드 성공시 SnackBar에 보여줄 문구
     * @param downloadFailToastMesage APK 다운로드 실패시 SnackBar에 보여줄 문구
     */
    fun downloadApkAndInstall(activity: AppCompatActivity, downloadUrl: String, fileName: String, downloadSuccessToastMesage: String? = "앱 다운로드 완료", downloadFailToastMesage: String? = "에러! 다시 시도하세요.") {
//        val dialog = setProgressDialog(activity, "Downloading...")// Dialog(activity)
        val dialog = CustomProgressDialog(activity, "앱 다운로드 중...")

        @Suppress("BlockingMethodInNonBlockingContext")
        this.executeAsyncTask<Boolean, Int, Int, Int>(
            onPreExecute = {
                // Do something before heavy task
//                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 배경을 투명하게
//                dialog.setContentView(ProgressBar(activity.applicationContext)) // ProgressBar 위젯 생성
                dialog.setCanceledOnTouchOutside(false) // 외부 터치 막음
                dialog.setCancelable(false)
//                dialog.setOnCancelListener { activity.finish() } // 뒤로가기시 현재 액티비티 종료
                dialog.show()
            },
            doInBackground = { publishProgress ->
                // Do some heavy works. Dont update ui here. Call publish progress for updating ui.
                var flag = false

                try {
                    val path = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
                    val outputFile = File("$path$fileName.apk")
                    Log.d(TAG, "outputFile: $outputFile")

                    /*
                    var repetition = 1
                    while (outputFile.exists()) {
                        outputFile = File("$path$fileName ($repetition).apk")
                        repetition++
                    }
                    */

                    var isDeleted = false
                    // 기존 파일 존재시 삭제하고 다운로드
                    if (outputFile.exists()) {
                        isDeleted = outputFile.delete()
                        Log.d(TAG, "isDeleted:$isDeleted")
                    }

                    val directory = File(path)
                    if (!directory.exists()) {
                        directory.mkdirs()
                    }

                    //URL Connection SSL ignore 적용
                    val sc = SSLContext.getInstance("SSL")
                    sc.init(null, createTrustManagers(), SecureRandom())
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
                    val allHostsValid = HostnameVerifier { hostname: String?, session: SSLSession? -> true }
                    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)

                    val url = URL(downloadUrl)
                    Log.d(TAG, "downloadUrl:$downloadUrl")
                    Log.d(TAG, "url:$url")

                    val c = url.openConnection() as HttpsURLConnection
                    c.requestMethod = "GET"
                    c.connect()

                    val fos = FileOutputStream(outputFile)
                    val inputStream = c.inputStream
                    val totalSize = c.contentLength.toFloat() //size of apk
                    Log.d(TAG, "totalSize:${totalSize.toInt()}")

                    val buffer = ByteArray(1024)
                    var len1: Int
                    var per: Float
                    var downloaded = 0f
                    while (inputStream.read(buffer).also { len1 = it } != -1) {
                        fos.write(buffer, 0, len1)
                        downloaded += len1
                        Log.d(TAG, "downloaded:${downloaded.toInt()}")
                        per = (downloaded * 100 / totalSize)
                        publishProgress(downloaded.toInt(), totalSize.toInt(), per.toInt())
                    }
                    fos.close()
                    inputStream.close()
                    openNewVersion(activity, outputFile.path)
                    flag = true
                } catch (e: MalformedURLException) {
                    Log.e(TAG, "Update Error: " + e.message)
                    flag = false
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.e(TAG, "IOException: " + e.message)
                }

                return@executeAsyncTask flag
            },
            onProgressUpdate = { downloaded, totalSize, percent ->
                // Update ui here
                //it.toString()
                dialog.updateProgress(downloaded, totalSize, percent)
                val msg = if (percent > 99) "Finishing... " else "Downloading... $percent%"
                Log.d(TAG, msg)
            },
            onPostExecute = {
                // Do something on the end
                dialog.dismiss()

                // 스낵바로 다운로드 성공 or 실패 표시
                val snackbar =
                    (if(it) downloadSuccessToastMesage else downloadFailToastMesage)?.let { it1 ->
                        Snackbar.make(activity.findViewById(R.id.content), it1, Toast.LENGTH_SHORT)
                    }

                val view = snackbar?.view
                val tv: TextView? = view?.findViewById(com.google.android.material.R.id.snackbar_text)
                tv?.textAlignment = View.TEXT_ALIGNMENT_CENTER
                snackbar?.show()
            }
        )
    }

    @SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
    private fun createTrustManagers(): Array<TrustManager> {
        return arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(
                x509Certificates: Array<X509Certificate?>?,
                s: String?
            ) {
            }

            override fun checkServerTrusted(
                x509Certificates: Array<X509Certificate?>?,
                s: String?
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOf()
            }
        })
    }

    private fun openNewVersion(activity: AppCompatActivity, location: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(
            getUriFromFile(activity, location),
            "application/vnd.android.package-archive"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(intent)
//        Handler(Looper.getMainLooper()).postDelayed({
//            activity.finish()
//        }, 1000)
    }

    private fun getUriFromFile(context: Context, filePath: String): Uri {
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            File(filePath)
        )
    }

    private fun <R, P1, P2, P3> executeAsyncTask (
        // Get functions
        onPreExecute: () -> Unit,
        doInBackground: suspend (suspend (P1, P2, P3) -> Unit) -> R,
        onProgressUpdate: (P1, P2, P3) -> Unit,
        onPostExecute: (R) -> Unit
    ) {
        // Run in Coroutine scope
        CoroutineScope(Dispatchers.Main).launch {
            // Run PreExecute
            onPreExecute()
            // Then run doInBackground on IO thread and pass data to result
            val result = withContext(Dispatchers.IO) {
                doInBackground { downloaded: P1, totalSize: P2, percent: P3 ->
                    // Run onProgress update in main thread
                    withContext(Dispatchers.Main) { onProgressUpdate(downloaded, totalSize, percent) }
                }
            }
            // On the last run onPostExecute
            onPostExecute(result)
        }
    }

    /**
     * 출처를 알 수 없는 앱 허용 여부를 리턴하는 메소드
     * @param activity AppCompatActivity 객체
     * @param message 허용하지 않았다면 AlertDialog에 message로 표시해줄 문구
     * @return 출처를 알 수 없는 앱 허용 여부 true/false
     */
    fun checkAppFromUnknownSource(activity: AppCompatActivity, message: String = "앱 업데이트를 위해 스마트폰 환경설정의 '앱 설치 허용'을 설정해 주시기 바랍니다."): Boolean {
        Log.d(TAG, "checkAppFromUnknownSource")
        var isTrue = false

        //오레오(8.0) 이후
        isTrue = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O /*26*/) {
            val packageManager = activity.packageManager
            packageManager.canRequestPackageInstalls() //알 수 없는 출처의 앱 설치 권한 확인
        }
        //오레오(8.0) 이전
        else {
            Settings.Secure.getInt(activity.contentResolver, Settings.Global.STAY_ON_WHILE_PLUGGED_IN) == 1
        }

        Log.d(TAG, "checkAppFromUnknownSource isTrue: $isTrue")

        if (!isTrue) {
            val alertDialog = AlertDialog.Builder(activity)//, android.R.style.Theme_DeviceDefault_Light_Dialog)
            alertDialog.setTitle("알림")
            alertDialog.setMessage(message)
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton("설정하기") { dialog, which ->
                moveAllowUnknownSetting(activity)
            }
            /*
            alertDialog.setNegativeButton("건너띄기") { dialog, which ->

            }
            */
            alertDialog.show()
        }

        return isTrue
    }

    /**
     * 출처를 알 수 없는 앱 허용 설정 화면으로 이동
     */
    private fun moveAllowUnknownSetting(activity: AppCompatActivity) {
        // 오레오(8.0) 이전
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O /*26*/) {
            activity.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
        // 오래오(8.0) 이후
        else {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivity(intent)
        }
    }
}