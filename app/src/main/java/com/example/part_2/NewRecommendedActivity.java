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

public class NewRecommendedActivity extends AppCompatActivity {

    EditText name, description, price, rating;
    ImageView imageView;
    Button Save;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recommended);

        name = findViewById(R.id.uploadName);
        description = findViewById(R.id.uploadDesc);
        price = findViewById(R.id.uploadtype);
        rating = findViewById(R.id.uploadrating);
        imageView = findViewById(R.id.uploadImage);
        Save = findViewById(R.id.saveButton);

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecommendedItem();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(v);
            }
        });
    }

    private void saveRecommendedItem() {
        String itemName = name.getText().toString();
        String itemDescription = description.getText().toString();
        String itemPrice = price.getText().toString();
        String itemRating = rating.getText().toString();

        if (itemName.isEmpty() || itemDescription.isEmpty() || itemPrice.isEmpty() || itemRating.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSaveData(itemName, itemDescription, itemPrice, itemRating);
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageAndSaveData(String name, String description, String price, String rating) {
        // Upload the image to Firebase Storage
        String imageName = System.currentTimeMillis() + "." + getFileExtension(selectedImageUri);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("recommended_images/" + imageName);
        storageReference.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, now get the download URL
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Save data to Firestore with the image URL
                        com.example.work_intergrated.Models.RecommendedModel recommendedItem = new com.example.work_intergrated.Models.RecommendedModel(name, description, rating, uri.toString(), Integer.parseInt(price));
                        saveItemToFirestore(recommendedItem);
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle errors during image upload
                    Toast.makeText(NewRecommendedActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveItemToFirestore(com.example.work_intergrated.Models.RecommendedModel recommendedItem) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Add a new document with a generated ID
        db.collection("RecommendedProduct")
                .add(recommendedItem)
                .addOnSuccessListener(documentReference -> {
                    // Document added successfully
                    // You can add any additional logic here
                    // For example, you might want to show a success message
                    Toast.makeText(NewRecommendedActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    // For example, you might want to show an error message
                    Toast.makeText(NewRecommendedActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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