package com.kelvinks.www.fingerprint;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerPrintActivity extends AppCompatActivity {
    private KeyStore keyStore;
    //variable used for storing the key in android keystore container
    private static final String KEY_NAME="KELVIN";
    private Cipher cipher;
    private TextView errorText;
    private KeyGenerator keyGenerator;


    /*
                > generateKey () fungsi yang menghasilkan kunci enkripsi yang kemudian disimpan dengan aman di perangkat.

                > cipherInit () fungsi yang menginisialisasi cipher yang akan digunakan untuk membuat FingerprintManager yang dienkripsi.

                > CryptoObject instance dan berbagai pemeriksaan lain sebelum memulai proses otentikasi yang diimplementasikan dalam
                 metode onCreate ().

                 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);
        init();
        //inisialiasasi keyguardManager dan fingerPrintManager
        KeyguardManager keyguardManager=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        FingerprintManager fingerprintManager=(FingerprintManager)getSystemService(FINGERPRINT_SERVICE);

        // Check whether the device has a Fingerprint sensor.
        if(!fingerprintManager.isHardwareDetected()){
            errorText.setText("Your Device does not have a Fingerprint Sensor");
        }
        else{
            // Checks whether fingerprint permission is set on manifest
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                errorText.setText("Fingerprint authentication permission not enabled");
            }
            else{
                // Check whether at least one fingerprint is registered
                if(!fingerprintManager.hasEnrolledFingerprints()){
                    errorText.setText("Register at least one fingerprint in Settings");
                }
                else{
                    // Checks whether lock screen security is enabled or not
                    if(!keyguardManager.isKeyguardSecure()){
                        errorText.setText("Lock screen security not enabled in Settings");
                    }
                    else{
                        generateKey();
                        if (cipherInit()) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            FingerprintHandler helper = new FingerprintHandler(this);
                            helper.startAuth(fingerprintManager, cryptoObject);
                        }
                    }

                }

            }
        }
    }

    private void  init(){
        errorText=findViewById(R.id.errorText);
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected void generateKey()  {
        try{
            keyStore=KeyStore.getInstance("AndroidKeyStore");
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            keyGenerator =  KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        }catch (NoSuchAlgorithmException | NoSuchProviderException e){
            throw new RuntimeException("Failed to get Keygenerator Instance",e);
        }

        try{
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
            keyGenerator.generateKey();

        }catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e){
            throw new RuntimeException(e);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }




}