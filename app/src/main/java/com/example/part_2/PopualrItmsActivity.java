package com.example.part_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.part_2.Adapter.PopularAdapter;
import com.example.part_2.Model.PopularModels;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PopualrItmsActivity extends AppCompatActivity {

    Button AddNewPopular;
    List<PopularModels> popularModelsList;
    PopularAdapter popularAdapter;
    RecyclerView popRec;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popualr_itms);
        AddNewPopular = findViewById(R.id.addPop);

        AddNewPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PopualrItmsActivity.this, NewPopularActivity.class));
            }
        });

        popRec = findViewById(R.id.cat_rec);
        //Popular Items
        popRec.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        popularModelsList = new ArrayList<>();
        popularAdapter =new PopularAdapter(this, popularModelsList);
        popRec.setAdapter(popularAdapter);
        db = FirebaseFirestore.getInstance();
        db.collection("PopularProducts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PopularModels popularModels = document.toObject(PopularModels.class);
                                popularModelsList.add(popularModels);
                                popularAdapter.notifyDataSetChanged();

                            }
                        } else {
                            Toast.makeText(PopualrItmsActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}