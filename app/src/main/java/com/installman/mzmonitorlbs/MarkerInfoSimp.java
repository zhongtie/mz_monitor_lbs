package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Intent;
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
    double gLocLat, gLocLng;
    protected final String EXTRA_ACTION = "ACTION";
    protected final String EXTRA_ACTION_MODIFY = "MODIFY";
    protected final String EXTRA_TITLE = "MRK_TITLE";
    protected final String EXTRA_LATITUDE = "MRK_LATITUDE";
    protected final String EXTRA_LONGTITUDE = "MRK_LONGITUDE";

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("MarkerInfoSimp", "Task id is " + getTaskId());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marker_info_simp_layout);
        Intent intent = getIntent();
        gAction = intent.getStringExtra(EXTRA_ACTION);
        gMrkTitle = intent.getStringExtra(EXTRA_TITLE);
        gLocLat = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0);
        gLocLng = intent.getDoubleExtra(EXTRA_LONGTITUDE, 0.0);

        TextView tvTitle = (TextView) findViewById(R.id.textView_info_name);
        tvTitle.setText(gMrkTitle);

        Button btnModify = (Button) findViewById(R.id.button_info_modify);
        View.OnClickListener btnModifyClick = new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MarkerInfoSimp.this, MarkerModifier.class);
                i.putExtra(EXTRA_ACTION, EXTRA_ACTION_MODIFY);
                i.putExtra(EXTRA_TITLE, gMrkTitle);
                i.putExtra(EXTRA_LATITUDE, gLocLat);
                i.putExtra(EXTRA_LONGTITUDE, gLocLng);
                startActivity(i);
                finish();
            }
        };
        btnModify.setOnClickListener(btnModifyClick);

        Button btnCancel = (Button) findViewById(R.id.button_info_cancel);
        View.OnClickListener buttonCancelListener = new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        };
        btnCancel.setOnClickListener(buttonCancelListener);
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
