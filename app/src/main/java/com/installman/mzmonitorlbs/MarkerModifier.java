package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.baidu.mapapi.map.BitmapDescriptor;

/**
 * Created by zhong on 16-3-21.
 */

public class MarkerModifier extends Activity {
    //覆盖物相关
    protected BitmapDescriptor mCurrentMarker;
    protected int mrkAngle = 0;//默认的覆盖物方向
    protected int angleInc = 90;//角度每次增加幅度
    protected String mrkTitle, oldMrkTitle;
    protected MonitorLbs.eMonitorType mMonitorType;

    //数据库存储相关
    DatabaseHelper mDbHelper;
    SQLiteDatabase mDatabase;

    //UI相关
    RadioGroup mRadioGroup;
    EditText mEditText;
    Button mButtonAngle, mButtonOk, mButtonCancel;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("MarkerModifier", "Task id is " + getTaskId());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marker_modifier_layout);

        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
        WindowManager.LayoutParams p = getWindow().getAttributes();  //获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.5);   //高度设置为屏幕的1.0
        p.width = (int) (d.getWidth() * 0.8);    //宽度设置为屏幕的0.8
        p.alpha = 0.7f;      //设置本身透明度
        p.dimAmount = 0.0f;      //设置黑暗度
        getWindow().setAttributes(p);

        //获取activity传过来的参数
        Intent intentGet = getIntent();
        oldMrkTitle = intentGet.getStringExtra("mrkTitle");

        //设置类型
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroupType);
        RadioGroup.OnCheckedChangeListener radioGroupCheckListener = new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioButtonBall){
                    mMonitorType = MonitorLbs.eMonitorType.BALL;
                }else if(checkedId == R.id.radioButtonGun){
                    mMonitorType = MonitorLbs.eMonitorType.GUN;
                }else if(checkedId == R.id.radioButtonSmart){
                    mMonitorType = MonitorLbs.eMonitorType.SMART;
                }else{
                    mMonitorType = MonitorLbs.eMonitorType.BALL;
                }
            }
        };
        mRadioGroup.setOnCheckedChangeListener(radioGroupCheckListener);

        //设置名称
        mEditText = (EditText) findViewById(R.id.editText_modi_name);
        mEditText.setText(oldMrkTitle);

        //设置角度，初始角度都是0
        mButtonAngle = (Button) findViewById(R.id.button_modi_angl);
        View.OnClickListener angleClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                //每点击一次角度增加
                mrkAngle = (mrkAngle + angleInc) % 360;
            }
        };
        mButtonAngle.setOnClickListener(angleClickListener);

        //确认修改
        //后台数据库sqlite存储
        mDbHelper = new DatabaseHelper(this);
        mDatabase = mDbHelper.getWritableDatabase();

        mButtonOk = (Button) findViewById(R.id.button_modi_ok);
        View.OnClickListener okClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                mrkTitle = mEditText.getText().toString();
                mDatabase.execSQL("update mzMonitor set title = ?, monitor_type = ?, monitor_angle = ?" +
                                " where title = ?",
                        new Object[]{mrkTitle, mMonitorType.ordinal(), mrkAngle, oldMrkTitle});
                finish();
            }
        };
        mButtonOk.setOnClickListener(okClickListener);

        //取消返回
        mButtonCancel = (Button) findViewById(R.id.button_modi_cancel);
        View.OnClickListener cancelClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        };
        mButtonCancel.setOnClickListener(cancelClickListener);
    }

    protected void onPause(){
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onRestart() {
        super.onRestart();
    }

    protected void onResume() {
        super.onResume();
    }
}
