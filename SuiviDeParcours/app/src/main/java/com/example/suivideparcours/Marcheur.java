package com.example.suivideparcours;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Marcheur extends AppCompatActivity
        implements
        SensorEventListener,
        GoogleMap.OnMyLocationChangeListener,
        OnMapReadyCallback {

    private static final int PERMISSION_FOR_LOCATION = 1;
    private GoogleMap map;

    TextView tvPas, tvVitesse, tvDistance, tvVitesseMoyenne;
    SensorManager sensorManager;
    Sensor stepSensor;
    float steps = 0, distance = 0, vitesseMoyenne = 0;
    double somme = 0;
    int nb = 0;
    String numeroSuiveur = " ";
    boolean reponse = false;
    double myLatitude, myLongitude;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcheur);

        tvPas = findViewById(R.id.pas);
        tvVitesse = findViewById(R.id.vitesse);
        tvDistance = findViewById(R.id.distance);
        tvVitesseMoyenne = findViewById(R.id.vitesseMoyenne);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);

        broadcastReceiver();
        timer();
    }

    public void broadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle intentAppelant = intent.getExtras();
                if (action.equals("numeroSuiveur")){
                    numeroSuiveur = intentAppelant.getString("numeroSuiveur");
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Demande de suivi");
                    builder.setMessage(numeroSuiveur+" souhaite suivre votre parcours.");
                    builder.setNegativeButton("Refuser", (dialog, which) -> {
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(numeroSuiveur,
                                    null,
                                    "Non",
                                    null,
                                    null);

                            Toast.makeText(context,"Votre sms a bien été envoyé",
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            Toast.makeText(context,"Votre sms a échoué... " + ex.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        dialog.cancel();
                    });

                    builder.setPositiveButton("Accepter", (dialog, which) -> {
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(numeroSuiveur,
                                    null,
                                    "Oui",
                                    null,
                                    null);

                            Toast.makeText(context,"Votre sms a bien été envoyé",
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            Toast.makeText(context,"Votre sms a échoué... " + ex.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                        reponse = true;
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                if (action.equals("reponse"))
                    reponse = intentAppelant.getBoolean("reponse");
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("numeroSuiveur"));
        registerReceiver(broadcastReceiver, new IntentFilter("reponse"));
    }

    public void timer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(myLatitude!=0 && myLongitude!=0 && reponse){
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(numeroSuiveur,
                                null,
                                "Position;"+myLatitude +";"+ myLongitude,
                                null,
                                null);
                    } catch (Exception ex) {
                        Toast.makeText(Marcheur.this,"Votre sms a échoué... " + ex.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }, 0, 10000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, stepSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            steps++;
            tvPas.setText("Nombre de pas : "+(int)steps);
            distance = (steps*78) / 100;
            tvDistance.setText("Distance : "+String.format("%.2f", distance)+" m");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        verifyPermissions();
        locationActivated();
        map.setOnMyLocationChangeListener(this);
    }

    public void locationActivated() {
        if(myLatitude==0 && myLongitude==0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Marcheur.this);
            builder.setMessage("Veuillez activer votre localisation");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void localizeMe(){
        map.setMyLocationEnabled(true);
    }

    public void verifyPermissions() {
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
            localizeMe();
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FOR_LOCATION);
        else
            localizeMe();
        }

    @Override
    public void onRequestPermissionsResult(int code, String permissions[], int grantResults[]) {
        switch (code) {
            case PERMISSION_FOR_LOCATION: {
                boolean ok = true;
                if(grantResults.length > 0) {
                    for(int res=0; res<grantResults.length; res++) {
                        if(grantResults[res] != PackageManager.PERMISSION_GRANTED)
                            ok = false;
                    }
                } else ok = false;
                if(ok)
                    localizeMe();
                else
                    Toast.makeText(this, "Localisation non autorisée", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        myLatitude = location.getLatitude();
        myLongitude = location.getLongitude();

        tvVitesse.setText("Vitesse : "+ String.format("%.2f", location.getSpeed()*3.6)+" km/h");

        somme += location.getSpeed()*3.6;
        nb++;
        vitesseMoyenne = (float) (somme/nb);
        tvVitesseMoyenne.setText("Vitesse moyenne : "+ String.format("%.2f", vitesseMoyenne)+" km/h");
    }

    public void buttonStop(View v) {
        SharedPreferences prefs = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date date = new Date();
        int nb = prefs.getInt("size",0);
        editor.putInt("size",nb+1);
        editor.putInt("pas_"+nb, (int)steps);
        editor.putFloat("distance_"+nb, distance);
        editor.putFloat("vitesseMoyenne"+nb, vitesseMoyenne);
        editor.putString("date_"+nb, dateFormat.format(date));
        editor.apply();

        timer.cancel();
        this.finish();
    }
}
