package com.example.anew;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.anew.databinding.ActivityMapBinding;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

public class MapActivity extends AppCompatActivity {

    private static final String ACTION_SPP_UPDATE="com.example.SPP_UPDATE";

    private Marker mMarker = null;
    // 定位权限
    private final String[] permissions = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            // 安卓13不注释掉这两行会一直闪,14不会
//             android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//             android.Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // 权限申请
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (!result.containsValue(false)) {
                    initMap();
                } else {
                    requestPermission();
                }
            });

    private ActivityMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化视图绑定
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    private BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("myBroad", "接受广播");


            // 处理 ACTION_SPP_UPDATE 广播
            if (ACTION_SPP_UPDATE.equals(intent.getAction())) {
                double latitude = intent.getDoubleExtra("latitude", 0);
                double longitude = intent.getDoubleExtra("longitude", 0);
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);
                changeMapCenter(geoPoint);
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();

         //注册广播接收器
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, new IntentFilter(ACTION_SPP_UPDATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 注销广播接收器
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
    }

    /**
     * 初始化地图
     */
    private void initMap() {
        binding.mapView.setTileSource(Config.TDTVEC_W);  // 设置瓦片地图资源

        // 添加叠加图层
        MapTileProviderBasic tileProvider = new MapTileProviderBasic(this);
        tileProvider.setTileSource(Config.TDTCVA_W);
        TilesOverlay overlay = new TilesOverlay(tileProvider, this);
        binding.mapView.getOverlayManager().add(overlay);


        binding.mapView.setMinZoomLevel(5.0);            // 最小缩放级别
        binding.mapView.setMaxZoomLevel(18.0);           // 最大缩放级别
        binding.mapView.setTilesScaledToDpi(true);       // 图块是否缩放到 DPI
        binding.mapView.setMultiTouchControls(true);     // 多点触控功能
        // 设置默认的地图中心点
        binding.mapView.getController().setZoom(16.0);
        binding.mapView.getController().setCenter(Config.DEFAULT_GEO_POINT);

        binding.mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getOverlayManager().getTilesOverlay().setEnabled(true);

        // 启用旋转手势
        RotationGestureOverlay rotationGestureOverlay = new RotationGestureOverlay(binding.mapView);
        rotationGestureOverlay.setEnabled(true);
        binding.mapView.getOverlays().add(rotationGestureOverlay);
    }

    /**
     * 检查权限
     */
    private void checkPermission() {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                requestPermission();
                return;
            }
        }
        // 初始化地图
        initMap();
    }

    /**
     * 请求权限
     */
    private void requestPermission() {
        permissionLauncher.launch(permissions);
    }

    /**
     * 修改地图中心点
     */
    private void changeMapCenter(GeoPoint geoPoint) {
        if(mMarker!=null){
            binding.mapView.getOverlays().remove(mMarker);
        }
        mMarker = new Marker(binding.mapView);
        mMarker.setTitle("Marker");
        mMarker.setPosition(geoPoint);
        mMarker.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_marker));
        // 添加标点
        binding.mapView.getOverlays().add(mMarker);

        Config.DEFAULT_GEO_POINT=geoPoint;

        //binding.mapView.getController().setZoom(16.0);
        binding.mapView.getController().setCenter(geoPoint);
    }
}