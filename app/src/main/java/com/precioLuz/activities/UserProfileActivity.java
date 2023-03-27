package com.precioLuz.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.precioLuz.R;
import com.precioLuz.adapters.MyPostAdapter;
import com.precioLuz.models.Post;
import com.precioLuz.providers.AuthProvider;
import com.precioLuz.providers.PostProvider;
import com.precioLuz.providers.UserProvider;
import com.precioLuz.utils.ViewedMessageHelper;
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
    RecyclerView mRecyclerView;
    Toolbar mToolbar;
    FloatingActionButton mFavChat;

    UserProvider mUserProvider;
    AuthProvider mAuthProvider;
    PostProvider mPostProvider;

    MyPostAdapter mAdapter;

    ListenerRegistration mListener;



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
        mRecyclerView = findViewById(R.id.recyclerViewMyPost);
        mToolbar = findViewById(R.id.toolbar);
        mFavChat = findViewById(R.id.fabChat);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(UserProfileActivity.this);//para que me ponga las tarjetas una debajo de otra.
        mRecyclerView.setLayoutManager(linearLayoutManager);


        mUserProvider = new UserProvider();
        mAuthProvider = new AuthProvider();
        mPostProvider = new PostProvider();

        MyPostAdapter mAdapter;

        mExtraIdUser = getIntent().getStringExtra("idUser");

        //Ocultamos el botón de chat si está en su mismo perfil (para no hablar consigo mismo).
        if(mAuthProvider.getUid().equals(mExtraIdUser)) {
            mFavChat.setVisibility(View.GONE); //setEnable también serviría.
        }


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
        ViewedMessageHelper.updateOnline(true, UserProfileActivity.this);//Controlador de si el usuario está online o no.

    }

    @Override
    protected void onPause() {
        super.onPause();
        ViewedMessageHelper.updateOnline(false, UserProfileActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mListener!= null) {
            mListener.remove();
        }
    }

    private void checkIfExistPost() {
        mListener = mPostProvider.getPostByUser(mExtraIdUser).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if(queryDocumentSnapshots!= null) {
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
                    if(documentSnapshot.contains("imageProfile")) {
                        String imageProfile = documentSnapshot.getString("imageProfile");
                        if(imageProfile != null && !imageProfile.isEmpty()){
                            Picasso.with(UserProfileActivity.this).load(imageProfile).into(mCircleImageProfile);
                        }
                    }
                    if(documentSnapshot.contains("imageCover")) {
                        String imageCover = documentSnapshot.getString("imageCover");
                        if(imageCover != null && !imageCover.isEmpty()){
                            Picasso.with(UserProfileActivity.this).load(imageCover).into(mImageViewCover);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //Fix para decir donde debe volver el botón atrás (para sobrescribir al Android Manifest).
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}