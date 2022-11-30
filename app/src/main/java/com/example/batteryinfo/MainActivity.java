package com.example.batteryinfo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    int view = R.layout.activity_main;

    boolean charging;
    float Volts;
    long max;
    long rem;
    long curr;

    IntentFilter intentfilter;

    ProgressBar batpct;
    ProgressBar a;
    ProgressBar v;
    ProgressBar w;
    ProgressBar progressBar;

    TextView amps;
    TextView volts;
    TextView watts;
    TextView BatDat;

    protected String Current() {
        a = findViewById(R.id.a);
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            curr = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            long curravg = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
            a.setProgress((int) Math.abs(curr));
            a.setMax(2000000);
          return ((curr*1.0) / 1000 + " mA");
        }
        return null;
    }

    protected String Volts() {
        v = findViewById(R.id.v);
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Volts = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                    Volts = (float) (Volts*0.001);
                }
            };
            this.registerReceiver(broadcastReceiver,intentfilter);
            v.setProgress((int) (Volts*1000000));
            v.setMax(4400000);
            return Volts+" V";
        }
        return null;
    }

    protected String Watts() {
        w = findViewById(R.id.w);
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            float wt = Volts*Math.abs(curr/100000)/10;
            w.setMax(1300);
            w.setProgress(Math.round(wt*100));
            return wt+" vA";
        }
        return null;
    }

    protected String BatPct() {
        batpct = findViewById(R.id.batpct);
        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            long trem = bm.computeChargeTimeRemaining();
            int percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            batpct.setProgress(percentage);
            if (bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == 2) {
                charging = true;
                batpct.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            }
            else if (bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == 3) {
                charging = false;
                batpct.setProgressTintList(ColorStateList.valueOf(Color.RED));
            }
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(trem), TimeUnit.MILLISECONDS.toMinutes(trem) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(trem)), TimeUnit.MILLISECONDS.toSeconds(trem) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(trem)));
            String ret;
            if (charging) {
                ret = percentage+"% | "+hms+" to full charge";
            } else {
                ret = percentage+"%";
            }
            return ret;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        byte testByte = 0;
        amps = findViewById(R.id.amps);
        volts = findViewById(R.id.volts);
        watts = findViewById(R.id.watts);
        BatDat = findViewById(R.id.BatDat);
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                while (testByte == 0) {
                    try {
                        Thread.sleep(1000); // Waits for 1 second (1000 milliseconds)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    amps.post(new Runnable() {
                        @Override
                        public void run() {
                            amps.setText(Current());
                        }
                    });

                    BatDat.post(new Runnable() {
                        @Override
                        public void run() {
                            BatDat.setText(BatPct());
                        }
                    });

                    volts.post(new Runnable() {
                        @Override
                        public void run() {
                            volts.setText(Volts());
                        }
                    });
                    watts.post(new Runnable() {
                        @Override
                        public void run() {
                            watts.setText(Watts());
                        }
                    });
                }
            }
        };
        Thread myThread = new Thread(myRunnable);
        myThread.start();

    }
}