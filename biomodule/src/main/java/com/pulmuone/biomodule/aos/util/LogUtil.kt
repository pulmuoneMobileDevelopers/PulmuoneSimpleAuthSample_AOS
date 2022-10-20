package com.pulmuone.biomodule.aos.util

import android.util.Log
import com.pulmuone.biomodule.aos.Constants

class LogUtil {
    companion object{
        fun e(msg : String){
            Log.e(Constants.TAG, log(msg))
        }

        fun w(msg : String){
            Log.w(Constants.TAG, log(msg))
        }

        fun i(msg : String){
            Log.i(Constants.TAG, log(msg))
        }

        fun d(msg : String){
            Log.d(Constants.TAG, log(msg))
        }

        fun v(msg : String){
            Log.v(Constants.TAG, log(msg))
        }

        private fun log(msg : String) : String{
            val ste :StackTraceElement = Thread.currentThread().stackTrace[4]
            return  "[${ste.fileName} :: ${ste.methodName} $msg]"
        }
    }
}