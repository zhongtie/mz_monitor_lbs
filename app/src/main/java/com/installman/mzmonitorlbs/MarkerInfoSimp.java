package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by zhong on 16-3-22.
 */
public class MarkerInfoSimp extends Activity {
    String gAction, gMrkTitle;
    int gType, gAngle;
    double gLocLat, gLocLng;

    //数据库存储相关
    DatabaseHelper gDbHelper;
    SQLiteDatabase gDatabase;

    public void onCreate(Bundle savedInstanceState){
        //后台数据库sqlite存储
        gDbHelper = new DatabaseHelper(this);
        gDatabase = gDbHelper.getWritableDatabase();

        super.onCreate(savedInstanceState);
        Log.d("MarkerInfoSimp", "Task id is " + getTaskId());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marker_info_simp_layout);

        //接收参数
        Intent intent = getIntent();
        gAction = intent.getStringExtra(MonitorLbs.EXTRA_ACTION);
        gMrkTitle = intent.getStringExtra(MonitorLbs.EXTRA_TITLE);
        gType = intent.getIntExtra(MonitorLbs.EXTRA_TYPE, 0);
        gAngle = intent.getIntExtra(MonitorLbs.EXTRA_ANGLE, 0);
        gLocLat = intent.getDoubleExtra(MonitorLbs.EXTRA_LATITUDE, 0.0);
        gLocLng = intent.getDoubleExtra(MonitorLbs.EXTRA_LONGTITUDE, 0.0);

        TextView tvTitle = (TextView) findViewById(R.id.textView_info_name);
        tvTitle.setText(gMrkTitle);

        Button btnModify = (Button) findViewById(R.id.button_info_modify);
        View.OnClickListener btnModifyClick = new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MarkerInfoSimp.this, MarkerModifier.class);
                i.putExtra(MonitorLbs.EXTRA_ACTION, MonitorLbs.EXTRA_ACTION_MODIFY);
                i.putExtra(MonitorLbs.EXTRA_TITLE, gMrkTitle);
                i.putExtra(MonitorLbs.EXTRA_TYPE, gType);
                i.putExtra(MonitorLbs.EXTRA_ANGLE, gAngle);
                i.putExtra(MonitorLbs.EXTRA_LATITUDE, gLocLat);
                i.putExtra(MonitorLbs.EXTRA_LONGTITUDE, gLocLng);
                startActivity(i);
                finish();
            }
        };
        btnModify.setOnClickListener(btnModifyClick);

        Button btnDel = (Button) findViewById(R.id.button_info_delete);
        View.OnClickListener btnDelClick = new View.OnClickListener() {
            public void onClick(View v) {
                delMarkerByTitle(gMrkTitle);
                finish();
            }
        };
        btnDel.setOnClickListener(btnDelClick);

        Button btnCancel = (Button) findViewById(R.id.button_info_cancel);
        View.OnClickListener btnCancelClick = new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        };
        btnCancel.setOnClickListener(btnCancelClick);
    }

    protected void delMarkerByTitle(String title){
        gDatabase.execSQL("delete from mzMonitor where title = ?", new Object[]{title});
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
