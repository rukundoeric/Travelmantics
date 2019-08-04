package com.example.travelmantics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.Serializable;
import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity activity;
    private ArrayList<TravelDeal> deals;
    public DealAdapter(Activity activity) {
        this.activity = activity;
        FirebaseUtil.openFbReference("traveldeals", activity);
        deals = FirebaseUtil.mDeals;
        FirebaseUtil.mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                deals.add(td);
                notifyItemInserted(deals.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row, parent, false);
        return new DealViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DealViewHolder view = ((DealViewHolder) holder);
        view.tvTitle.setText(deals.get(position).getTitle());
        view.tvPrice.setText(deals.get(position).getPrice());
        view.tvDesc.setText(deals.get(position).getDescription());
        if (deals.get(position).getImageUrl().length() > 0 ){
            Glide.with(activity)
                    .load(deals.get(position).getImageUrl())
                    .apply(new RequestOptions()
                            .optionalFitCenter()
                            .placeholder(R.drawable.avata_default)
                            .centerCrop())
                    .into(view.tvImage);
        }
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        RoundedImageView tvImage;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvDesc;
        Context context;
        public DealViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPrice);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDescription);
            tvImage = (RoundedImageView) itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
          TravelDeal selectedDeal = deals.get(getAdapterPosition());
          Intent intent = new Intent(view.getContext(), DealActivity.class);
          intent.putExtra("Deal", selectedDeal);
          context.startActivity(new Intent(intent ));
            Toast.makeText(context, "Position"+getAdapterPosition(), Toast.LENGTH_SHORT).show();
        }
    }
}
