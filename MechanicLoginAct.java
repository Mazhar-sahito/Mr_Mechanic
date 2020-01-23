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

public class MechanicLoginAct extends AppCompatActivity {

    EditText emailMechanic , passMechanic;
    Button loginMechanic;
    TextView reset_pass;
    private ProgressDialog progressDialog;

    FirebaseAuth mAuth;
    DatabaseReference currentUserDb;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_login);
        // widgets
        emailMechanic = (EditText)findViewById(R.id.email_mechanic);
        passMechanic =  (EditText)findViewById(R.id.pass_mechanic);
        loginMechanic= (Button)findViewById(R.id.btn_login_mechanic);
        //txtSignup= (TextView)findViewById(R.id.txt_Signup);
        reset_pass = (TextView)findViewById(R.id.reset_password);
        progressDialog= new ProgressDialog(this);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(MechanicLoginAct.this , MechanicMapAct.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        loginMechanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailMechanic.getText().toString();
                final String pass=passMechanic.getText().toString();

                registerMechanic(email , pass);

            }
        });

        reset_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MechanicLoginAct.this, ResetPasswordCustomer.class);
                startActivity(intent);
                finish();
            }
        });


    }



    private void registerMechanic(String email, String pass) {

        if (TextUtils.isEmpty(email)){
            Toast.makeText(MechanicLoginAct.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)){
            Toast.makeText(MechanicLoginAct.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }else{
            progressDialog.setTitle("Mechanic Login");
            progressDialog.setMessage("Please wait..");
            progressDialog.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(MechanicLoginAct.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MechanicLoginAct.this, "SignIn successfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(MechanicLoginAct.this, "SignIn Unsuccessfully", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    public void MechanicReg(View view){
        final String email= emailMechanic.getText().toString();
        final String pass= passMechanic.getText().toString();

        registerCustomer(email, pass);


    }



    private void registerCustomer(String email, String pass) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(MechanicLoginAct.this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(MechanicLoginAct.this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else {

            mAuth.createUserWithEmailAndPassword(email , pass).addOnCompleteListener(MechanicLoginAct.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(MechanicLoginAct.this, "sign in error ...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String user_id= mAuth.getCurrentUser().getUid();
                        currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(user_id);
                        currentUserDb.setValue(true);
                    }
                }
            });
        }
    }
}
