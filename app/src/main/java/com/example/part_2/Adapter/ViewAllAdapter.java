package com.example.part_2.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.part_2.Model.ViewAllModel;
import com.example.part_2.R;
import com.example.part_2.ShowActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ViewAllAdapter extends RecyclerView.Adapter<ViewAllAdapter.ViewHolder>{
    Context context;
    List<ViewAllModel> viewAllModelList;

    public ViewAllAdapter(Context context, List<ViewAllModel> viewAllModelList) {
        this.context = context;
        this.viewAllModelList = viewAllModelList;
    }
    @NonNull
    @Override
    public ViewAllAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewAllAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_all_product, parent
                ,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewAllAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(viewAllModelList.get(position).getImage_url()).into(holder.viewImage);
        holder.name.setText(viewAllModelList.get(position).getName());
        holder.description.setText(viewAllModelList.get(position).getDescription());
        holder.price.setText(String.valueOf(viewAllModelList.get(position).getPrice()));
        holder.rating.setText(viewAllModelList.get(position).getRating());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfirmationDialog(position);
            }
        });


    }
    private void showConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Confirmation");
        builder.setMessage("Do you want to delete this item?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User clicked Yes, perform the deletion
            deleteItem(position);
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }
    private void deleteItem(int position) {
        // Get the document ID (or any unique identifier) of the item
        String itemId = viewAllModelList.get(position).getName(); // Assuming there's a getId method in your model

        // Remove the item from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Products") // Replace with your Firestore collection name
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // DocumentSnapshot successfully deleted
                    // Perform any additional tasks if needed
                    viewAllModelList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, viewAllModelList.size());
                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(context, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return viewAllModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView viewImage;
        TextView name, description, rating, price;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewImage = itemView.findViewById(R.id.view_img);
            name =itemView.findViewById(R.id.view_name);
            description = itemView.findViewById(R.id.view_description);
            rating = itemView.findViewById(R.id.view_rating);
            price = itemView.findViewById(R.id.view_price);

        }

    }
}