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
import com.example.part_2.Model.PopularModels;
import com.example.part_2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder> {

    Context context;
    List<PopularModels> popular_list;

    public PopularAdapter(Context context, List<PopularModels> popular_list) {
        this.context = context;
        this.popular_list = popular_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.popular_item, parent
                ,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(popular_list.get(position).getImage_url()).into(holder.popImage);
        holder.name.setText(popular_list.get(position).getName());
        holder.description.setText(popular_list.get(position).getDescription());
        holder.discount.setText(popular_list.get(position).getDiscount());
        holder.rating.setText(popular_list.get(position).getRating());

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
        String itemId = popular_list.get(position).getName(); // Assuming there's a getId method in your model

        // Remove the item from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("PopularProducts") // Replace with your Firestore collection name
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // DocumentSnapshot successfully deleted
                    // Perform any additional tasks if needed
                    popular_list.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, popular_list.size());
                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(context, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public int getItemCount() {
        return popular_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView popImage;
        TextView name, description, rating, discount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            popImage = itemView.findViewById(R.id.pop_img);
            name =itemView.findViewById(R.id.item_name);
            description = itemView.findViewById(R.id.pop_desc);
            rating = itemView.findViewById(R.id.pop_rating);
            discount = itemView.findViewById(R.id.pop_discount);
        }
    }
}
