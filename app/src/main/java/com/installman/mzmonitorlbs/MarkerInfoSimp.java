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
    String gMrkTitle;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d("MarkerInfoSimp", "Task id is " + getTaskId());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.marker_info_simp_layout);
        Intent intent = getIntent();
        gMrkTitle = intent.getStringExtra("gMrkTitle");

        TextView tvTitle = (TextView) findViewById(R.id.textView_info_name);
        tvTitle.setText(gMrkTitle);

        Button btnModify = (Button) findViewById(R.id.button_info_modify);
        View.OnClickListener buttonModifyListener = new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(MarkerInfoSimp.this, MarkerModifier.class);
                i.putExtra("gMrkTitle", gMrkTitle);
                startActivity(i);
                finish();
            }
        };
        btnModify.setOnClickListener(buttonModifyListener);

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
