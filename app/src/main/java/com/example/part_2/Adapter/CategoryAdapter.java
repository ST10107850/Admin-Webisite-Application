package com.example.part_2.Adapter;

import static com.google.common.io.Files.getFileExtension;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.part_2.AddNewItemActivity;
import com.example.part_2.Interface.ImagePickerListener;
import com.example.part_2.Model.CategoriesModel;
import com.example.part_2.R;
import com.example.part_2.ShowActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    public static final int PICK_IMAGE_REQUEST = 2;
    Context context;
    List<CategoriesModel> list;
    private ImagePickerListener imagePickerListener;
    private Uri selectedUpdateImageUri;
    private static final int PICK_IMAGE_REQUEST_UPDATE = 2;


    public CategoryAdapter(Context context, List<CategoriesModel> list, ImagePickerListener listener) {
        this.context = context;
        this.list = list;
        this.imagePickerListener = listener;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return  new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_view_item, parent
                ,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.name.setText(list.get(position).getName());
        Glide.with(context).load(list.get(position).getImage_url()).into(holder.allCateImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShowActivity.class);
                intent.putExtra("type", list.get(position).getType());
                context.startActivity(intent);
            }
        });

        holder.menuDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.inflate(R.menu.popu_menu);

        // Set up the item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        showUpdateDialog(position);
                        return true;
                    case R.id.menu_delete:
                        showDeleteConfirmationDialog(position);
                        return true;
                    case R.id.menu_add:
                        // Handle edit item action
                        Intent intent = new Intent(context, AddNewItemActivity.class);
                        intent.putExtra("type", list.get(position).getType());
                        try {
                            context.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return true;
                    default:
                        return false;
                }
            }

        });

        // Show the popup menu
        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this item?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle delete item action
                deleteItem(position);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing or handle as needed
                dialog.dismiss();
            }
        });

        builder.show();
    }
    private void deleteItem(int position) {
        // Get the type of the item to be deleted
        String itemType = list.get(position).getType();

        // Get the reference to the Firestore collection
        CollectionReference itemsCollection = FirebaseFirestore.getInstance().collection("Categories");

        // Query the collection to find documents with the specified type
        itemsCollection
                .whereEqualTo("type", itemType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Loop through the documents and delete them
                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(i).getId();
                        itemsCollection.document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Item deleted successfully");
                                    // Optionally, you can notify your adapter that the data has changed
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> showToast("Failed to delete item"));
                    }
                })
                .addOnFailureListener(e -> showToast("Failed to query items"));
    }


    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(int position) {
        // Create a custom dialog layout
        View view = LayoutInflater.from(context).inflate(R.layout.update_category_popup, null);

        EditText categoryNameEditText = view.findViewById(R.id.editTextUpdateCategoryName);
        //EditText typeEditText = view.findViewById(R.id.editTextUpdateType);
        ImageView categoryImageView = view.findViewById(R.id.imageViewUpdateCategory);
        Button updateButton = view.findViewById(R.id.buttonUpdate);
        Button cancel = view.findViewById(R.id.buttonCancel);
        Button pickImage = view.findViewById(R.id.buttonPickImage);

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Notify the hosting Activity or Fragment to start the image picker activity
                if (imagePickerListener != null) {
                    imagePickerListener.startImagePickerActivity();
                }
            }
        });


        // Set the current values to the EditTexts
        CategoriesModel currentItem = list.get(position);
        categoryNameEditText.setText(currentItem.getName());
       // typeEditText.setText(currentItem.getType());
        Glide.with(context).load(currentItem.getImage_url()).into(categoryImageView);

        // Set up the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        Dialog dialog = builder.create();

        // Set up the update button click listener
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated values
                String updatedCategoryName = categoryNameEditText.getText().toString().trim();
                //String updatedType = typeEditText.getText().toString().trim();

                // Update data in Firestore
                updateCategoryInFirestore(position, updatedCategoryName);

                // Dismiss the dialog
                dialog.dismiss();
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

    private void updateCategoryInFirestore(int position, String updatedCategoryName) {
        // Get the document ID of the item to be updated
        String documentId = list.get(position).getType();

        // Get the reference to the Firestore collection
        CollectionReference itemsCollection = FirebaseFirestore.getInstance().collection("Categories");

        // Check if the image has changed
        if (selectedUpdateImageUri != null) {
            // Upload the updated image to Firebase Storage
            String imageName = System.currentTimeMillis() + "." + getFileExtension(String.valueOf(selectedUpdateImageUri));
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("category_images/" + imageName);
            storageReference.putFile(selectedUpdateImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get the download URL
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Update data in Firestore with the new image URL
                            itemsCollection.document(documentId)
                                    .update("name", updatedCategoryName, "image_url", uri.toString())
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Item updated successfully");
                                        // Optionally, you can notify your adapter that the data has changed
                                        notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> showToast("Failed to update item"));
                        });
                    })
                    .addOnFailureListener(e -> showToast("Error uploading updated image: " + e.getMessage()));
        } else {
            // No image change, update data without changing the image URL
            itemsCollection.document(documentId)
                    .update("name", updatedCategoryName)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showToast("Item updated successfully");
                            // Optionally, you can notify your adapter that the data has changed
                            notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast("Failed to update item");
                        }
                    });
        }
    }
    public void handleImagePickResult(Uri selectedUpdateImageUri) {
        // Handle the result in your adapter
        this.selectedUpdateImageUri = selectedUpdateImageUri;
        // Notify your adapter that the data has changed if needed
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView allCateImage, menuDots;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nav_cart_name);
            allCateImage = itemView.findViewById(R.id.cart_img);
            menuDots = itemView.findViewById(R.id.menuDots);
        }
    }

}