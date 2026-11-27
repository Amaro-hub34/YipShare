package com.startupidea.yipshare.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.feedback.Review;

public class ReviewAdapter extends ListAdapter<Review, ReviewAdapter.ReviewViewHolder> {

    public ReviewAdapter() {
        super(new RecordDiff<>(review -> review));
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = getItem(position);
        holder.authorLabel.setText(review.authorUsername());
        holder.ratingBar.setRating(review.getRating());
        holder.textLabel.setText(review.text());
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView authorLabel, textLabel;
        RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            authorLabel = itemView.findViewById(R.id.tv_autor_resena);
            ratingBar = itemView.findViewById(R.id.rb_resena);
            textLabel = itemView.findViewById(R.id.tv_texto_resena);
        }
    }
}
