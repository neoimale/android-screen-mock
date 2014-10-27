package com.dianping.screenmock;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final CharSequence[] RESERVED_SCREEN = { "1080x1920,xxhdpi (1080p)",
            "720x1280,xhdpi (720p)", "480x800,hdpi", "480x854,hdpi", "320x480,mdpi" };

    private Process suProcess;
    private DataOutputStream cmdOutput;

    private View pickBtn;
    private EditText customInput;
    private Button applyBtn, resetBtn;
    private CheckedTextView serviceSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initSuProcess();
        stopService(new Intent(MainActivity.this, FloatWindowService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suProcess != null) {
            try {
                cmdOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            suProcess.destroy();
        }
        if (serviceSwitcher.isChecked())
            startService(new Intent(this, FloatWindowService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        pickBtn = findViewById(R.id.pick_size);
        customInput = (EditText) findViewById(R.id.custom_size);
        applyBtn = (Button) findViewById(R.id.apply);
        resetBtn = (Button) findViewById(R.id.reset);
        serviceSwitcher = (CheckedTextView) findViewById(R.id.enable_service);

        pickBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(RESERVED_SCREEN, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                execInSuProcess("wm size 1080x1920 \n wm density 480");
                                break;
                            case 1:
                                execInSuProcess("wm size 720x1280 \n wm density 320");
                                break;
                            case 2:
                                execInSuProcess("wm size 480x800 \n wm density 240");
                                break;
                            case 3:
                                execInSuProcess("wm size 480x854 \n wm density 240");
                                break;
                            case 4:
                                execInSuProcess("wm size 320x480 \n wm density 160");
                                break;
                            default:
                                break;
                        }
                    }
                }).create().show();
            }
        });
        applyBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String input = customInput.getText().toString();
                int w = 0, h = 0, d = 0;
                if (input != null) {
                    try {
                        if (input.contains(",")) {
                            String density = input.substring(input.indexOf(",") + 1);
                            d = Integer.parseInt(density);
                        }
                        if (input.contains("x")) {
                            int index = input.indexOf("x");
                            String width = input.substring(0, index);
                            w = Integer.parseInt(width);
                            int end = input.contains(",") ? input.indexOf(",") : input.length();
                            String height = input.substring(index + 1, end);
                            h = Integer.parseInt(height);
                        }
                        if (w > 0 && h > 0) {
                            execInSuProcess("wm size " + w + "x" + h);
                        }
                        if (d > 0) {
                            execInSuProcess("wm density " + d);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Input Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        resetBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                execInSuProcess("wm size reset \n wm density reset");
            }
        });
        serviceSwitcher.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                serviceSwitcher.toggle();
                if (!serviceSwitcher.isChecked()) {
                    stopService(new Intent(MainActivity.this, FloatWindowService.class));
                }
            }
        });
    }

    private void initSuProcess() {
        try {
            suProcess = Runtime.getRuntime().exec("su");
            cmdOutput = new DataOutputStream(suProcess.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void execInSuProcess(String cmd) {
        if (suProcess != null) {
            try {
                cmdOutput.writeBytes(cmd + "\n");
                cmdOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
