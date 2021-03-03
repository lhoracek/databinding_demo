package cz.lhoracek.databinding

import android.util.Log
import timber.log.Timber

class ErrorReportingTree: Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if(priority > Log.ERROR){
           //  Report crash to crashlitics
        }else{
           //  FirebaseCrashlytics.getInstance().log(message);
        }
    }
}