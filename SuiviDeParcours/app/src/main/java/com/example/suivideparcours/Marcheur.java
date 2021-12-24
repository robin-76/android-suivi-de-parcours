package com.example.suivideparcours;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Marcheur extends AppCompatActivity
        implements
        SensorEventListener,
        GoogleMap.OnMyLocationChangeListener,
        OnMapReadyCallback {

    private static final int PERMISSION_FOR_LOCATION = 1;
    private GoogleMap map;

    TextView tvPas, tvVitesse, tvDistance, tvVitesseMoyenne, tvStop;
    SensorManager sensorManager;
    Sensor stepSensor;
    float steps = 0, distance = 0, vitesseMoyenne = 0;
    Location initialPosition = null;
    double somme = 0;
    int nb = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marcheur);

        tvPas = findViewById(R.id.pas);
        tvVitesse = findViewById(R.id.vitesse);
        tvDistance = findViewById(R.id.distance);
        tvVitesseMoyenne = findViewById(R.id.vitesseMoyenne);
        tvStop = findViewById(R.id.buttonStop);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.INVISIBLE);
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
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(46.62, 1.85), 5)); // Centre de la France
        verifyPermissions();
        map.setOnMyLocationChangeListener(this);
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
        if(initialPosition == null){
            initialPosition = location;

            map.clear();
            MarkerOptions mp = new MarkerOptions();
            mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
            mp.title("Départ");
            map.addMarker(mp);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 16));
        }

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

        this.finish();
    }

}
