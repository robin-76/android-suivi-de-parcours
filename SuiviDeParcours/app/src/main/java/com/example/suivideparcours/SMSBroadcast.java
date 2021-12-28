package com.example.suivideparcours;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSBroadcast extends BroadcastReceiver {
    private static String SMS = "android.provider.Telephony.SMS_RECEIVED";
    SmsMessage messages;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(SMS)) {
            Bundle bundle = intent.getExtras();

            Object[] objects = (Object[]) bundle.get("pdus");

            for(int i=0; i<objects.length; i++)
                messages = SmsMessage.createFromPdu((byte[]) objects[i]);

            if(messages.getMessageBody().equals("Puis-je suivre votre parcours ?")) {
                Intent intentNumeroSuiveur = new Intent("numeroSuiveur");
                intentNumeroSuiveur.putExtra("numeroSuiveur",messages.getDisplayOriginatingAddress());
                context.sendBroadcast(intentNumeroSuiveur);
            }

            else if(messages.getMessageBody().equals("Oui")){
                Intent intentSuiveur = new Intent(context,Suiveur.class);
                intentSuiveur.putExtra("numeroMarcheur",messages.getDisplayOriginatingAddress());
                intentSuiveur.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intentSuiveur);
            }

            else if(messages.getMessageBody().equals("Non"))
                Toast.makeText(context, "Vous ne pouvez pas suivre le marcheur",
                        Toast.LENGTH_SHORT).show();

            else if(messages.getMessageBody().contains("Position;")){
                String[] latLong = messages.getMessageBody().split(";");
                double latitude = Double.parseDouble(latLong[1]);
                double longitude = Double.parseDouble(latLong[2]);

                Intent i = new Intent("position");
                i.putExtra("latitude",latitude);
                i.putExtra("longitude",longitude);
                context.sendBroadcast(i);
            }

            else if(messages.getMessageBody().equals("Stop")){
                Toast.makeText(context, "Fin de suivi",
                        Toast.LENGTH_SHORT).show();

                Intent intentStop = new Intent("reponse");
                intentStop.putExtra("reponse",false);
                context.sendBroadcast(intentStop);
            }
        }
    }
}
