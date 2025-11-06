package com.example.lab6_20203266;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.facebook.FacebookSdk;

import java.security.MessageDigest;

public class Lab6App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicialización Facebook (evita crash "SDK not initialized")
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id));
        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));
        FacebookSdk.sdkInitialize(getApplicationContext());

        // Log de Key Hash para configurar Facebook (útil si no tienes openssl)
        logFacebookKeyHash();
    }

    private void logFacebookKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(),
                    PackageManager.GET_SIGNING_CERTIFICATES
            );
            if (info.signingInfo != null) {
                Signature[] signatures = info.signingInfo.getApkContentsSigners();
                for (Signature signature : signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    String keyHash = Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                    Log.d("FB_KEY_HASH", keyHash);
                }
            }
        } catch (Exception e) {
            Log.e("FB_KEY_HASH", "Error obteniendo key hash: " + e.getMessage());
        }
    }
}
