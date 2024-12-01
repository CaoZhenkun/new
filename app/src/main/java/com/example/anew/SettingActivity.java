package com.example.anew;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingActivity extends AppCompatActivity {


    private static final String TAG = "Setting";
    private TextView textViewGnssSystem;
    private TextView frequencyText;
    private TextView recordText;



    //SharedPreferences是Android平台上一个轻量级的存储辅助类，用来保存应用的一些常用配置，它提供了String，set，int，long，float，boolean六种数据类型。
    // SharedPreferences的数据以键值对的进行保存在以xml形式的文件中。在应用中通常做一些简单数据的持久化缓存。
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences(Constants.SPP_SETTING, 0);
        textViewGnssSystem = findViewById(R.id.sppsetting_GnssSystem);
        frequencyText = findViewById(R.id.sppsetting_frequency);
        recordText=findViewById((R.id.sppsetting_Record));


        //初始化显示
        reloadSettingText();

        findViewById(R.id.sppsetting_btnGnssSystem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 从 SharedPreferences 中读取之前的选择 创建预选项数组
                boolean isGpsSelected = sharedPreferences.getInt(Constants.KEY_GPS_SYSTEM, Constants.DEF_GPS_SYSTEM) == 1;
                boolean isGloSelected = sharedPreferences.getInt(Constants.KEY_GLO_SYSTEM, Constants.DEF_GLO_SYSTEM) == 1;
                boolean isGalSelected = sharedPreferences.getInt(Constants.KEY_GAL_SYSTEM, Constants.DEF_GAL_SYSTEM) == 1;
                boolean isBdsSelected = sharedPreferences.getInt(Constants.KEY_BDS_SYSTEM, Constants.DEF_BDS_SYSTEM) == 1;
                boolean isQzssSelected = sharedPreferences.getInt(Constants.KEY_QZSS_SYSTEM, Constants.DEF_QZSS_SYSTEM) == 1;
                List<Integer> selectedIndices = new ArrayList<>();
                if (isGpsSelected) selectedIndices.add(0);
                if (isGloSelected) selectedIndices.add(1);
                if (isGalSelected) selectedIndices.add(2);
                if (isBdsSelected) selectedIndices.add(3);
                if (isQzssSelected) selectedIndices.add(4);
                Integer[] selectedIndicesArray = selectedIndices.toArray(new Integer[0]);


                String[] system = {
                        "GPS",
                        "GLO",
                        "GAL",
                        "BDS",
                        "QZSS"
                };
                new MaterialDialog.Builder(view.getContext())
                        .title(R.string.system_gnss)// 标题
                        .items(system)// 列表数据
                        // itemsCallbackMultiChoice 方法中的第一个参数代表预选项的值，没有预选项这个值就设置为 null，有预选项就传入一组预选项的索引值即可。
                        .itemsCallbackMultiChoice(selectedIndicesArray, new MaterialDialog.ListCallbackMultiChoice() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                return true;
                            }

                        })
                        // 如果没有使用 positiveText() 设置正面操作按钮，则当用户按下正面操作按钮时，
                        // 对话框将自动调用多项选择回调方法，该对话框也将自行关闭，除非关闭自动关闭。
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                Log.d(TAG, ":::" + Arrays.toString(dialog.getSelectedIndices()));
                                Integer[] index = dialog.getSelectedIndices();
                                textViewGnssSystem.setText("");
                                for (int i = 0; i < index.length; i++) {
                                    switch (index[i]) {
                                        case 0:
                                            sharedPreferences.edit().putInt(Constants.KEY_GPS_SYSTEM, 1).apply();
                                            textViewGnssSystem.append("GPS");
                                            break;
                                        case 1:
                                            sharedPreferences.edit().putInt(Constants.KEY_GLO_SYSTEM, 1).apply();
                                            textViewGnssSystem.append("/GLO");
                                            break;
                                        case 2:
                                            sharedPreferences.edit().putInt(Constants.KEY_GAL_SYSTEM, 1).apply();
                                            textViewGnssSystem.append("/GAL");
                                            break;
                                        case 3:
                                            sharedPreferences.edit().putInt(Constants.KEY_BDS_SYSTEM, 1).apply();
                                            textViewGnssSystem.append("/BDS");
                                            break;
                                        case 4:
                                            sharedPreferences.edit().putInt(Constants.KEY_QZSS_SYSTEM, 1).apply();
                                            textViewGnssSystem.append("/QZSS");
                                            break;
                                    }
                                }
                            }
                        })
                        .positiveText("确认")
                        // 如果调用 alwaysCallMultiChoiceCallback() 该方法，则每次用户选择/取消项目时都会调用多项选择回调方法。
                        .alwaysCallMultiChoiceCallback()
                        .show();// 显示对话框
            }
        });

        findViewById(R.id.sppsetting_btnfrequency).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] frequencyOptions = {"单频", "双频"};
                int selectedIndex = sharedPreferences.getInt(Constants.KEY_doublefrequency, Constants.DEF_frequency);

                new MaterialDialog.Builder(SettingActivity.this)
                        .title("选择频率")
                        .items(frequencyOptions)
                        //单选回调，预选项为 selectedIndex
                        .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            //which 参数表示用户选择的选项的索引
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                // 更新频率文本
                                frequencyText.setText(text);
                                // 保存状态到 SharedPreferences
                                sharedPreferences.edit().putInt(Constants.KEY_doublefrequency, which).apply();
                                return true;
                            }
                        })
                        .positiveText("确认")
                        .show();
            }
        });

        findViewById(R.id.sppsetting_btnRecord).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] frequencyOptions = {"否", "是"};
                int selectedIndex = sharedPreferences.getInt(Constants.KEY_Record, Constants.DEF_Record);

                new MaterialDialog.Builder(SettingActivity.this)
                        .title("是否记录观测文件")
                        .items(frequencyOptions)
                        //单选回调，预选项为 selectedIndex
                        .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            //which 参数表示用户选择的选项的索引
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                // 更新频率文本
                                recordText.setText(text);
                                // 保存状态到 SharedPreferences
                                sharedPreferences.edit().putInt(Constants.KEY_Record, which).apply();
                                return true;
                            }
                        })
                        .positiveText("确认")
                        .show();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void reloadSettingText() {

        //默认状态下未进行卫星系统的选择
        textViewGnssSystem.setText("");
        int gps = sharedPreferences.getInt(Constants.KEY_GPS_SYSTEM, Constants.DEF_GPS_SYSTEM);
        if (gps == 1) textViewGnssSystem.append("GPS");
        int gal = sharedPreferences.getInt(Constants.KEY_GAL_SYSTEM, Constants.DEF_GAL_SYSTEM);

        if (gal == 1) textViewGnssSystem.append("/GAL");
        int glo = sharedPreferences.getInt(Constants.KEY_GLO_SYSTEM, Constants.DEF_GLO_SYSTEM);
        if (glo == 1) textViewGnssSystem.append("/GLO");
        int bds = sharedPreferences.getInt(Constants.KEY_BDS_SYSTEM, Constants.DEF_BDS_SYSTEM);
        if (bds == 1) textViewGnssSystem.append("/BDS");
        int qzss = sharedPreferences.getInt(Constants.KEY_QZSS_SYSTEM, Constants.DEF_QZSS_SYSTEM);
        if (qzss == 1) textViewGnssSystem.append("/QZSS");


        //单双频文本初始化
        int isSingleFrequency = sharedPreferences.getInt(Constants.KEY_doublefrequency, Constants.DEF_frequency);
        frequencyText.setText(isSingleFrequency!=1 ? "单频" : "双频");

        //是否记录观测文件初始化
        int isRecord = sharedPreferences.getInt(Constants.KEY_Record, Constants.DEF_Record);
        frequencyText.setText(isRecord!=1 ? "否" : "是");
    }
}