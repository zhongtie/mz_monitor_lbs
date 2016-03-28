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

/**
 * Created by zhong on 16-3-21.
 */

public class MarkerModifier extends Activity {
    //覆盖物相关
    protected int gMrkAngle = 0;//默认的覆盖物方向
    protected String gNewMrkTitle, gOldMrkTitle;
    protected MzMonitor.eMonitorType gMonitorType;

    //数据库存储相关
    DatabaseHelper gDbHelper;
    SQLiteDatabase gDatabase;

    //UI相关
    RadioGroup gRadioGroupType, gRadioGroupAngle;
    EditText gEtMrkTitle;
    Button gBtnOk, gBtnCancel;

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
        Intent i = getIntent();
        gOldMrkTitle = i.getStringExtra("gMrkTitle");

        //设置类型
        gRadioGroupType = (RadioGroup) findViewById(R.id.radioGroupType);
        gRadioGroupType.check(R.id.radioButtonBall);
        RadioGroup.OnCheckedChangeListener rgCheck = new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radioButtonBall:
                        gMonitorType = MzMonitor.eMonitorType.BALL;
                        break;
                    case R.id.radioButtonGun:
                        gMonitorType = MzMonitor.eMonitorType.GUN;
                        break;
                    case R.id.radioButtonSmart:
                        gMonitorType = MzMonitor.eMonitorType.SMART;
                        break;
                    default:
                        gMonitorType = MzMonitor.eMonitorType.BALL;
                        break;
                }
            }
        };
        gRadioGroupType.setOnCheckedChangeListener(rgCheck);

        //设置名称
        gEtMrkTitle = (EditText) findViewById(R.id.editText_modi_name);
        gEtMrkTitle.setText(gOldMrkTitle);

        //设置角度，初始角度都是0
        gRadioGroupAngle = (RadioGroup) findViewById(R.id.radioGroupAngle);
        gRadioGroupAngle.check(R.id.radioButtonUp);
        RadioGroup.OnCheckedChangeListener angleChange = new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radioButtonUp:
                        gMrkAngle = 0;
                        break;
                    case R.id.radioButtonRight:
                        gMrkAngle = 90;
                        break;
                    case R.id.radioButtonDown:
                        gMrkAngle = 180;
                        break;
                    case R.id.radioButtonLeft:
                        gMrkAngle = 270;
                        break;
                    default:
                        gMrkAngle = 0;
                        break;
                }
            }
        };
        gRadioGroupAngle.setOnCheckedChangeListener(angleChange);

        //确认修改
        //后台数据库sqlite存储
        gDbHelper = new DatabaseHelper(this);
        gDatabase = gDbHelper.getWritableDatabase();

        gBtnOk = (Button) findViewById(R.id.button_modi_ok);
        View.OnClickListener btnOkClick = new View.OnClickListener() {
            public void onClick(View v) {
                gNewMrkTitle = gEtMrkTitle.getText().toString();
                gDatabase.execSQL("update mzMonitor set title = ?, monitor_type = ?, monitor_angle = ?" +
                                " where title = ?",
                        new Object[]{gNewMrkTitle, gMonitorType.ordinal(), gMrkAngle, gOldMrkTitle});
                finish();
            }
        };
        gBtnOk.setOnClickListener(btnOkClick);

        //取消返回
        gBtnCancel = (Button) findViewById(R.id.button_modi_cancel);
        View.OnClickListener btnCancelClick = new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        };
        gBtnCancel.setOnClickListener(btnCancelClick);
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
