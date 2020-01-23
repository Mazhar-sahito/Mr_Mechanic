package com.example.mazharali.projectfyp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginAct extends AppCompatActivity {

    EditText emailCustomer, passCustomer;
    Button loginCustomer;
    TextView txtSignup, reset_password;
    private ProgressDialog progressDialog;

    FirebaseAuth cAuth;
    DatabaseReference currentUserDb;
    FirebaseAuth.AuthStateListener cAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        // widgets
        emailCustomer = (EditText) findViewById(R.id.email_customer);
        passCustomer = (EditText) findViewById(R.id.pas_customer);
        loginCustomer = (Button) findViewById(R.id.btn_login_customer);
        //txtSignup= (TextView)findViewById(R.id.txt_Signup);
        reset_password = (TextView) findViewById(R.id.reset_password);
        progressDialog = new ProgressDialog(this);


        cAuth = FirebaseAuth.getInstance();
        cAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(CustomerLoginAct.this, CustomerMapNavAct.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        loginCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailCustomer.getText().toString();
                final String pass = passCustomer.getText().toString();

                loginCustomer(email, pass);

            }
        });

        reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerLoginAct.this, ResetPasswordCustomer.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void loginCustomer(String email, String pass) {

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerLoginAct.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(CustomerLoginAct.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Customer Login");
            progressDialog.setMessage("Please wait..");
            progressDialog.show();

            cAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(CustomerLoginAct.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CustomerLoginAct.this, "SignIn successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(CustomerLoginAct.this, "SignIn Unsuccessfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        cAuth.addAuthStateListener(cAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cAuth.removeAuthStateListener(cAuthStateListener);
    }

    public void CustomerReg(View view) {
        final String email = emailCustomer.getText().toString();
        final String pass = passCustomer.getText().toString();

        registerCustomer(email, pass);

    }

    private void registerCustomer(String email, String pass) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(CustomerLoginAct.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(CustomerLoginAct.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else {

            cAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(CustomerLoginAct.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(CustomerLoginAct.this, "sign in error ...", Toast.LENGTH_SHORT).show();
                    } else {
                        String user_id = cAuth.getCurrentUser().getUid();
                        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                        currentUserDb.setValue(true);
                    }
                }
            });
        }
    }
}
