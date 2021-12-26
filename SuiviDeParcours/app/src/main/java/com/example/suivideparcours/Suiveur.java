package com.example.suivideparcours;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Suiveur extends AppCompatActivity
        implements GoogleMap.OnMyLocationChangeListener,
        OnMapReadyCallback {

    TextView tv;
    GoogleMap map;
    Polyline polyline;
    Location initialPosition = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suiveur);

        Intent intentAppelant = getIntent();
        String numeroMarcheur = intentAppelant.getStringExtra("numeroMarcheur");

        tv = findViewById(R.id.numero);
        tv.setText("Suivi de : "+numeroMarcheur);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        PolylineOptions polylineOpts = new PolylineOptions()
                .color(Color.RED)
                .geodesic(true);

        polyline = map.addPolyline(polylineOpts);

        map.setMyLocationEnabled(true);
        map.setOnMyLocationChangeListener(this);
    }

    @Override
    public void onMyLocationChange(Location location) {
        if(initialPosition == null){
            MarkerOptions mp = new MarkerOptions();
            mp.position(new LatLng(location.getLatitude(), location.getLongitude()));
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date();
            mp.title("Départ ("+dateFormat.format(date)+")");
            map.addMarker(mp);
        }

        initialPosition = location;

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 16));

        List<LatLng> points = polyline.getPoints();
        points.add(new LatLng(location.getLatitude(), location.getLongitude()));
        polyline.setPoints(points);
    }

    public void buttonStop(View v) {
        this.finish();
    }
}