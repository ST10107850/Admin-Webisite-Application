package com.example.part_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.part_2.Adapter.CategoryAdapter;
import com.example.part_2.Interface.ImagePickerListener;
import com.example.part_2.Model.CategoriesModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements ImagePickerListener {

    FirebaseFirestore db;
    RecyclerView recyclerView;
    List<CategoriesModel> navCategoryModelList;
    CategoryAdapter navCategoryAdapter;
    Button addCategory, AddRecommended,AddPopular;
    private ImageView categoryImageView;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        recyclerView = findViewById(R.id.cat_rec);
       // recyclerView.setVisibility(View.GONE);
        db = FirebaseFirestore.getInstance();
        //Popular Items
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        navCategoryModelList = new ArrayList<>();
        navCategoryAdapter =new CategoryAdapter((Context) this, navCategoryModelList, (ImagePickerListener) this);
        recyclerView.setAdapter(navCategoryAdapter);



        AddPopular =findViewById(R.id.AddPopular);

        AddRecommended = findViewById(R.id.AddRecommended);
        AddRecommended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RecommendedActivity.class));
            }
        });

        AddPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PopualrItmsActivity.class));
            }
        });
        db.collection("Categories")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CategoriesModel navCategoryModel = document.toObject(CategoriesModel.class);
                                navCategoryModelList.add(navCategoryModel);
                                navCategoryAdapter.notifyDataSetChanged();


                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
         });

        addCategory = findViewById(R.id.addCategory);
        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCategoryDialog();
            }
        });
    }
    @Override
    public void startImagePickerActivity() {
        // Open the image picker
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, CategoryAdapter.PICK_IMAGE_REQUEST);
    }
    private void showAddCategoryDialog() {
        // Create a custom dialog layout
        View view = getLayoutInflater().inflate(R.layout.add_category_popup, null);

        EditText categoryNameEditText = view.findViewById(R.id.editTextCategoryName);
        EditText typeEditText = view.findViewById(R.id.editTextType);
        categoryImageView = view.findViewById(R.id.imageViewCategory);
        Button saveButton = view.findViewById(R.id.buttonSave);
        Button pickImage = view.findViewById(R.id.buttonPickImage);
        Button cancel = view.findViewById(R.id.buttonCancell);

        // Set up the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        Dialog dialog = builder.create();

        // Set up the save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String categoryName = categoryNameEditText.getText().toString().trim();
                String type = typeEditText.getText().toString().trim();
                if (!categoryName.isEmpty() && !type.isEmpty()) {
                    // Save data to Firestore
                    saveCategoryToFirestore(categoryName, type, selectedImageUri);

                    // Dismiss the dialog
                    dialog.dismiss();
                } else {
                    // Show a message if inputs are empty
                    // TODO: You may want to handle this differently
                    categoryNameEditText.setError("Required");
                    typeEditText.setError("Required");
                }
            }
        });

        // Set up the pick image button click listener
        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the image picker
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        // Set up the cancel button click listener
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog on cancel
                dialog.dismiss();
            }
        });

        // Show the dialog
        dialog.show();
    }


    private void saveCategoryToFirestore(String categoryName, String type, Uri imageUri) {
        // Check if an image is selected
        if (imageUri != null) {
            // Upload the image to Firebase Storage
            String imageName = System.currentTimeMillis() + "." + getFileExtension(imageUri);
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("category_images/" + imageName);
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get the download URL
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Save data to Firestore with the image URL
                            CategoriesModel category = new CategoriesModel(categoryName, type, uri.toString());
                            db.collection("Categories").add(category);
                            Toast.makeText(MainActivity.this, "Category added successfully", Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors during image upload
                        Toast.makeText(MainActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No image selected, save data without the image URL
            CategoriesModel category = new CategoriesModel(categoryName, type, "");
            db.collection("Categories").add(category);
            Toast.makeText(MainActivity.this, "Category added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to get the file extension of the selected image
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI
            selectedImageUri = data.getData();

            // Set the image to the ImageView
            categoryImageView.setImageURI(selectedImageUri);
        }
    }

}