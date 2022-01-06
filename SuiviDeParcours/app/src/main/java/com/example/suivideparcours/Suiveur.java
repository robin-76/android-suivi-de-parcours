package com.example.suivideparcours;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.SEND_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Suiveur extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final int PERMISSION_FOR_STORAGE = 0;
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

        if (!checkPermission())
            verifyPermissions();

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

                    else {
                        MarkerOptions mp = new MarkerOptions();
                        mp.position(new LatLng(latitude, longitude));
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        Date date = new Date();
                        mp.title("Position du marcheur ("+dateFormat.format(date)+")");
                        mp.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                        map.addMarker(mp);
                    }

                    initialPosition = true;

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(latitude, longitude), 18));

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

    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    public void verifyPermissions() {
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_FOR_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int code, String permissions[], int grantResults[]) {
        switch (code) {
            case PERMISSION_FOR_STORAGE: {
                boolean ok = true;
                if(grantResults.length > 0) {
                    for(int res=0; res<grantResults.length; res++) {
                        if(grantResults[res] != PackageManager.PERMISSION_GRANTED)
                            ok = false;
                    }
                } else ok = false;
                if(ok) {
                    Toast.makeText(this, "Stockage autorisé", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "Stockage non autorisé", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void buttonScreen(View v) {
        map.setOnMapLoadedCallback(() -> map.snapshot(bitmap -> {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream("/mnt/sdcard/map.png");
                Toast.makeText(Suiveur.this,"Image enregistrée",
                        Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        }));
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