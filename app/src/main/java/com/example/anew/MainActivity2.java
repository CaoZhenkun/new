package com.example.anew;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GnssMeasurementsEvent;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.Manifest;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.anew.Satellite;
import com.example.anew.widget.CompassView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    private SharedPreferences sharedPreferences_spp;
    private Map<Integer,Satellite> mapSatellite=new HashMap<>();
    private CompassView cv_satellite; // 声明一个罗盘视图对象
    private String[] mSystemArray = new String[] {"UNKNOWN", "GPS", "SBAS",
            "GLONASS", "QZSS", "BEIDOU", "GALILEO", "IRNSS"};
    //GNSS监听器部分
    private LocationManager locationManager;//位置管理器
    private Location location;//位置参考值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);


        cv_satellite = findViewById(R.id.cv_satellite);
        //这个是获取SPP设置的信息
        sharedPreferences_spp=getSharedPreferences(Constants.SPP_SETTING,0);

        //GNSS监听器初始化
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        registerGnssMeasurements();


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 定义一个GNSS状态监听器
    private GnssStatus.Callback mGnssStatusListener = new GnssStatus.Callback() {
        @Override
        public void onStarted() {}

        @Override
        public void onStopped() {}

        @Override
        public void onFirstFix(int ttffMillis) {}

        // 在卫星导航系统的状态变更时触发
        @Override
        public void onSatelliteStatusChanged(GnssStatus status) {
            mapSatellite.clear();
            for (int i=0; i<status.getSatelliteCount(); i++) {

                Satellite item = new Satellite(); // 创建一个卫星信息对象
                item.signal = status.getCn0DbHz(i); // 获取卫星的信号
                item.elevation = status.getElevationDegrees(i); // 获取卫星的仰角
                item.azimuth = status.getAzimuthDegrees(i); // 获取卫星的方位角
                int systemType = status.getConstellationType(i); // 获取卫星的类型
                item.name = mSystemArray[systemType];
                mapSatellite.put(i, item);
            }
            cv_satellite.setSatelliteMap(mapSatellite); // 设置卫星浑天仪
        }
    };

    //GNSS原始观测值监听器注册
    private void registerGnssMeasurements() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.registerGnssStatusCallback(mGnssStatusListener); // 注册 GNSS 状态监听器
    }

}