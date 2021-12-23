package com.example.suivideparcours;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class Marcheur extends AppCompatActivity implements SensorEventListener,
        GoogleMap.OnMyLocationChangeListener, OnMapReadyCallback {

    TextView tvPas, tvVitesse, tvDistance, tvVitesseMoyenne;
    SensorManager sensorManager;
    Sensor stepSensor;
    float steps = 0;
    GoogleMap map;
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

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
            float distance = (steps*78) / 100;
            tvDistance.setText("Distance : "+String.format("%.2f", distance)+" m");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(this);
    }

    @Override
    public void onMyLocationChange(@NonNull Location location) {
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
        tvVitesseMoyenne.setText("Vitesse moyenne : "+ String.format("%.2f", somme / nb)+" km/h");
    }
}


