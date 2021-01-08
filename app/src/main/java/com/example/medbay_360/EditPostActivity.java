package com.example.medbay_360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class EditPostActivity extends AppCompatActivity
{
    private ImageView PostImage;
    private TextView PostDescription;
    private Button DeletePostButton, EditPostButton;
    private DatabaseReference EditPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, CurrentUserID, databaseUserID, description, image;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();

        //Retrieving post key from main activity
        PostKey = getIntent().getExtras().get("PostKey").toString();
        EditPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        PostImage = (ImageView) findViewById(R.id.edit_post_image);
        PostDescription = (TextView) findViewById(R.id.edit_post_description);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);

        //Initially edit and delete post buttons will be invisible
        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

        EditPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //Retrieving post image and description
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();

                    //getting user ID from database
                    databaseUserID =dataSnapshot.child("uid").getValue().toString();

                    PostDescription.setText(description);
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(PostImage);

                    //Display Edit and delete post buttons only for current logged in user
                    if(CurrentUserID.equals(databaseUserID))
                    {
                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }


                    //Editing Post
                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            EditCurrentPost(description);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                DeleteCurrentPost();
            }
        });
    }


    private void EditCurrentPost(String description)
    {
        //Alert dialog box to edit post description
        AlertDialog.Builder builder = new AlertDialog.Builder(EditPostActivity.this);
        builder.setTitle("Edit Post");

        //Text of input field set to post description
        final EditText inputField = new EditText(EditPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        //Creating two buttons as update and cancel for the dialog box
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                EditPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(EditPostActivity.this, "Post Updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_blue_bright);
    }

    //Deleting post and sending user to main activity
    private void DeleteCurrentPost()
    {
        EditPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post Deleted", Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(EditPostActivity.this, MainActivity.class);

        //Validation to prevent user from going back
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
