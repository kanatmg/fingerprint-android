package com.example.myapplication;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.za.finger.ZA_finger;
import com.za.finger.ZAandroid;

import java.io.DataOutputStream;
import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class MainActivity extends AppCompatActivity {

    private Button btnRegActivity;
    private Button btnRegister;;
    private Button btntutorial;
    private Button btnsave;
    private Button btncompare;
    private ImageView mFingerprintIv ;
    private EditText editName;
    private ProgressBar progressBar;
    private FloatingActionButton floatingActionButton;
    private  Toast toast;
    Bitmap bmpDefaultPic;
    Dialog dialog;
    Dialog userDialog;
    Dialog notfoundDialog;
    Dialog poweronDialog;
    Dialog poweringDialog;

    private boolean fpflag=false;
    private boolean fpcharflag = false;
    private boolean fpmatchflag = false;
    private boolean fperoll = false;
    private boolean fpsearch = false;
    private boolean isfpon  = false;




    private TextView mtvMessage;
    long ssart = System.currentTimeMillis();
    long ssend = System.currentTimeMillis();
    private Handler objHandler_fp;
    //private HandlerThread thread;



    private int testcount = 0;
    private ZAandroid a6 = new ZAandroid();
    private int fpcharbuf = 1;
    private byte[] pTempletbase = new byte[2304];
    private int IMG_SIZE = 0;//同参数：（0:256x288 1:256x360）

    private String TAG = "zazdemo";
    private int DEV_ADDR = 0xffffffff;
    private byte[] pPassword = new byte[4];
    private Handler objHandler_3 ;
    private int rootqx = 1;///0 noroot  1root
    //private int defDeviceType =  2;//zaz060
    private int defDeviceType =  12;//zaz050
    private int defiCom = 4;//;
    private int defiBaud = 6;
    private boolean isshowbmp = false;

    private int iPageID = 0;
    Context ahandle;
    public Thread deviceThread;
    private boolean entryPointFingerprint;
    private int fpcharlen = 512;
    private int  fpchcount = 2;



    public static final int opensuccess = 101;
    public static final int openfail = 102;
    public static final int usbfail = 103;

    private final Handler m_fEvent = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String temp  = null;
            switch (msg.what) {
                case opensuccess:
                    temp = getResources().getString(R.string.open_str);
                    //mtvMessage.setText(temp);
                    toast.makeText(ahandle,temp,Toast.LENGTH_SHORT).show();
                    //btnopen.setText(getResources().getString(R.string.close_str));
                    break;
                case openfail:
                    //temp =getResources().getString(R.string.openfail_str);
                    mtvMessage.setText(temp);
                    Toast.makeText(ahandle,temp,Toast.LENGTH_SHORT).show();
                    //btnopen.setText(getResources().getString(R.string.open_str));
                    break;
                case usbfail:
                    //temp =getResources().getString(R.string.usbfail_str);
                    mtvMessage.setText(temp);
                    //btnopen.setText(getResources().getString(R.string.open_str));
                    break;
            }
        }
    };

    private void Sleep(int times)
    {
        try {
            Thread.sleep(times);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void skipshow(String Str)
    {
        Toast.makeText(ahandle,Str,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mtvMessage = (TextView) findViewById(R.id.tempText);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegActivity = (Button) findViewById(R.id.reg);
        setSupportActionBar(toolbar);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        btnRegActivity.setEnabled(false);
        btnRegister.setEnabled(false);
        btnOnClick();
        objHandler_fp = new Handler();//
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        userDialog = new Dialog(this);
        userDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        notfoundDialog = new Dialog(this);
        notfoundDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        poweronDialog = new Dialog(this);
        poweronDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        poweringDialog = new Dialog(this);
        poweringDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //初始化基本参数
        ahandle = this;		//页面句柄
        rootqx = 1;			//系统权限(0:not root  1:root)
        defDeviceType=12;	//设备通讯类型(2:usb  1:串口)
        defiCom= 6;			//设备波特率(1:9600 2:19200 3:38400 4:57600 6:115200  usb无效)
    }
    private void showTimer(String time)
    {

        TextView estimatedTime;
        dialog.setContentView(R.layout.pop_up_finger_contact);
        dialog.show();
    }
    public void userDetailDialog(String name,String surname,String iin){
        TextView nameResponse;
        TextView surnameResponse;
        TextView iinResponse;
        userDialog.setContentView(R.layout.pop_up_user_detail);
        nameResponse = (TextView) userDialog.findViewById(R.id.nameResponse);
        surnameResponse = (TextView) userDialog.findViewById(R.id.surnameResponse);
        iinResponse = (TextView) userDialog.findViewById(R.id.iinResponse);
        nameResponse.setText(name);
        surnameResponse.setText(surname);
        iinResponse.setText(iin);
        userDialog.show();

    }
    public void notfoundDialog(){
        notfoundDialog.setContentView(R.layout.pop_up_not_found);
        notfoundDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (notfoundDialog.isShowing()){
                    notfoundDialog.dismiss();
                }
            }
        }, 5000);
    }
    public void showPowerOnDialog(){
        poweronDialog.setContentView(R.layout.pop_up_power_on);
        poweronDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (poweronDialog.isShowing()){
                    poweronDialog.dismiss();
                }
            }
        }, 3000);
    }
    public void showPoweringDialog(){
        poweringDialog.setContentView(R.layout.pop_up_powering);
        poweringDialog.show();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void btnOnClick()
    {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                byte[] pPassword = new byte[4];
                //skipshow("open");
                showPoweringDialog();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (poweringDialog.isShowing()){
                            poweringDialog.dismiss();
                        }
                    }
                }, 4000);
                Runnable r = new Runnable() {
                    public void run() {
                        isusbfinshed = 3;
                        ZA_finger fppower = new ZA_finger();
                        //fppower.finger_power_on();
                        Sleep(1000);
                        OpenDev();

                    }
                };
                deviceThread = new Thread(r);
                deviceThread.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnRegActivity.setEnabled(true);
                        btnRegister.setEnabled(true);
                    }
                }, 4000);
                poweringDialog.dismiss();

                showPowerOnDialog();


            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setflag(true);
                fpflag = false;
                objHandler_fp.removeCallbacks(fpTasks);
                readsfpimg();

            }
        });
        btnRegActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registrationIntent = new Intent(MainActivity.this,RegistrationActivity.class);
                MainActivity.this.startActivity(registrationIntent);
            }
        });

    }
    private   void compareImageRequest(String filePath) {
        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
        //Create a file object using file path
        File file = new File(filePath);
        // Create a request body with file and image media type
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);
        // Create MultipartBody.Part using file request-body,file name and part name
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);

        Call<User> call = uploadAPIs.ComeInImage(part);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call,
                                   Response<User> response) {
                Toast toast = null;
                assert response.body() != null;
                String temp = response.body().getName();
                toast = Toast.makeText(getApplicationContext(),
                        response.body().getName(), Toast.LENGTH_SHORT);
                toast.show();
                if(response.body().getName()!=null) {
                    userDetailDialog(response.body().getName(), response.body().getSurname(), response.body().getIin());
                } else {
                    notfoundDialog();
                }

                btnRegister.setEnabled(true);
                /*
                Snackbar.make(getCurrentFocus(), temp, Snackbar.LENGTH_LONG)
                        .setAction(temp, null).show();*/


                /*  if(response.body().getName() == null){
                    Intent popUp = new Intent(MainActivity.this, NotFoundCard.class);
                    MainActivity.this.startActivity(popUp);
                }else{
                    Intent popUp = new Intent(MainActivity.this, UserCard.class);
                    popUp.putExtra("name",response.body().getName());
                    popUp.putExtra("surname",response.body().getSurname());
                    popUp.putExtra("iin",response.body().getIin());
                    MainActivity.this.startActivity(popUp);
                }*/


                Log.v("Upload", "success");
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });


    }

    //打开设备
    private void OpenDev() {
        // TODO Auto-generated method stub
        int status = -1;
        rootqx = 1;
        if( 1 == rootqx){
            Log.i(TAG,"start Opendev");
            //	skipshow("tryusbroot");
            Log.i(TAG,"use by root ");
            LongDunD8800_CheckEuq();
            status = a6.ZAZOpenDevice(-1, 12, defiCom, defiBaud, 0, 0);
            Log.i(TAG,"status =  "+status + "  (1:success other：error)");
            if(status == 0 ){
                status = a6.ZAZVfyPwd(DEV_ADDR, pPassword) ;
                a6.ZAZSetImageSize(IMG_SIZE);
            }
            else{
                rootqx = 0;
            }
        }

        //if(false)
        if( 0 == rootqx)
        {
            Log.i(TAG,"use by not root ");
            device = null;
            isusbfinshed  = 0;
            int fd = 0;
            defDeviceType = 12;
            isusbfinshed = getrwusbdevices();
            //skipshow("watting a time");
            Log.i(TAG,"waiting user put root ");
            if(WaitForInterfaces() == false)  {
                m_fEvent.sendMessage(m_fEvent.obtainMessage(usbfail, R.id.fab, 0));
                return;
            }
            fd = OpenDeviceInterfaces();
            if(fd == -1)
            {
                m_fEvent.sendMessage(m_fEvent.obtainMessage(usbfail, R.id.fab, 0));
                return;
            }
            Log.e(TAG, "open fd: " + fd);
            status = a6.ZAZOpenDevice(fd, defDeviceType, defiCom, defiBaud, 0, 0);
            Log.e("ZAZOpenDeviceEx",""+defDeviceType +"  "+defiCom+"   "+defiBaud +"  status "+status);
            if(status == 0 ){
                status = a6.ZAZVfyPwd(DEV_ADDR, pPassword) ;
                a6.ZAZSetImageSize(IMG_SIZE);
            }
        }
        Log.e(TAG, " open status: " + status);
        if(status == 0){
            m_fEvent.sendMessage(m_fEvent.obtainMessage(opensuccess, R.id.fab, 0)  );

        }
        else{
            m_fEvent.sendMessage(m_fEvent.obtainMessage(openfail, R.id.fab, 0));
            if(defDeviceType == 2)
                defDeviceType =5;
            else if(defDeviceType ==5)
                defDeviceType =12;
            else if(defDeviceType ==12)
                defDeviceType =15;
            else
                defDeviceType =2;
        }
    }


    //获取图像
    public void readsfpimg()
    {
        ssart = System.currentTimeMillis();
        ssend = System.currentTimeMillis();
        testcount = 0;
        objHandler_fp.postDelayed(fpTasks, 0);
    }


    private Runnable fpTasks = new Runnable() {
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
                //mtvMessage.setText(temp);
                return;
            }
            if(fpflag){
                temp =getResources().getString(R.string.stopgetimage_str)+"\r\n";
                //mtvMessage.setText(temp);
                return;
            }
            int nRet = 0;
            st = System.currentTimeMillis();
            nRet = a6.ZAZGetImage(DEV_ADDR);
            sd = System.currentTimeMillis();
            timecount = (sd - st);
            temp = getResources().getString(R.string.getimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
            st = System.currentTimeMillis();
            if(nRet  == 0)
            {
                testcount = 0;
                int[] len = { 0, 0 };
                byte[] Image = new byte[256 * 360];
                a6.ZAZUpImage(DEV_ADDR, Image, len);
                sd = System.currentTimeMillis();
                timecount = (sd - st);
                temp += getResources().getString(R.string.upimagesuccess_str) + "耗时:"+timecount+"ms\r\n";
                //mtvMessage.setText(temp);

                String str = "/mnt/sdcard/test.bmp";
                a6.ZAZImgData2BMP(Image, str);
                bmpDefaultPic = BitmapFactory.decodeFile(str,null);
                //mFingerprintIv.setImageBitmap(bmpDefaultPic);
                String fingerPrintPath = Utils.saveImage(bmpDefaultPic,getRootDir());
                //progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(false);
                compareImageRequest(fingerPrintPath);
                dialog.dismiss();

                //FingerprintRequests.compareImageRequest(fingerPrintPath,getApplicationContext(),entryPointFingerprint);
                //progressBar.setVisibility(View.VISIBLE);
            }
            else if(nRet==a6.PS_NO_FINGER){
                temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"."+(1000-(ssend - ssart)%1000) +"s";
                showTimer(temp);
                //mtvMessage.setText(temp);
                objHandler_fp.postDelayed(fpTasks, 100);
            }
            else if(nRet==a6.PS_GET_IMG_ERR){
                temp =getResources().getString(R.string.getimageing_str);
                Log.d(TAG, temp+"2: "+nRet);
                objHandler_fp.postDelayed(fpTasks, 100);
                dialog.dismiss();
                //mtvMessage.setText(temp);
                return;
            }else if(nRet == -2)
            {
                testcount ++;
                if(testcount <3){
                    temp = getResources().getString(R.string.readingfp_str)+((10000-(ssend - ssart)))/1000 +"s";
                    isfpon = false;
                    //mtvMessage.setText(temp);
                    showTimer(temp);
                    objHandler_fp.postDelayed(fpTasks, 10);
                }
                else{
                    temp =getResources().getString(R.string.Communicationerr_str);
                    Log.d(TAG, temp+": "+nRet);
                    dialog.dismiss();
                    //mtvMessage.setText(temp);
                    return;
                }
            }
            else
            {
                temp =getResources().getString(R.string.Communicationerr_str);
                Log.d(TAG, temp+"2: "+nRet);
                dialog.dismiss();
                //mtvMessage.setText(temp);
                return;
            }

        }
    };

    private void setflag(boolean value)
    {
        fpflag = value;
        fpcharflag = value;
        fpmatchflag= value;
        fperoll = value;
        fpsearch = value;


    }

    /*****************************************
     * 线程   end
     * ***************************************/


    private static String charToHexString(byte[] val,int len) {
        String temp="";
        for(int i=0;i<len;i++)
        {
            String hex = Integer.toHexString(0xff & val[i]);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            temp += hex.toUpperCase();
        }
        return temp;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }





    public int LongDunD8800_CheckEuq()
    {
        Process process = null;
        DataOutputStream os = null;

        // for (int i = 0; i < 10; i++)
        // {
        String path = "/dev/bus/usb/00*/*";
        String path1 = "/dev/bus/usb/00*/*";
        File fpath = new File(path);
        Log.d("*** LongDun D8800 ***", " check path:" + path);
        // if (fpath.exists())
        // {
        String command = "chmod 777 " + path;
        String command1 = "chmod 777 " + path1;
        Log.d("*** LongDun D8800 ***", " exec command:" + command);
        try
        {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command+"\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            return 1;
        }
        catch (Exception e)
        {
            Log.d("*** DEBUG ***", "Unexpected error - Here is what I know: "+e.getMessage());
        }
        //  }
        //  }
        return 0;
    }


    private String getRootDir(){
        String root = this.getFilesDir().toString();
        return root;
    }

    private UsbManager mDevManager = null;
    private PendingIntent permissionIntent = null;
    private UsbInterface intf = null;
    private UsbDeviceConnection connection = null;
    private UsbDevice device = null;
    public int isusbfinshed = 0;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public int getrwusbdevices() {

        mDevManager = ((UsbManager) this.getSystemService(Context.USB_SERVICE));
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        this.registerReceiver(mUsbReceiver, filter);
        //this.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
        HashMap<String, UsbDevice> deviceList = mDevManager.getDeviceList();
        if (true) Log.e(TAG, "news:" + "mDevManager");


        for (UsbDevice tdevice : deviceList.values()) {
            Log.i(TAG,	tdevice.getDeviceName() + " "+ Integer.toHexString(tdevice.getVendorId()) + " "
                    + Integer.toHexString(tdevice.getProductId()));
            if (tdevice.getVendorId() == 0x2109 && (tdevice.getProductId() == 0x7638))
            {
                Log.e(TAG, " 指纹设备准备好了 ");
                mDevManager.requestPermission(tdevice, permissionIntent);
                return 1;
            }
        }
        Log.e(TAG, "news:" + "mDevManager  end");
        return 2;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(mUsbReceiver);
            isusbfinshed = 0;
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (context) {
                    device = (UsbDevice) intent	.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.e("BroadcastReceiver","3333");
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            if (true) Log.e(TAG, "Authorize permission " + device);
                            isusbfinshed = 1;
                        }
                    }
                    else {
                        if (true) Log.e(TAG, "permission denied for device " + device);
                        device=null;
                        isusbfinshed = 2;

                    }
                }
            }
        }
    };

    public boolean WaitForInterfaces() {
        int i =0;
        while (device==null || isusbfinshed == 0) {
            i++;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e){

            }
            if(i>2000){
                isusbfinshed = 2;break;
            }
            if(isusbfinshed == 2)break;
            if(isusbfinshed == 3)break;
        }
        if(isusbfinshed == 2)
            return false;
        if(isusbfinshed == 3)
            return false;
        return true;
    }

    public int OpenDeviceInterfaces() {
        UsbDevice mDevice = device;
        Log.d(TAG, "setDevice " + mDevice);
        int fd = -1;
        if (mDevice == null) return -1;
        connection = mDevManager.openDevice(mDevice);
        if (!connection.claimInterface(mDevice.getInterface(0), true)) return -1;

        if (mDevice.getInterfaceCount() < 1) return -1;
        intf = mDevice.getInterface(0);

        if (intf.getEndpointCount() == 0) 	return -1;

        if ((connection != null)) {
            if (true) Log.e(TAG, "open connection success!");
            fd = connection.getFileDescriptor();
            return fd;
        }
        else {
            if (true) Log.e(TAG, "finger device open connection FAIL");
            return -1;
        }
    }
}
