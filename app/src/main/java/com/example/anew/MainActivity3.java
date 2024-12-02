package com.example.anew;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GnssClock;
import android.location.GnssMeasurementsEvent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.anew.Constellations.GnssConstellation;
import com.example.anew.Constellations.GpsTime;
import com.example.anew.Ntrip.GNSSEphemericsNtrip;
import com.example.anew.Ntrip.RTCM3Client;
import com.example.anew.Ntrip.RTCM3ClientListener;
import com.example.anew.coord.Coordinates;
import com.google.android.material.navigation.NavigationView;
import com.example.anew.RinexFileLogger.Rinex;
import com.example.anew.RinexFileLogger.RinexHeader;
import com.example.anew.coord.Coordinates;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity3 extends AppCompatActivity  {
    private static final String ACTION_SPP_UPDATE="com.example.SPP_UPDATE";
    //主界面控件共用部分

    private static SharedPreferences sharedPreferences;

    //共用按钮功能切换判断
    private boolean isSaveIndoorData = false;//开启与关闭存储室内原始数据
    private boolean isSaveIndoorPos = false;//开启与关闭存储室内定位结果
    private boolean isStartNavigatoin = false;//开启与关闭导航功能
    private boolean positioningstate = true;//室内外逻辑判断，true为室内，false为室外
    //按钮
    private TextView textBtnSky;
    private TextView textViewBtnSetting;
    private TextView textNotice;
    private TextView textViewBtnStart;
    private TextView textViewSizeAll;
    private TextView textViewSizeGps;
    private TextView textViewSizeGal;
    private TextView textViewSizeBds;
    private TextView textViewSizeGlo;
    private TextView textViewsizeQzss;
    private TextView textViewSizeGps1;
    private TextView textViewSizeGal1;
    private TextView textViewSizeBds1;
    private TextView textViewSizeGlo1;
    private TextView textViewsizeQzss1;
    private TextView textViewTiaoshi;
    private TextView textViewTMap;
    private TextView textViewResult;
    private TextView textViewLocation;
    private TextView textNum;
    //是否开始记录Rinex文件
    private boolean isRecord;
    //rinex观测值问件的获取
    private Rinex rinex;
    //GNSS监听器部分
    private LocationManager locationManager;//位置管理器
    private Location location;//位置参考值
    double[] xyz ={0,0,0};

    //GPS时间
    private GpsTime gpsTime;

    //参与运算的广播星历系统
    private GNSSEphemericsNtrip mGNSSEphemericsNtrip;
    //数据类部分
    private PositioningData positioningData;
    private GnssConstellation mGnssConstellation;
//
//    private RTCM3Client GPSRTCM3Client;//获取广播星历
//    private RTCM3Client SSRRTCM3Client;//获取精密星历SSR改正数
//    private RTCM3Client IonoRTCM3Client;//获取电离层参数
//
    private Coordinates pose;//获取的手机PGS芯片自带位置，用于平差计算
    private WeightedLeastSquares mWeightedLeastSquares;//最小二乘
    private WeightedLeastSquares1 mWeightedLeastSquares1;//最小二乘

    boolean poseinitialized=false;




    //uiHandler在主线程中创建，所以自动绑定主线程
    private Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            textViewTiaoshi.setText(data.getString("调试"));
        }
    };
    @SuppressLint("SuspiciousIndentation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        //权限申请函数调用
        registerPermission();

        sendUiUpdateMessage("调试","hello");


        //主界面初始化
        //setDrawerLeftEdgeSize(this,mapdrawerlayout);
        //数据类初始化
        positioningData = new PositioningData();
        //是否开始
        isRecord=false;
        //按钮等
        textViewSizeAll=findViewById(R.id.sizeall);
        textViewSizeGps=findViewById(R.id.sizeagps);
        textViewSizeGal=findViewById(R.id.sizegal);
        textViewSizeBds=findViewById(R.id.sizebds);
        textViewSizeGlo=findViewById(R.id.sizeglo);
        textViewsizeQzss=findViewById(R.id.sizeqzss);

        textViewSizeGps1=findViewById(R.id.sizeagps1);
        textViewSizeGal1=findViewById(R.id.sizegal1);
        textViewSizeBds1=findViewById(R.id.sizebds1);
        textViewSizeGlo1=findViewById(R.id.sizeglo1);
        textViewsizeQzss1=findViewById(R.id.sizeqzss1);
        textViewResult=findViewById(R.id.result);
        textViewLocation=findViewById(R.id.location);
        textNum=findViewById(R.id.number);
        textViewTMap=findViewById(R.id.map);
        textViewTMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity3.this, MapActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        textViewTiaoshi=findViewById(R.id.tiaoshi);
        textNotice=findViewById(R.id.notice);

        textBtnSky=findViewById(R.id.buttonSky);
        textBtnSky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity3.this,MainActivity2.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        textViewBtnSetting=findViewById(R.id.settingButtonFrom3);
        textViewBtnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity3.this, SettingActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        textViewBtnStart=findViewById(R.id.Start);
        textViewBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecord) {
                    if(gpsTime==null)
                    {
                        notice("无GNSS信号无法记录");
                        return;
                    }

                    mGNSSEphemericsNtrip=new GNSSEphemericsNtrip(new RTCM3Client(Constants.DEF_NTRIP_HOST,Integer.parseInt(Constants.DEF_NTRIP_PORT),"RTCM3EPH01", Constants.DEF_NTRIP_USERNAME,Constants.DEF_NTRIP_PASSWARD, RTCMListener));
                    new Thread(mGNSSEphemericsNtrip,"GNSS").start();

                    startRecordRinex();
                    isRecord = true;

                    textViewBtnStart.setBackgroundResource(R.drawable.bg_btn_red);
                    textViewBtnStart.setText("停止记录");

                    notice("开始记录");
                }else {
                    isRecord = false;
                    stopRecordRinex();
                    mGNSSEphemericsNtrip.stopNtrip();
                    textViewBtnStart.setBackgroundResource(R.drawable.bg_btn_lightgreen);
                    textViewBtnStart.setText("开始记录");
                    notice("结束记录");
                }
            }
        });

        //这个是获取SPP设置的信息
        sharedPreferences=getSharedPreferences(Constants.SPP_SETTING,0);

        //平差类初始化
        //mWeightedLeastSquares = new WeightedLeastSquares();
        mWeightedLeastSquares1=new WeightedLeastSquares1();


        // 从 SharedPreferences 中读取参数
        int param1 = sharedPreferences.getInt(Constants.KEY_GPS_SYSTEM, 1); // 默认值为 1
        int param2 = sharedPreferences.getInt(Constants.KEY_GAL_SYSTEM, 0); // 默认值为 0
        int param3 = sharedPreferences.getInt(Constants.KEY_GLO_SYSTEM, 0); // 默认值为 0
        int param4 = sharedPreferences.getInt(Constants.KEY_BDS_SYSTEM, 0); // 默认值为 0
        int param5 = sharedPreferences.getInt(Constants.KEY_QZSS_SYSTEM, 0); // 默认值为 0
        mGnssConstellation = new GnssConstellation(uiHandler,1, 1, 1, 1, 1,positioningData);
        //mGnssConstellation=new GnssConstellation(1,0,0,0,0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGnssConstellation.init(); // 初始化导航电文
                //ntrip连接
//                String host=sharedPreferences_spp.getString(Constants.KEY_NTRIP_HOST,Constants.DEF_NTRIP_HOST);
//                int port=Integer.parseInt(sharedPreferences_spp .getString(Constants.KEY_NTRIP_PORT,Constants.DEF_NTRIP_PORT));
//                String username=sharedPreferences_spp.getString(Constants.KEY_NTRIP_USERNAME,Constants.DEF_NTRIP_USERNAME);
//                String password=sharedPreferences_spp.getString(Constants.KEY_NTRIP_PASSWORD,Constants.DEF_NTRIP_PASSWARD);
//                mGNSSEphemericsNtrip=new GNSSEphemericsNtrip(new RTCM3Client(host,port,"RTCM3EPH01", username,password, RTCMListener));


                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notice("初始化完成");
                    }
                });
            }
        }).start();

            //mGnssConstellation=new GnssConstellation(sharedPreferences_spp.getInt(Constants.KEY_GPS_SYSTEM,Constants.DEF_GPS_SYSTEM),sharedPreferences_spp.getInt(Constants.KEY_GAL_SYSTEM ,Constants.DEF_GAL_SYSTEM),sharedPreferences_spp.getInt(Constants.KEY_GLO_SYSTEM,Constants.DEF_GLO_SYSTEM),sharedPreferences_spp.getInt(Constants.KEY_BDS_SYSTEM,Constants.DEF_BDS_SYSTEM),sharedPreferences_spp.getInt(Constants.KEY_QZSS_SYSTEM,Constants.DEF_QZSS_SYSTEM));

        //RTCM文件监听初始化
//        GPSRTCM3Client = new RTCM3Client("ntrip.gnsslab.cn", 2101, "RTCM3EPH-MGEX-GPS", "liuzancumt", "liuz@2021",rawGPSDataProcessing);
//        SSRRTCM3Client = new RTCM3Client("ntrip.gnsslab.cn", 2101, "IONO01IGS0", "liuzancumt", "liuz@2021",rawGPSDataProcessing);
//        IonoRTCM3Client = new RTCM3Client("ntrip.gnsslab.cn", 2101, "SSRA01GFZ0", "liuzancumt", "liuz@2021",rawGPSDataProcessing);
        //异步函数处理NTRIP协议连接，包括域名解析
        //Thread GPSthread = new Thread(GPSRTCM3Client);
        //Thread SSRthread = new Thread(GPSRTCM3Client);
        //Thread Ionothread = new Thread(GPSRTCM3Client);
        //开启处理RTCM数据的线程
        //GPSthread.start();
        //SSRthread.start();
        //Ionothread.start();


        //GNSS监听器初始化
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        registerGnssMeasurements();
        registerLocation();



    }

    private RTCM3ClientListener RTCMListener=new RTCM3ClientListener() {
        @Override
        public void onDataReceived(byte[] data) {
            mGNSSEphemericsNtrip.onDataReceived(data);
        }
    };
    //软件权限申请
    private void registerPermission() {
        //精确位置权限
        if (ActivityCompat.checkSelfPermission(MainActivity3.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity3.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        //模糊位置权限
        if (ActivityCompat.checkSelfPermission(MainActivity3.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity3.this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        //外部写入权限
        if (ActivityCompat.checkSelfPermission(MainActivity3.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity3.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        //外部读取权限
        if (ActivityCompat.checkSelfPermission(MainActivity3.this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity3.this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
        }
    }

    // 室外数据监听
    //GNSS原始观测值监听器注册
    private void registerGnssMeasurements() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.registerGnssMeasurementsCallback(gnssMeasurementsEvent);
    }

    //GNSS原始观测值监听器
    private GnssMeasurementsEvent.Callback gnssMeasurementsEvent = new GnssMeasurementsEvent.Callback() {
        @Override
        public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
            super.onGnssMeasurementsReceived(eventArgs);

            GnssClock clock = eventArgs.getClock();
            gpsTime =new GpsTime(clock);
            System.out.println("j");
            //mGnssConstellation.updateMeasurements(eventArgs);
            mGnssConstellation.updateMeasurements1(eventArgs);

//            textViewSizeAll.setText("历元观测数目"+ positioningData.gnssDataArrayList.size());
//            textViewSizeGps.setText("GPS"+ positioningData.gpsDataList.size());
//            textViewSizeGal.setText("GAL"+ positioningData.galileoDataList.size());
//            textViewSizeGlo.setText("GLO"+ positioningData.glonassDataList.size());
//            textViewSizeBds.setText("BDS"+ positioningData.bdsDataList.size());
//            textViewsizeQzss.setText("Qzss"+ positioningData.qzssDataList.size());
//            textViewSizeGps1.setText("GPS"+ positioningData.gpsSatelliteList.size());
//            textViewSizeGal1.setText("GAL"+ positioningData.galileoSatelliteList.size());
//            textViewSizeGlo1.setText("GLO"+ positioningData.glonassSatelliteList.size());
//            textViewSizeBds1.setText("BDS"+ positioningData.bdsSatelliteList.size());
//            textViewsizeQzss1.setText("Qzss"+ positioningData.qzssSatelliteList.size());



            if(isRecord)
            {
                rinex.writeBody(positioningData);
            }
            if(sharedPreferences.getInt(Constants.KEY_doublefrequency,0)==1)
            {
            }



            if(pose!=null){
                mGnssConstellation.calculateSatPosition(pose);
                textNum.setText("数目:"+positioningData.gnssDataArrayListtest.size()+"\n"+"数目:"+positioningData.computDataList.size());


                if (positioningData.computDataList.size() >= 5) {
                    pose=mWeightedLeastSquares1.calculatePose(positioningData,pose);
                    textViewResult.setText(String.format("X: %.6f  Y: %.6f  Z: %.2f", pose.getX(), pose.getY(), pose.getZ()));

                    // 创建 Intent 并发送广播
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MainActivity3.this);
                    Intent intent = new Intent(ACTION_SPP_UPDATE);
                    intent.putExtra("latitude", pose.getGeodeticLatitude());
                    intent.putExtra("longitude", pose.getGeodeticLongitude());
                    localBroadcastManager.sendBroadcast(intent);
                    Log.d("myBroad", "发生广播");
                }
            }
        }

    };

    //GPS芯片位置监听器注册器
    private void registerLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String bestProvider = locationManager.getBestProvider(getCriteria(), true);
        assert bestProvider != null;
        location = locationManager.getLastKnownLocation(bestProvider);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    //位置服务获取条件
    private static Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setSpeedRequired(true);
        criteria.setCostAllowed(false);
        criteria.setBearingRequired(true);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        return criteria;
    }

    //GPS芯片位置监听器
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location mlocation) {
            if (mlocation != null && !poseinitialized) {
                location = mlocation;
                xyz = Coordinates.WGS84LLAtoXYZ(location.getLatitude(), location.getLongitude(), location.getAltitude());
                pose = Coordinates.globalGeodInstance(location.getLatitude(), location.getLongitude(), location.getAltitude());
                textViewLocation.setText(String.format("X: %.6f  Y: %.6f  Z: %.2f", pose.getX(), pose.getY(), pose.getZ()));
                poseinitialized = true;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public static SharedPreferences getSharedPreferences()
    {
        return sharedPreferences;
    }

    private void startRecordRinex() {
        rinex = new Rinex(getApplicationContext(), sharedPreferences.getInt(Constants.KEY_RINEX_VER, Constants.DEF_RINEX_VER));
        rinex.writeHeader(new RinexHeader(
                sharedPreferences.getString(Constants.KEY_MARK_NAME, Constants.DEF_MARK_NAME),
                sharedPreferences.getString(Constants.KEY_MARK_TYPE, Constants.DEF_MARK_TYPE),
                sharedPreferences.getString(Constants.KEY_OBSERVER_NAME, Constants.DEF_OBSERVER_NAME),
                sharedPreferences.getString(Constants.KEY_OBSERVER_AGENCY_NAME, Constants.DEF_OBSERVER_AGENCY_NAME),
                sharedPreferences.getString(Constants.KEY_RECEIVER_NUMBER, Constants.DEF_RECEIVER_NUMBER),
                sharedPreferences.getString(Constants.KEY_RECEIVER_TYPE, Constants.DEF_RECEIVER_TYPE),
                sharedPreferences.getString(Constants.KEY_RECEIVER_VERSION, Constants.DEF_RECEIVER_VERSION),
                sharedPreferences.getString(Constants.KEY_ANTENNA_NUMBER, Constants.DEF_ANTENNA_NUMBER),
                sharedPreferences.getString(Constants.KEY_ANTENNA_TYPE, Constants.DEF_ANTENNA_TYPE),
                Double.parseDouble(sharedPreferences.getString(Constants.KEY_ANTENNA_ECCENTRICITY_EAST, Constants.DEF_ANTENNA_ECCENTRICITY_EAST)),
                Double.parseDouble(sharedPreferences.getString(Constants.KEY_ANTENNA_ECCENTRICITY_NORTH, Constants.DEF_ANTENNA_ECCENTRICITY_NORTH)),
                Double.parseDouble(sharedPreferences.getString(Constants.KEY_ANTENNA_HEIGHT, Constants.DEF_ANTENNA_HEIGHT)),
                String.format("%.4f", xyz[0]),
                String.format("%.4f", xyz[1]),
                String.format("%.4f", xyz[2]),
                gpsTime
        ));


    }
    private void stopRecordRinex() {
        rinex.closeFile();
    }


    private void notice(String s){
        textNotice.setText(s);
        textNotice.setVisibility(View.VISIBLE); // 确保 TextView 可见
        // 延迟两秒后隐藏 TextView
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textNotice.setVisibility(View.GONE); // 隐藏 TextView
            }
        }, 2000); // 2000 毫秒 = 2 秒
    }


    private void sendUiUpdateMessage(String key, String value) {
        Message msg = uiHandler.obtainMessage();
        Bundle data = new Bundle();
        data.putString(key, value);
        msg.setData(data);
        uiHandler.sendMessage(msg);
    }
}