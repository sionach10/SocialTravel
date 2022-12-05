package com.socialtravel.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.socialtravel.R;
import com.socialtravel.adapters.MyPostAdapter;
import com.socialtravel.models.Post;
import com.socialtravel.providers.AuthProvider;
import com.socialtravel.providers.PostProvider;
import com.socialtravel.providers.UserProvider;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    String mExtraIdUser;
    LinearLayout mLinearLayoutEditProfile;
    TextView mTextViewUsername;
    TextView mTextViewPhone;
    TextView mTextViewEmail;
    TextView mTextViewPostNumber;
    TextView mTextViewPostExist;
    ImageView mImageViewCover;
    CircleImageView mCircleImageProfile;
    CircleImageView mCircleImageViewBack;
    RecyclerView mRecyclerView;

    UserProvider mUserProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;

    MyPostAdapter mAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mLinearLayoutEditProfile = findViewById(R.id.linearLayoutEditProfile);
        mTextViewEmail = findViewById(R.id.textViewEmail);
        mTextViewUsername = findViewById(R.id.textViewUsername);
        mTextViewPhone = findViewById(R.id.textViewPhone);
        mTextViewPostNumber = findViewById(R.id.textViewPostNumber);
        mTextViewPostExist = findViewById(R.id.textViewPostExist);
        mCircleImageProfile = findViewById(R.id.circleImageProfile);
        mImageViewCover = findViewById(R.id.imageViewCover);
        mCircleImageViewBack = findViewById(R.id.circleImageBack);
        mRecyclerView = findViewById(R.id.recyclerViewMyPost);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserProfileActivity.this);//para que me ponga las tarjetas una debajo de otra.
        mRecyclerView.setLayoutManager(linearLayoutManager);


        mUserProvider = new UserProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();

        MyPostAdapter mAdapter;

        mExtraIdUser = getIntent().getStringExtra("idUser");


        mCircleImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        getUser();
        getPostNumber();
        checkIfExistPost();

    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = mPostProvider.getPostByUser(mExtraIdUser);//Devuelve todos los Post ordenador por timestamp.
        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class).build();

        mAdapter = new MyPostAdapter(options, UserProfileActivity.this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private void checkIfExistPost() {
        mPostProvider.getPostByUser(mExtraIdUser).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                int numberPost = queryDocumentSnapshots.size();
                if(numberPost >0) {
                    mTextViewPostExist.setText("Publicaciones:");
                    mTextViewPostExist.setTextColor(Color.BLACK);
                }
                else {
                    mTextViewPostExist.setText("No hay publicaciones.");
                    mTextViewPostExist.setTextColor(Color.GRAY);
                }
            }
        });
    }

    private void getPostNumber() {
        mPostProvider.getPostByUser(mExtraIdUser).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberPost = queryDocumentSnapshots.size();
                mTextViewPostNumber.setText(String.valueOf(numberPost));
            }
        });
    }

    private void getUser() {
        mUserProvider.getUser(mExtraIdUser).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    if(documentSnapshot.contains("email")) {
                        String email = documentSnapshot.getString("email");
                        mTextViewEmail.setText(email);
                    }
                    if(documentSnapshot.contains("username")) {
                        String username = documentSnapshot.getString("username");
                        mTextViewUsername.setText(username);
                    }
                    if(documentSnapshot.contains("phone")) {
                        String phone = documentSnapshot.getString("phone");
                        mTextViewPhone.setText(phone);
                    }
                    if(documentSnapshot.contains("image_profile")) {
                        String imageProfile = documentSnapshot.getString("image_profile");
                        if(imageProfile != null && !imageProfile.isEmpty()){
                            Picasso.with(UserProfileActivity.this).load(imageProfile).into(mCircleImageProfile);
                        }
                    }
                    if(documentSnapshot.contains("image_cover")) {
                        String imageCover = documentSnapshot.getString("image_cover");
                        if(imageCover != null && !imageCover.isEmpty()){
                            Picasso.with(UserProfileActivity.this).load(imageCover).into(mImageViewCover);
                        }
                    }
                }
            }
        });
    }
}