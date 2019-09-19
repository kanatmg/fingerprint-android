package com.example.myapplication;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.za.finger.ZA_finger;
import com.za.finger.ZAandroid;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;



public class RegistrationActivity extends AppCompatActivity {
    private Button btnsave;
    private ImageView mFingerprintIv ;
    private EditText editName;
    private EditText editSurname;
    private EditText editIin;
    private Toolbar  toolbar;
    private TextView mtvMessage;
    Bitmap bmpDefaultPic;

    private boolean fpflag=false;
    long ssart = System.currentTimeMillis();
    long ssend = System.currentTimeMillis();
    private Handler objHandler_fp;
    private int testcount = 0;
    private ZAandroid a6 = new ZAandroid();

    private String TAG = "zazdemo";
    private int DEV_ADDR = 0xffffffff;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnsave = (Button) findViewById(R.id.btnsave);
        editIin = (EditText) findViewById(R.id.iin);
        editName = (EditText) findViewById(R.id.firstname);
        editSurname  = (EditText) findViewById(R.id.surname);
        mtvMessage = (TextView) findViewById(R.id.responseText);
        mFingerprintIv = (ImageView) findViewById(R.id.imageView);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        File appDirectory = new File( Environment.getExternalStorageDirectory() + "/saved_images" );
        File logDirectory = new File( appDirectory + "/log" );
        File logFile = new File( logDirectory, "logcat" + System.currentTimeMillis() + ".txt" );

        // create app folder
        if ( !appDirectory.exists() ) {
            appDirectory.mkdir();
        }

        // create log folder
        if ( !logDirectory.exists() ) {
            logDirectory.mkdir();
        }

        // clear the previous logcat and then write the new one to the file
        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            process = Runtime.getRuntime().exec("logcat -f " + logFile);
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        objHandler_fp = new Handler();
        
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setflag(true);
                fpflag = false;
                readsfpimg1();

            }
        });
    }

    private void uploadImageRequest(String filePath, String name, String surname, String iin) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        //Create a file object using file path
        File file = new File(filePath);
        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);
        //Create request body with text description and text media type
        RequestBody name1 = RequestBody.create(okhttp3.MultipartBody.FORM, name);
        RequestBody surname2 = RequestBody.create(okhttp3.MultipartBody.FORM, surname);
        RequestBody iin3 = RequestBody.create(okhttp3.MultipartBody.FORM, iin);
        Call<ResponseBody> call = uploadAPIs.uploadFingerprint(part, name1, surname2, iin3);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                Toast toast = null;
                try {
                    assert response.body() != null;
                    int counter = 3;
                    String responseBody = response.body().string();
                    if(responseBody.length()< 2){
                        System.out.println(responseBody);
                        int tempCounter = Integer.parseInt(responseBody);
                        int summary = counter-tempCounter;
                        toast = Toast.makeText(getApplicationContext(),
                                "Еще, "+summary+"раза", Toast.LENGTH_SHORT);
                        toast.show();
                        String str = "Приложите отпечаток еще, "+summary+" раз";
                        mtvMessage.setText(str);
                    }else {
                        System.out.println(responseBody);
                        toast = Toast.makeText(getApplicationContext(),
                                responseBody, Toast.LENGTH_SHORT);
                        toast.show();
                        mtvMessage.setText(responseBody);
                        ReturnHome(getCurrentFocus());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }
    public void ReturnHome(View view){
        super.onBackPressed();
    }
    public void readsfpimg1()
    {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        testcount = 0;
        objHandler_fp.postDelayed(fpTasks1, 0);
    }




    private Runnable fpTasks1 = new Runnable() {
        public void run()// 运行该服务执行此函数
        {
            String temp="";
            long st = System.currentTimeMillis();
            long sd = System.currentTimeMillis();
            long timecount=0;
            ssend = System.currentTimeMillis();
            timecount = (ssend - ssart);
            if (timecount >10000)
            {
                temp =getResources().getString(R.string.readfptimeout_str)+"\r\n";
                mtvMessage.setText(temp);
                return;
            }
            if(fpflag){
                temp =getResources().getString(R.string.stopgetimage_str)+"\r\n";
                mtvMessage.setText(temp);
                return;
            }
            int nRet = 0;
            st = System.currentTimeMillis();
            nRet = a6.ZAZGetImage(DEV_ADDR);
            sd = System.currentTimeMillis();
            timecount = (sd - st);
            temp = getResources().getString(R.string.getimagesuccess_str) + "Осталось:"+timecount+"ms\r\n";
            st = System.currentTimeMillis();
            if(nRet  == 0)
            {
                testcount = 0;
                int[] len = { 0, 0 };
                byte[] Image = new byte[256 * 360];
                a6.ZAZUpImage(DEV_ADDR, Image, len);
                sd = System.currentTimeMillis();
                timecount = (sd - st);
                temp += getResources().getString(R.string.upimagesuccess_str) + "Осталось:"+timecount+"ms\r\n";
                mtvMessage.setText(temp);

                String str = "/mnt/sdcard/test.bmp";
                a6.ZAZImgData2BMP(Image, str);
                bmpDefaultPic = BitmapFactory.decodeFile(str,null);
                mFingerprintIv.setImageBitmap(bmpDefaultPic);
                String name = editName.getText().toString();
                String surname = editSurname.getText().toString();
                String iin = editIin.getText().toString();
                if(TextUtils.isEmpty(name)){
                    editName.setError("Введите Имя");
                    return;
                }
                if(TextUtils.isEmpty(surname)){
                    editSurname.setError("Введите Фамилия");
                    return;
                }
                if(TextUtils.isEmpty(iin)){
                    editIin.setError("Введите ИИН");
                    return;
                }
                editName.setError(null);
                editSurname.setError(null);
                editIin.setError(null);
                disableEditText(editName);
                disableEditText(editSurname);
                disableEditText(editIin);
                String fingerPrintPath = Utils.saveImage(bmpDefaultPic,getRootDir());
                uploadImageRequest(fingerPrintPath, name, surname,iin);

            }
            else if(nRet==a6.PS_NO_FINGER){
                temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
                mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fpTasks1, 100);
            }
            else if(nRet==a6.PS_GET_IMG_ERR){
                temp =getResources().getString(R.string.getimageing_str);
                Log.d(TAG, temp+"2: "+nRet);
                objHandler_fp.postDelayed(fpTasks1, 100);
                mtvMessage.setText(temp);
                return;
            }else if(nRet == -2)
            {
                testcount ++;
                if(testcount <3){
                    temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"s";
                    mtvMessage.setText(temp);
                    objHandler_fp.postDelayed(fpTasks1, 10);
                }
                else{
                    temp =getResources().getString(R.string.Communicationerr_str);
                    Log.d(TAG, temp+": "+nRet);
                    mtvMessage.setText(temp);
                    return;
                }
            }
            else
            {
                temp =getResources().getString(R.string.Communicationerr_str);
                Log.d(TAG, temp+"2: "+nRet);
                mtvMessage.setText(temp);
                return;
            }

        }
    };

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setEnabled(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setBackgroundColor(Color.TRANSPARENT);
    }
    private void setflag(boolean value)
    {
        fpflag = value;

    }



    private String getRootDir(){
        String root = this.getFilesDir().toString();
        return root;
    }


}
