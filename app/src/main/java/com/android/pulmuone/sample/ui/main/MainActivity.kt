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
import com.android.pulmuone.sample.ui.dialog.DefaultDialog
import com.android.pulmuone.sample.utils.Constants
import com.google.android.material.snackbar.Snackbar
import com.pulmuone.permission.*
import com.pulmuone.toast.aos.GentleToast

class MainActivity : AppCompatActivity() {

    var statusAuth: String? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var preference: SharedPreferences

    private val requireList = ArrayList<PermissionData>() // 필수권한 리스트
    private val optionalList = ArrayList<PermissionData>() // 선택권한 리스트

    private lateinit var requestPermissionData: PermissionData // 유저에게 요청하고 있는 퍼미션 데이터

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

        setPermissionList()
        setButtonEvent()
        showOneTimeNoticePopup {
            startPermissionLoop(0)
        }
    }

    // 모든 필수 권한 허용 완료 후 동작
    private fun completeAllGrant() {
        showToast("필수권한 모두 허용 상태 다음 동작이 있으면 수행")
    }

    // 권한 목록 리스트 세팅
    private fun setPermissionList() {
        // 필수 권한 리스트
        requireList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_contacts,
                mainText = "스마트폰 내 연락처",
                description = "연락처를 사용하여 친구에게 추천합니다.",
                permission = Manifest.permission.READ_CONTACTS,
            )
        )
        requireList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_location_on,
                mainText = "기기 및 앱 기록",
                description = "통계, 푸시발송, 오류 정보 확인",
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )
        requireList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_call,
                mainText = "전화",
                description = "앱에서 직접 전화를 걸 수 있습니다.",
                permission = Manifest.permission.CALL_PHONE,
            )
        )
        requireList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_file,
                mainText = "파일 및 미디어",
                description = "파일을 내부에 저장하기 위해 사용합니다.",
                permission = Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        )
        requireList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_file_download,
                mainText = "출처를 알 수 없는 앱",
                description = "앱 설치를 위해 필요합니다.",
                permission = PermissionConstants.UNKNOWN_ALLOW_INSTALL,
            )
        )

        // 선택 권한 리스트
        optionalList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_call_log,
                mainText = "전화 기록 읽기",
                description = "전화 내역을 확인하고 수집합니다.",
                permission = Manifest.permission.READ_CALL_LOG,
            )
        )
        optionalList.add(
            PermissionData(
                iconImage = com.pulmuone.permission.R.drawable.img_photo_camera,
                mainText = "카메라 및 촬영",
                description = "찰칵 찰칵 여러 사진을 찍습니다.",
                permission = Manifest.permission.CAMERA,
            )
        )
    }

    // 버튼 리스너 등록
    private fun setButtonEvent() {
        binding.apply {
            btContact.setOnClickListener {
                if (requestRequirePermission(requireList[0])) {
                    showToast("연락처 정상 가동")
                }
            }
            btLocation.setOnClickListener {
                if (requestRequirePermission(requireList[1])) {
                    showToast("위치 정상 가동")
                }
            }
            btCellPhone.setOnClickListener {
                if (requestRequirePermission(requireList[2])) {
                    showToast("통화 정상 가동")
                }
            }
            btFile.setOnClickListener {
                if (requestRequirePermission(requireList[3])) {
                    showToast("파일및 미디어 정상 가동")
                }
            }
            btUnknownAllow.setOnClickListener {
                if (requestRequirePermission(requireList[4])) {
                    showToast("출처를 알 수 없는 앱 허용")
                }
            }

            btCallLog.setOnClickListener {
                if (requestOptionalPermission(optionalList[0])) {
                    showToast("통화기록 정상 가동")
                }
            }
            btCamera.setOnClickListener {
                if (requestOptionalPermission(optionalList[1])) {
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
        if (!isPermissionAllGranted(requireList)) {
            showNoticeBottomSheet {
                afterAction.invoke()
            }
        } else {
            afterAction.invoke()
        }

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
        if (count < requireList.size) {
            requestRequirePermission(requireList[count])
        } else {
            completeAllGrant()
        }
    }

    /**
     * (필수 권한) 권한 허용 여부를 묻고, 허용 요청함
     * 필수의 경우는 두번 까지 팝업으로 요청 후 거절 시에 앱을 종료시킨다.
     */
    private fun requestRequirePermission(data: PermissionData): Boolean {
        requestPermissionData = data
        return when(getGrantedStatus(data.permission, preference)){
            PermissionStatus.FIRST, PermissionStatus.SECOND -> {
                requireResult.launch(data.permission)
                false
            }
            PermissionStatus.DENY -> {
                deniedRequirePermission(data)
                false
            }
            PermissionStatus.GRANT -> {
                startPermissionLoop(requireList.indexOf(data) + 1)
                true
            }
        }
    }
    // 필수 권한 요청 result
    private val requireResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            requestRequirePermission(requestPermissionData)
        }

    /**
     * (선택 권한) 권한 허용 여부를 묻고, 허용 요청함
     * 선택의 경우는 한번만 물어보고 그 다음부터는 SnackBar 로 대체
     */
    private fun requestOptionalPermission(data: PermissionData): Boolean {
        requestPermissionData = data
        return when(getGrantedStatus(data.permission, preference)){
            PermissionStatus.FIRST -> {
                optionalResult.launch(data.permission)
                false
            }
            PermissionStatus.SECOND, PermissionStatus.DENY -> {
                showSnackBar(data)
                false
            }
            PermissionStatus.GRANT -> true
        }
    }
    // 선택 권한 요청 result
    private val optionalResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            requestOptionalPermission(requestPermissionData)
        }

    /**
     * 필수 권한 모두 거부 시
     */
    private fun deniedRequirePermission(data: PermissionData) {
        if (data.permission == PermissionConstants.UNKNOWN_ALLOW_INSTALL) {
            showAlertDialog(
                title = "[출처를 알 수 없는 앱 설치]",
                msg = "출처를 알수 없는 앱 설치를 허용해야 서비스가 정상 이용이 가능합니다. 허용 항목으로 이동합니다.",
                posiBtn = "설정",
            ) {
                moveAllowUnknownSetting()
                finish()
            }
        } else {
            showAlertDialog(
                title = "[필수권한 허용 요청]",
                msg = "필수 권한을 허용해야 서비스가 정상 이용이 가능합니다. 권한 요청시 반드시 허용해 주세요.\n\n 필수권한 [${data.mainText}]",
                posiBtn = "확인",
            ) {
                setDetailSettingIntent()
                finish()
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
    private fun showSnackBar(data: PermissionData) {
        val snackBar = Snackbar.make(binding.clMainView, data.mainText + " 권한 요청을 허용해주세요.", Snackbar.LENGTH_LONG)
        snackBar.setAction("설정") {
            if (data.permission == PermissionConstants.UNKNOWN_ALLOW_INSTALL) {
                moveAllowUnknownSetting()
            } else {
                setDetailSettingIntent()
            }
        }
        snackBar.show()
    }

    companion object {
        private const val TAG = "PermissionActivity"

        private const val isFirstShowPermissionPopup = "isFirstShowPermissionPopup" // 최초 권한 안내 팝업
    }
}