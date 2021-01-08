package com.example.medbay_360;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity
{
    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendRequestBtn, DeclineFriendRequestBtn;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId =mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("Visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        InitializeFields();

        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //Retrieving data from Users node in the database
                String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                String myUserName = dataSnapshot.child("username").getValue().toString();
                String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                String myDOB = dataSnapshot.child("dob").getValue().toString();
                String myCountry = dataSnapshot.child("country").getValue().toString();
                String myGender = dataSnapshot.child("gender").getValue().toString();
                String myRelationsStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                //setting profile image
                Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                //setting other details in settings page
                userName.setText("Username : " + myUserName);
                userProfName.setText("Full Name : " + myProfileName);
                userStatus.setText("Status : " + myProfileStatus);
                userCountry.setText("Country : " + myCountry);
                userGender.setText("Gender : " + myGender);
                userRelation.setText("Relationship Status : " + myRelationsStatus);
                userDOB.setText("Birthday : " + myDOB);

                MaintainanceOfButtons();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
        DeclineFriendRequestBtn.setEnabled(false);

        if(!senderUserId.equals(receiverUserId))
        {
            SendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SendFriendRequestBtn.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends"))
                    {
                        UnFriendAnExistingFriend();
                    }
                }
            });
        }
        else
        {
            DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
            SendFriendRequestBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void UnFriendAnExistingFriend()
    {
        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        FriendsRef.child(receiverUserId).child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            SendFriendRequestBtn.setEnabled(true);
                                            CURRENT_STATE = "not_friends";
                                            SendFriendRequestBtn.setText("Send Friend Request");

                                            DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                            DeclineFriendRequestBtn.setEnabled(false);
                                        }
                                    }
                                });
                    }
                });
    }

    private void AcceptFriendRequest()
    {
        Calendar calForDate = Calendar.getInstance();
        //Getting current date
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                if (task.isSuccessful())
                                                                                {
                                                                                    SendFriendRequestBtn.setEnabled(true);
                                                                                    CURRENT_STATE = "friends";
                                                                                    SendFriendRequestBtn.setText("Unfriend");

                                                                                    DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                                                    DeclineFriendRequestBtn.setEnabled(false);
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        FriendRequestRef.child(receiverUserId).child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            SendFriendRequestBtn.setEnabled(true);
                                            CURRENT_STATE = "not_friends";
                                            SendFriendRequestBtn.setText("Send Friend Request");

                                            DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                            DeclineFriendRequestBtn.setEnabled(false);
                                        }
                                    }
                                });
                    }
                });
    }

    private void MaintainanceOfButtons()
    {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserId))
                        {
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                SendFriendRequestBtn.setText("Cancel Friend Request");

                                DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestBtn.setEnabled(false);
                            }
                            else if (request_type.equals("received"))
                            {
                                CURRENT_STATE = "request_received";
                                SendFriendRequestBtn.setText("Accept Friend Request");
                                DeclineFriendRequestBtn.setVisibility(View.VISIBLE);
                                DeclineFriendRequestBtn.setEnabled(true);

                                DeclineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserId))
                                            {
                                                CURRENT_STATE = "freinds";
                                                SendFriendRequestBtn.setText("Unfreind");

                                                DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestBtn.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void SendFriendRequestToaPerson()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                         FriendRequestRef.child(receiverUserId).child(senderUserId)
                                 .child("request_type").setValue("received")
                                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                                     @Override
                                     public void onComplete(@NonNull Task<Void> task)
                                     {
                                         if (task.isSuccessful())
                                         {
                                             SendFriendRequestBtn.setEnabled(true);
                                             CURRENT_STATE = "request_sent";
                                             SendFriendRequestBtn.setText("Cancel Friend Request");

                                             DeclineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                             DeclineFriendRequestBtn.setEnabled(false);
                                         }
                                     }
                                 });
                    }
                });
    }


    private void InitializeFields()
    {
        userName = (TextView) findViewById(R.id.person_username);
        userProfName = (TextView) findViewById(R.id.person_full_name);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelation = (TextView) findViewById(R.id.person_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);

        SendFriendRequestBtn = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestBtn = (Button) findViewById(R.id.person_decline_friend_request_btn);

        CURRENT_STATE = "not_friends";
    }
}
