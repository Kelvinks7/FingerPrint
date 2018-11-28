package com.kelvinks.www.fingerprint;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private Context context;
    private  CancellationSignal cancellationSignal = new CancellationSignal();
    public FingerprintHandler(Context mcontext) {
        context=mcontext;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    public void startListening(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    public void stopListening() {
        cancellationSignal.cancel();
        cancellationSignal=null;
    }

    public void completeFingerAuthentification(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject){

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try{
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }catch (SecurityException ex){
            Log.d("Error", "An error occurred:\n" + ex.getMessage());
        }catch (Exception ex){
            Log.d("Error", "An error occurred\n" + ex.getMessage());
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("Fingerprint Authentication error\n" + errString, false);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Fingerprint Authentication help\n" + helpString, false);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        ((Activity) context).finish();
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false);
    }
    public void update(String e, Boolean success){
        TextView textView = (TextView) ((Activity)context).findViewById(R.id.errorText);
        textView.setText(e);
    }

}
