package com.example.part_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toolbar;

import com.example.part_2.Adapter.ViewAllAdapter;
import com.example.part_2.Model.ViewAllModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends AppCompatActivity {
    FirebaseFirestore db;
    RecyclerView viewRec;
    //Popular items
    List<ViewAllModel> viewAllModelList;
    ViewAllAdapter viewAllAdapter;
    ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);



        String type = getIntent().getStringExtra("type");
        db = FirebaseFirestore.getInstance();
        viewRec = findViewById(R.id.view_all_rec);


        // Set up Toolbar


        viewAllModelList = new ArrayList<>();
        viewAllAdapter = new ViewAllAdapter(this, viewAllModelList);
        viewRec.setAdapter(viewAllAdapter);
        viewRec.setLayoutManager(new LinearLayoutManager(this));

        if(type != null && type.equalsIgnoreCase(type)){
            db.collection("Products").whereEqualTo("type", type).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                            ViewAllModel viewAllModel = documentSnapshot.toObject(ViewAllModel.class);
                            if (viewAllModel != null) {
                                viewAllModelList.add(viewAllModel);
                            } else {
                                Log.e("ViewAllActivity", "viewAllModel is null for document ID: " + documentSnapshot.getId());
                            }
                        }
                        viewAllAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("ViewAllActivity", "Firestore query failed: " + task.getException());
                    }
                }

            });
        }
    }
}