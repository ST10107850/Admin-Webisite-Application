package com.example.part_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.part_2.Adapter.CategoryAdapter;
import com.example.part_2.Adapter.RecommendedAdapter;
import com.example.part_2.Interface.ImagePickerListener;
import com.example.part_2.Model.CategoriesModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecommendedActivity extends AppCompatActivity {

    List<com.example.work_intergrated.Models.RecommendedModel> recommendedModelList;
    RecommendedAdapter recommendedAdapter;

    RecyclerView recomm_recycleview;
    Button addRecommended;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommended);

        addRecommended = findViewById(R.id.addRecommended);

        recomm_recycleview = findViewById(R.id.recomm_recycleview);

        db = FirebaseFirestore.getInstance();



        addRecommended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecommendedActivity.this, NewRecommendedActivity.class));
            }
        });

        recomm_recycleview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recommendedModelList = new ArrayList<>();
        recommendedAdapter =new RecommendedAdapter(this, recommendedModelList);
        recomm_recycleview.setAdapter(recommendedAdapter);

        db.collection("RecommendedProduct")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                com.example.work_intergrated.Models.RecommendedModel recommendedModel = document.toObject(com.example.work_intergrated.Models.RecommendedModel.class);
                                recommendedModelList.add(recommendedModel);
                                recommendedAdapter.notifyDataSetChanged();

                            }
                        } else {
                            Toast.makeText(RecommendedActivity.this,"Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });



    }

}