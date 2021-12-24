package com.example.suivideparcours;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class Historique extends AppCompatActivity {

    RelativeLayout rl;
    ListView list;
    Button bClear;
    SharedPreferences prefs;
    String[] tabHistorique;
    int size;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        rl = findViewById(R.id.historique);
        list = findViewById(R.id.listView);

        initHistorique();
        buttonClear();
    }

    private void initHistorique(){
        prefs = this.getSharedPreferences("myPrefsKey", Context.MODE_PRIVATE);
        size = prefs.getInt("size",0);
        tabHistorique = new String[size];
        for(int i=0; i<size; i++)
            tabHistorique[i] = prefs.getInt("pas_" + i, 0) + " pas    |     "
                    + prefs.getFloat("distance_" + i, 0) + " m    |     "
                    + prefs.getFloat("vitesseMoyenne_" + i, 0) + " km/h\n            "
                    + prefs.getString("date_" + i, null) ;

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.activity_historique, R.id.textView, tabHistorique);
        list.setAdapter(arrayAdapter);
    }

    private void buttonClear(){
        bClear = new Button(this);
        bClear.setWidth(300);
        bClear.setX(380);
        bClear.setText("Effacer");
        rl.addView(bClear);
        if(size == 0) bClear.setEnabled(false);
        bClear.setOnClickListener(view -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });
    }
}