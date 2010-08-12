package com.piratemedia.lockscreen;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.KeyguardManager.OnKeyguardExitResult;
import android.content.Context;
import android.util.Log;

public class ManageKeyguard {
  private static KeyguardManager myKM = null;
  private static KeyguardLock myKL = null;
  private static final boolean DEBUG=true;
  private static final String LOGTAG="LOCKSCREEN_KEYGUARDMANAGER";
  public static synchronized void initialize(Context context) {
    if (myKM == null) {
      myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }
  }

  public static synchronized void disableKeyguard(Context context) {
    // myKM = (KeyguardManager)
    // context.getSystemService(Context.KEYGUARD_SERVICE);
    initialize(context);

    if (myKM.inKeyguardRestrictedInputMode()) {
      myKL = myKM.newKeyguardLock(LOGTAG);
      myKL.disableKeyguard();
      if (DEBUG) Log.v(LOGTAG,"--Keyguard disabled");
    } else {
      if (DEBUG) Log.v(LOGTAG,"--Keyguard not disabled....");
      myKL = null;
    }
  }

  public static synchronized boolean inKeyguardRestrictedInputMode() {
    if (myKM != null) {
      if (DEBUG) Log.v(LOGTAG,"--inKeyguardRestrictedInputMode = " + myKM.inKeyguardRestrictedInputMode());
      return myKM.inKeyguardRestrictedInputMode();
    }
    return false;
  }

  public static synchronized void reenableKeyguard() {
    if (myKM != null) {
      if (myKL != null) {
        myKL.reenableKeyguard();
        myKL = null;
        if (DEBUG) Log.v(LOGTAG,"--Keyguard reenabled");
      }
    }
  }

  public static synchronized void exitKeyguardSecurely(final LaunchOnKeyguardExit callback) {
    if (inKeyguardRestrictedInputMode()) {
      if (DEBUG) Log.v(LOGTAG,"--Trying to exit keyguard securely");
      myKM.exitKeyguardSecurely(new OnKeyguardExitResult() {
        public void onKeyguardExitResult(boolean success) {
          reenableKeyguard();
          if (success) {
            if (DEBUG) Log.v(LOGTAG,"--Keyguard exited securely");
            callback.LaunchOnKeyguardExitSuccess();
          } else {
            if (DEBUG) Log.v(LOGTAG,"--Keyguard exit failed");
          }
        }
      });
    } else {
      callback.LaunchOnKeyguardExitSuccess();
    }
  }

  public interface LaunchOnKeyguardExit {
    public void LaunchOnKeyguardExitSuccess();
  }
}
