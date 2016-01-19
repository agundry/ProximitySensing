package com.example.austin.proximitysensing;

import android.app.ProgressDialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class EmbeddedSensorActivity extends AppCompatActivity implements SensorEventListener {

    TextView proxText;
    SensorManager mSensorManager;
    Sensor mProximity;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_embedded_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Registering the proximity sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proxText = (TextView) findViewById(R.id.proximityTextView);

        // Set up the listener
        mSensorManager.registerListener((SensorEventListener) this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        proxText.setText(String.valueOf(event.values[0]));

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("value", event.values[0]);
//            new SensorClient().execute("http://192.168.1.124:8000", jsonObject.toString());
            executeAsyncTask(new SensorClient(), "http://24.12.195.46:8000", jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_embedded_sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static public <T> void executeAsyncTask(AsyncTask<T, ?, ?> task,
                                            T... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
        else {
            task.execute(params);
        }
    }

    public class SensorClient extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            String u = "http://192.168.1.124:8000/sensor?value="+params[1].toString();
            HttpURLConnection urlConnection = null;
            JSONArray response = new JSONArray();

            try {
                url = new URL(u);
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("GET");
                OutputStream os = urlConnection.getOutputStream();
                os.write(params[1].getBytes());
                os.flush();
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return null;
        }
    }
}
