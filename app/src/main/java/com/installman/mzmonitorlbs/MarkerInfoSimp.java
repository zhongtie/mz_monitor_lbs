package com.installman.mzmonitorlbs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by zhong on 16-3-22.
 */
public class MarkerInfoSimp extends Activity {
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("MarkerInfoSimp", "Task id is " + getTaskId());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marker_info_simp_layout);

        Button buttonModify = (Button) findViewById(R.id.button_info_modify);
        View.OnClickListener buttonModifyOnClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MarkerInfoSimp.this, MarkerModifier.class);
                startActivity(intent);
                finish();
            }
        };
        buttonModify.setOnClickListener(buttonModifyOnClickListener);
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
