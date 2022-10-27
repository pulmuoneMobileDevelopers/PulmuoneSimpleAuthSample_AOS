package com.android.pulmuone.sample.ui.main

import android.Manifest
import android.app.Activity
import android.content.Context
import com.android.pulmuone.sample.utils.Constants.BIO_AUTH
import com.android.pulmuone.sample.utils.Constants.KEY_INTERVAL
import com.android.pulmuone.sample.utils.Constants.KEY_MAIN
import com.android.pulmuone.sample.utils.Constants.KEY_PIN_CODE_EMPTY
import com.android.pulmuone.sample.utils.Constants.PIN_AUTH
import com.android.pulmuone.sample.utils.Constants.PIN_CODE_STATUS
import com.android.pulmuone.sample.utils.Constants.SIMPLE_AUTH_STATUS

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.pulmuone.sample.R
import com.android.pulmuone.sample.databinding.ActivityMainBinding
import com.pulmuone.biomodule.aos.BioManager.Companion.getInstance
import com.android.pulmuone.sample.utils.PreferenceManager
import com.android.pulmuone.sample.ui.bio.BioModuleActivity
import com.android.pulmuone.sample.ui.pin.view.PinModuleActivity
import com.android.pulmuone.sample.ui.auth.AuthenticationMethodActivity
import com.android.pulmuone.dialog.DefaultDialog
import com.android.pulmuone.sample.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.pulmuone.permission.*
import com.pulmuone.toast.aos.GentleToast

class MainActivity : AppCompatActivity() {

    var statusAuth: String? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var preference: SharedPreferences

    private lateinit var requestPermission: String // 유저에게 요청하고 있는 퍼미션 데이터

    private var defaultDialog: DefaultDialog? = null

    override fun onResume() {
        super.onResume()

        Log.d("MainActivity", "onResume()")
        val interval: Long = PreferenceManager().getLong(this, KEY_INTERVAL)

        /*
            백그라운드 전환 시간이 10초 이상일 경우 체크해서 간편인증 화면으로 이동
         */
        if (interval > 10) {
            if (PreferenceManager().getString(this, SIMPLE_AUTH_STATUS) == BIO_AUTH) {
                startActivity(
                    Intent(this, BioModuleActivity::class.java).putExtra(
                        "from",
                        KEY_MAIN
                    )
                )
                overridePendingTransition(0, 0)
                PreferenceManager().setLong(this, KEY_INTERVAL, 0)
                finish()
            } else if (PreferenceManager().getString(this, SIMPLE_AUTH_STATUS) == PIN_AUTH) {
                startActivity(
                    Intent(this, PinModuleActivity::class.java).putExtra(
                        "from",
                        KEY_MAIN
                    )
                )
                overridePendingTransition(0, 0)
                PreferenceManager().setLong(this, KEY_INTERVAL, 0)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        statusAuth = PreferenceManager().getString(this, SIMPLE_AUTH_STATUS)
        assert(statusAuth != null)
        if (statusAuth == "") PreferenceManager().setString(this, SIMPLE_AUTH_STATUS, "none")
        statusAuth = PreferenceManager().getString(this, SIMPLE_AUTH_STATUS)

        preference = getPreferences(Context.MODE_PRIVATE)

        setButtonEvent()

        // 권한 안내 팝업 호출
        showOneTimeNoticePopup {
            startPermissionLoop(0)
        }
    }

    // 모든 필수 권한 허용 완료 후 동작
    private fun completeAllGrant() {
        showToast("필수권한 모두 허용 상태 다음 동작이 있으면 수행")
    }

    // 버튼 리스너 등록
    private fun setButtonEvent() {
        binding.apply {
            btContact.setOnClickListener {
                if (requestRequirePermission(Manifest.permission.READ_CONTACTS)) {
                    showToast("연락처 읽기 정상 가동")
                }
            }
            btLocation.setOnClickListener {
                if (requestRequirePermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showToast("위치 정보 정상 가동")
                }
            }
            btCellPhone.setOnClickListener {
                if (requestRequirePermission(Manifest.permission.CALL_PHONE)) {
                    showToast("통화 걸기 정상 가동")
                }
            }
            btFile.setOnClickListener {
                if (requestRequirePermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showToast("내부저장소 파일 읽기 정상 가동")
                }
            }
            btUnknownAllow.setOnClickListener {
                if (requestRequirePermission(PermissionConstants.UNKNOWN_ALLOW_INSTALL)) {
                    showToast("출처를 알 수 없는 앱 허용")
                }
            }

            btCamera.setOnClickListener {
                if (requestOptionalPermission(Manifest.permission.CAMERA)) {
                    showToast("카메라 정상 가동")
                }
            }
            btnTest.setOnClickListener {
                /*
                    로그인 버튼 클릭 - 간편인증 설정 화면으로 이동 (로그아웃 시 간편인증 초기화)
                 */
                statusAuth = PreferenceManager().getString(this@MainActivity, SIMPLE_AUTH_STATUS)
                when(statusAuth) {
                    BIO_AUTH -> {
                        startActivity(Intent(this@MainActivity, BioModuleActivity::class.java).putExtra("from",
                            Constants.KEY_INTRO
                        ))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    PIN_AUTH -> {
                        startActivity(Intent(this@MainActivity, PinModuleActivity::class.java).putExtra("from",
                            Constants.KEY_INTRO
                        ))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                    else -> {
                        startActivity(Intent(this@MainActivity, AuthenticationMethodActivity::class.java))
                        overridePendingTransition(0, 0)
                        finish()
                    }
                }
            }
            btnReset.setOnClickListener {
                PreferenceManager().setString(this@MainActivity, SIMPLE_AUTH_STATUS, "")
                PreferenceManager().setString(this@MainActivity, PIN_CODE_STATUS, KEY_PIN_CODE_EMPTY)
                val bioManager = getInstance(this@MainActivity)
                bioManager.removeBiometrics()
                startActivity(Intent(this@MainActivity, AuthenticationMethodActivity::class.java))
                overridePendingTransition(0, 0)
                finish()
            }
        }
    }

    /**
     * 권한 안내 팝업 (최초 1회)
     * 원래는 최초 1회 였는데 필수 허용이 안되는 경우는 항상 1회씩 보여주도록 설정
     * @param afterAction 함수가 끝난 후 동작할 액션 블록
     */
    private fun showOneTimeNoticePopup( afterAction: () -> Unit ) {
        if (!isPermissionAllGranted(requirePermissionArray)) {
            showNoticeBottomSheet {
                afterAction.invoke()
            }
        } else {
            afterAction.invoke()
        }

        //FIXME: - 앱 켜지고 딱 한번만 호출하고 싶은경우는 아래 로직 사용
//        val isFirst = preference.getBoolean(isFirstShowPermissionPopup, true)
//        if (isFirst) {
//            preference.edit().putBoolean(isFirstShowPermissionPopup, false).apply()
//
//            showNoticeBottomSheet {
//                afterAction.invoke()
//            }
//        } else {
//            afterAction.invoke()
//        }
    }

    /**
     * 필수 권한 Loop 시작
     */
    private fun startPermissionLoop(count: Int) {
        if (count < requirePermissionArray.size) {
            requestRequirePermission(requirePermissionArray[count])
        } else {
            completeAllGrant()
        }
    }

    /**
     * (필수 권한) 권한 허용 여부를 묻고, 허용 요청함
     * 필수의 경우는 두번 까지 팝업으로 요청 후 거절 시에 앱을 종료시킨다.
     */
    private fun requestRequirePermission(permission: String): Boolean {
        requestPermission = permission
        return when(getGrantedStatus(permission, preference)){
            PermissionStatus.FIRST, PermissionStatus.SECOND -> {
                requireResult.launch(permission)
                false
            }
            PermissionStatus.DENY -> {
                deniedRequirePermission(permission)
                false
            }
            PermissionStatus.GRANT -> {
                startPermissionLoop(requirePermissionArray.indexOf(permission) + 1)
                true
            }
        }
    }
    // 필수 권한 요청 result
    private val requireResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            requestRequirePermission(requestPermission)
        }

    /**
     * (선택 권한) 권한 허용 여부를 묻고, 허용 요청함
     * 선택의 경우는 한번만 물어보고 그 다음부터는 SnackBar 로 대체
     */
    private fun requestOptionalPermission(permission: String): Boolean {
        requestPermission = permission
        return when(getGrantedStatus(permission, preference)){
            PermissionStatus.FIRST -> {
                optionalResult.launch(permission)
                false
            }
            PermissionStatus.SECOND, PermissionStatus.DENY -> {
                showSnackBar(permission)
                false
            }
            PermissionStatus.GRANT -> true
        }
    }
    // 선택 권한 요청 result
    private val optionalResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            requestOptionalPermission(requestPermission)
        }

    /**
     * 필수 권한 모두 거부 시
     */
    private fun deniedRequirePermission(permission: String) {
        when (permission) {
            PermissionConstants.UNKNOWN_ALLOW_INSTALL -> {
                showAlertDialog(
                    title = "[출처를 알 수 없는 앱 설치]",
                    msg = "출처를 알수 없는 앱 설치를 허용해야 서비스가 정상 이용이 가능합니다. 허용 항목으로 이동합니다.",
                    posiBtn = "설정",
                ) {
                    moveAllowUnknownSetting()
                    finish()
                }
            }
            PermissionConstants.APP_UP_APP -> {
                //TODO: - 구현하기
            }
            PermissionConstants.PICTURE_IN_PICTURE -> {
                //TODO: - 구현하기
            }
            else -> {
                showAlertDialog(
                    title = "[필수권한 허용 요청]",
                    msg = "필수 권한을 허용해야 서비스가 정상 이용이 가능합니다. 권한 요청시 반드시 허용해 주세요.\n\n 필수권한 [${this.defaultPermissionData(permission).mainText}]",
                    posiBtn = "확인",
                ) {
                    setDetailSettingIntent()
                    finish()
                }
            }
        }
    }

    private fun showAlertDialog(title: String, msg: String, posiBtn: String, block: () -> Unit) {
        defaultDialog = DefaultDialog(
            this,
            title,
            msg,
            true,
            _ok = {},
            _cancel = {},
            _singleOk = {
                block.invoke()
            },
            "",
            "",
            posiBtn
        )
        if (!(this as Activity).isFinishing) defaultDialog?.show()
    }

    /**
     * 권한 안내 bottomSheet Show
     */
    private fun showNoticeBottomSheet(afterAction: () -> Unit) {

        val requireList = ArrayList<PermissionData>() // 필수권한 리스트
        val optionalList = ArrayList<PermissionData>() // 선택권한 리스트
        classifyPermissionGroup(requirePermissionArray).forEach {
            requireList.add(
                this.defaultPermissionData(it)
            )
        }
        classifyPermissionGroup(optionalPermissionArray, requirePermissionArray).forEach {
            optionalList.add(
                this.defaultPermissionData(it)
            )
        }
        // 선택 권한 리스트에 빈 내용 추가
        optionalList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_vector_smile,
                mainText = "그냥 알림",
                description = "그냥 안내하고 싶을 때는 이렇게 추가로 사용",
                permission = PermissionConstants.PASS_PERMISSION
            )
        )

        // 권한 안내 팝업 호출
        PermissionFragment().showBottomSheet(
            supportFragmentManager,
            PermissionFragmentData(
                requirePermissionList = requireList,
                optionalPermissionList = optionalList,
                howToSettingText = "설정 > 애플리케이션 > ${getString(R.string.app_name)} > 권한 > 허용",
                onClick = {
                    afterAction.invoke()
                }
            )
        )
    }

    // Toast Message 호출
    private fun showToast(str: String) {
        GentleToast.with(this)
            .shortToast(str)
            .setTextColor(R.color.colorToastText)
            .setBackgroundColor(R.color.colorToastBg)
            .setBackgroundRadius(4)
            .setTextSize(14)
            .setAlpha(153) // 60%
            .show()
    }

    // SnackBar 호출
    private fun showSnackBar(permission: String) {
        val snackBar = Snackbar.make(binding.clMainView, this.defaultPermissionData(permission).mainText + " 권한 요청을 허용해주세요.", Snackbar.LENGTH_LONG)
        snackBar.setAction("설정") {
            when (permission) {
                PermissionConstants.UNKNOWN_ALLOW_INSTALL -> {
                    moveAllowUnknownSetting()
                }
                PermissionConstants.APP_UP_APP -> {
                    //TODO: - 구현하기
                }
                PermissionConstants.PICTURE_IN_PICTURE -> {
                    //TODO: - 구현하기
                }
                else -> {
                    setDetailSettingIntent()
                }
            }
        }
        snackBar.show()
    }

    companion object {
        private const val TAG = "PermissionActivity"

        private const val isFirstShowPermissionPopup = "isFirstShowPermissionPopup" // 최초 권한 안내 팝업

        /**
         * 필수 권한 리스트
         * */
        private val requirePermissionArray: Array<String> = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            PermissionConstants.UNKNOWN_ALLOW_INSTALL,
        )

        /**
         * 선택 권한 리스트
         * */
        private val optionalPermissionArray: Array<String> = arrayOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CAMERA,
            Manifest.permission.RECEIVE_MMS,
        )
    }
}