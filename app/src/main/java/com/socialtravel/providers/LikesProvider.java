package com.socialtravel.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.socialtravel.models.Like;

public class LikesProvider {

    CollectionReference mColletion;

    public LikesProvider() {
        mColletion = FirebaseFirestore.getInstance().collection("Likes");
    }

    public Task<Void> create (Like like) {
        DocumentReference document = mColletion.document();
        String id = document.getId();
        like.setId(id);
        return document.set(like);
    }

    public Query getLikesByPost(String idPost) {
        return mColletion.whereEqualTo("idPost", idPost);
    }


    public Query getLikeByPostAndUser(String idPost, String idUser) {
        return mColletion.whereEqualTo("idPost", idPost).whereEqualTo("idUser", idUser);
    }

    public Task<Void> delete(String id) {
        return mColletion.document(id).delete();
    }
}
