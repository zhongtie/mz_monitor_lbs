package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.baidu.mapapi.map.BitmapDescriptor;

/**
 * Created by zhong on 16-3-21.
 */

public class MarkerModifier extends Activity {
    //覆盖物相关
    protected BitmapDescriptor mCurrentMarker;
    protected int mrkAngle = 0;//默认的覆盖物方向
    protected String mrkTitle;
    protected double dLocLat, dLocLng;
    protected String strLocAddr;

    DatabaseHelper mDbHelper;
    SQLiteDatabase mDatabase;

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

        //后台数据库sqlite存储
        mDbHelper = new DatabaseHelper(this);
        mDatabase = mDbHelper.getWritableDatabase();

        Button btnModifyOk = (Button) findViewById(R.id.button_modi_ok);

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
