package com.example.part_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.part_2.Model.ViewAllModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddNewItemActivity extends AppCompatActivity {

    EditText name, price, rating, description;
    ImageView image;

    private static final int PICK_IMAGE_REQUEST = 1;

    Button saveBtn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference itemsCollection = db.collection("Products");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);

        name=findViewById(R.id.uploadName);
        price=findViewById(R.id.uploadprice);
        rating=findViewById(R.id.uploadrating);
        description=findViewById(R.id.uploadDesc);
        image=findViewById(R.id.uploadImage);
        saveBtn=findViewById(R.id.saveButton);

        // Retrieve the "type" from the Intent
        String itemType = getIntent().getStringExtra("type");


        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItemToFirestore(itemType);
            }
        });

    }
    private void saveItemToFirestore(String itemType) {
        // Retrieve the entered values
        String itemName = name.getText().toString().trim();
        String itemPrice = price.getText().toString().trim();
        String itemRating = rating.getText().toString().trim();
        String itemDescription = description.getText().toString().trim();

        // Check if any of the fields are empty
        if (itemName.isEmpty() || itemPrice.isEmpty() || itemRating.isEmpty() || itemDescription.isEmpty()) {
            Toast.makeText(AddNewItemActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new instance of ViewAllModel
        ViewAllModel newItem = new ViewAllModel(itemName, itemDescription, itemRating, itemType, "", Integer.parseInt(itemPrice));

        // Get the reference to the Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        // Create a reference to the location where you want to save the image (e.g., "images" folder)
        StorageReference imageRef = storageRef.child( itemName + ".jpg");

        // Get the image Bitmap from ImageView
        image.setDrawingCacheEnabled(true);
        image.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

        // Convert Bitmap to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the image to Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Image upload successful, get the download URL
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri downloadUri) {
                        // Update the newItem with the image URL
                        newItem.setImage_url(downloadUri.toString());

                        // Store the item in Firestore
                        itemsCollection.add(newItem)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(AddNewItemActivity.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(AddNewItemActivity.this, ShowActivity.class);
                                        intent.putExtra("type", newItem.getType());
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddNewItemActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddNewItemActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            if (image != null) {
                // Do something with the selected image URI (e.g., load it into an ImageView)
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    image.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "ImageView is null", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
