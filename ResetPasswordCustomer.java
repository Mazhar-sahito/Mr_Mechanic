package com.example.mazharali.projectfyp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordCustomer extends AppCompatActivity {

    EditText email_reset;
    Button reset_button;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        email_reset = (EditText)findViewById(R.id.reset_email);
        reset_button = (Button) findViewById(R.id.reset_button);

        firebaseAuth = FirebaseAuth.getInstance();

        reset_button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                String email= email_reset.getText().toString();
                if(email.equals("")){
                    Toast.makeText(ResetPasswordCustomer.this , "Please provide email" , Toast.LENGTH_SHORT).show();
                }else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordCustomer.this , "check your mail" , Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordCustomer.this , MainActivity.class));
                                finish();
                                return;
                            }else {
                                String error = task.getException().getMessage();
                                Toast.makeText(ResetPasswordCustomer.this , "Error : "+error , Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
