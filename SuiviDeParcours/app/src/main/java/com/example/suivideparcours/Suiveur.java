package com.example.suivideparcours;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Suiveur extends AppCompatActivity
        implements OnMapReadyCallback {

    TextView tv;
    GoogleMap map;
    Polyline polyline;
    boolean initialPosition = false;
    double latitude, longitude;
    String numeroMarcheur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suiveur);

        Intent intentAppelant = getIntent();
        numeroMarcheur = intentAppelant.getStringExtra("numeroMarcheur");

        tv = findViewById(R.id.numero);
        tv.setText("Suivi de : "+numeroMarcheur);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        broadcastReceiver();
    }

    public void broadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Bundle intentAppelant = intent.getExtras();
                if (action.equals("position")) {
                    latitude = intentAppelant.getDouble("latitude");
                    longitude = intentAppelant.getDouble("longitude");

                    if(initialPosition == false) {
                        MarkerOptions mp = new MarkerOptions();
                        mp.position(new LatLng(latitude, longitude));
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date date = new Date();
                        mp.title("Début du suivi ("+dateFormat.format(date)+")");
                        map.addMarker(mp);
                    }

                    initialPosition = true;

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latitude, longitude), 18));

                    MarkerOptions mp = new MarkerOptions();
                    mp.position(new LatLng(latitude, longitude));
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    Date date = new Date();
                    mp.title("Position du marcheur ("+dateFormat.format(date)+")");
                    mp.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    map.addMarker(mp);

                    List<LatLng> points = polyline.getPoints();
                    points.add(new LatLng(latitude, longitude));
                    polyline.setPoints(points);
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("position"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        PolylineOptions polylineOpts = new PolylineOptions()
                .color(Color.RED)
                .geodesic(true);

        polyline = map.addPolyline(polylineOpts);
    }

    public void buttonStop(View v) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(numeroMarcheur,
                    null,
                    "Stop",
                    null,
                    null);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),"Votre sms a échoué... " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        this.finish();
    }
}