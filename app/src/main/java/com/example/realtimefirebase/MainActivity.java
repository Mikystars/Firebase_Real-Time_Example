package com.example.realtimefirebase;

import static android.content.ContentValues.TAG;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    SensorManager sm;
    TextView datosLocalZ;
    TextView datosLocalX;
    TextView datosLocalY;
    TextView datosFirebaseZ;
    TextView datosFirebaseX;
    TextView datosFirebaseY;
    EditText dbIDguardar;
    EditText dbIDLeer;
    Button btOn;
    SeekBar seekBar;
    int seekBarPosition;

    FirebaseDatabase database;
    DatabaseReference myRef;

    List list;


    SensorEventListener sel = new SensorEventListener(){
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            datosLocalX.setText("" + values[0]);
            datosLocalY.setText("" + values[1]);
            datosLocalZ.setText("" + values[2]);
            myRef.child("x").setValue(event.values[0]);
            myRef.child("y").setValue(event.values[1]);
            myRef.child("z").setValue(event.values[2]);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datosLocalX = (TextView) findViewById(R.id.textViewXrot);
        datosLocalY = (TextView) findViewById(R.id.textViewYrot);
        datosLocalZ = (TextView) findViewById(R.id.textViewZrot);
        datosFirebaseX = (TextView) findViewById(R.id.textViewXrotServer);
        datosFirebaseY = (TextView) findViewById(R.id.textViewYrotServer);
        datosFirebaseZ = (TextView) findViewById(R.id.textViewZrotServer);
        dbIDguardar = (EditText) findViewById(R.id.dbIDguardar);
        dbIDLeer = (EditText) findViewById(R.id.dbIDleer);
        btOn = (Button) findViewById(R.id.btON);
        seekBar = (SeekBar) findViewById(R.id.seekBar);



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                myRef.child("Barra").child("valor").setValue(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Write a message to the database
                database = FirebaseDatabase.getInstance("https://ejemplo-real-time-db-default-rtdb.europe-west1.firebasedatabase.app");
                myRef = database.getReference("Datos");


                /* Get a SensorManager instance */
                sm = (SensorManager)getSystemService(SENSOR_SERVICE);

                list = sm.getSensorList(Sensor.TYPE_GAME_ROTATION_VECTOR);
                if(list.size()>0){
                    sm.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
                }else{
                    Toast.makeText(getBaseContext(), "Error: No hay sensor de Campo Magnético.", Toast.LENGTH_LONG).show();
                }

                // Read from the database
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        float valueX = dataSnapshot.child("x").getValue(float.class);
                        float valueY = dataSnapshot.child("y").getValue(float.class);
                        float valueZ = dataSnapshot.child("z").getValue(float.class);
                        Log.d(TAG, "ValueX: " + valueX + "ValueY: " + valueY + "ValueZ: " + valueZ);
                        datosFirebaseX.setText("" + valueX);
                        datosFirebaseY.setText("" + valueY);
                        datosFirebaseZ.setText("" + valueZ);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Fallo al leer datos.", error.toException());
                    }
                });
            }
        });
    }

    @Override
    protected void onStop() {
        if(list.size()>0){
            sm.unregisterListener(sel);
        }
        super.onStop();
    }
}
