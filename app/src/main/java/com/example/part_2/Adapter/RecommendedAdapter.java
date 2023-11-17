package com.example.part_2.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.example.part_2.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecommendedAdapter extends RecyclerView.Adapter<RecommendedAdapter.ViewHolder>{
    Context context;
    List<com.example.work_intergrated.Models.RecommendedModel> recommendedModelList;

    public RecommendedAdapter(Context context, List<com.example.work_intergrated.Models.RecommendedModel> recommendedModelList) {
        this.context = context;
        this.recommendedModelList = recommendedModelList;
    }

    @NonNull
    @Override
    public RecommendedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_view, parent
                ,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendedAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Glide.with(context).load(recommendedModelList.get(position).getImage_url()).into(holder.rec_image);
        holder.rec_name.setText(recommendedModelList.get(position).getName());
        holder.rec_description.setText(recommendedModelList.get(position).getDescription());
        holder.rec_rating.setText(recommendedModelList.get(position).getRating());

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
        String itemId = recommendedModelList.get(position).getName(); // Assuming there's a getId method in your model

        // Remove the item from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("RecommendedProduct") // Replace with your Firestore collection name
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // DocumentSnapshot successfully deleted
                    // Perform any additional tasks if needed
                    recommendedModelList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, recommendedModelList.size());
                    Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(context, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    @Override
    public int getItemCount() {
        return recommendedModelList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView rec_image;
        TextView rec_name, rec_description, rec_rating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rec_image =itemView.findViewById(R.id.rec_image);
            rec_name = itemView.findViewById(R.id.rec_name);
            rec_description = itemView.findViewById(R.id.rec_description);
            rec_rating = itemView.findViewById(R.id.rec_rating);
        }
    }
}
