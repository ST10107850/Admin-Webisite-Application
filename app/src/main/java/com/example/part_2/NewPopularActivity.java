package com.example.part_2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.part_2.Model.PopularModels;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class NewPopularActivity extends AppCompatActivity {

    EditText popName, popDescription, popDiscount, catType, popRating;
    Button savePop, addNewPopular;

    ImageView imagView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_popular);

        popName = findViewById(R.id.uploadName);
        popDescription = findViewById(R.id.uploadDesc);
        popDiscount = findViewById(R.id.uploadDiscount);
        catType = findViewById(R.id.uploadtype);
        popRating = findViewById(R.id.uploadrating);
        savePop = findViewById(R.id.saveButton);

        imagView = findViewById(R.id.uploadImage);
        imagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(v);
            }
        });
        savePop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePopularItem();
            }
        });


    }

    private void savePopularItem() {
        String name = popName.getText().toString();
        String description = popDescription.getText().toString();
        String discount = popDiscount.getText().toString();
        String type = catType.getText().toString();
        String rating = popRating.getText().toString();

        if (name.isEmpty() || description.isEmpty() || discount.isEmpty() || type.isEmpty() || rating.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSaveData(name, description, discount, type, rating);
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveData(String name, String description, String discount, String type, String rating) {
        // Upload the image to Firebase Storage
        String imageName = System.currentTimeMillis() + "." + getFileExtension(selectedImageUri);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("popular_images/" + imageName);
        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, now get the download URL
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save data to Firestore with the image URL
                        PopularModels popularItem = new PopularModels(name, description, rating, discount, type, uri.toString());
                        saveItemToFirestore(popularItem);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle errors during image upload
                    Toast.makeText(NewPopularActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveItemToFirestore(PopularModels popularItem) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a new document with a generated ID
        db.collection("PopularProducts")
                .add(popularItem)
                .addOnSuccessListener(documentReference -> {
                    // Document added successfully
                    // You can add any additional logic here
                    // For example, you might want to show a success message
                    Toast.makeText(NewPopularActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    // For example, you might want to show an error message
                    Toast.makeText(NewPopularActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    public void chooseImage(View view) {
        // Open the image picker
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image URI
            selectedImageUri = data.getData();
        }
    }
}