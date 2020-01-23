package com.example.mazharali.projectfyp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnCustomer , btnMechanic ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCustomer = (Button)findViewById(R.id.btn_customer);
        btnMechanic = (Button)findViewById(R.id.btn_mechanic);

        startService(new Intent(MainActivity.this, onAppKill.class));

        btnMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this , MechanicLoginAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        btnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this , CustomerLoginAct.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}