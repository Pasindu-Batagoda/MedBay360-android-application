package com.example.medbay_360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity
{
    //Initializing
    private EditText userEmail, userPassword, userConfirmPassword;
    private Button createAccountButton;
    //Loading bar
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        //Casting edit texts and button
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        userConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        createAccountButton = (Button) findViewById(R.id.register_create_account);
        loadingBar =  new ProgressDialog(this);

        //Click listener for create account button
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // calling create new account method
                CreateNewAccount();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            //Sending user to main activity if user already registered
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);

        //Validation to prevent user from going back
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    //create new account method
    private void CreateNewAccount()
    {
        //Creating variables
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmPassword.getText().toString();

        //validations
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please Enter Email", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please Provide a password", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(confirmPassword))
        {
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
        }

        // if password and confirm password doesn't match, notify user
        else if (!password.equals(confirmPassword))
        {
            Toast.makeText(this, "Your Password doesn't match", Toast.LENGTH_SHORT).show();
        }

        // create account
        else
        {
            //Loading bar
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                           if(task.isSuccessful())
                           {
                               Toast.makeText(RegisterActivity.this, "One more step to go!", Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();

                               //Send user to Setup activity
                               SendUserToSetupActivity();
                           }

                           //If task not successful

                           else
                           {
                               String message = task.getException().getMessage();
                               Toast.makeText(RegisterActivity.this, "Error : "+ message, Toast.LENGTH_SHORT).show();
                               loadingBar.dismiss();
                           }
                        }
                    });
        }
    }

    private void SendUserToSetupActivity()
    {
        //Method to send user to Setup activity
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);

        //Validation to prevent user from going back to register activity from setup activity
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
