package odev.com.geziodev.recyclerView;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import odev.com.geziodev.R;

public class TripsViewHolder extends RecyclerView.ViewHolder {

    public ImageView tripImage;
    public ImageView bookmarkedIcon;
    public TextView titleTextView;
    public TextView locationTextView;
    public TextView ratingTextView;
    public View mItemView;

    public TripsViewHolder(@NonNull View itemView) {
        super(itemView);

        tripImage = itemView.findViewById(R.id.recycler_imageView);
        bookmarkedIcon = itemView.findViewById(R.id.recycler_bookmarked_imageView);
        titleTextView = itemView.findViewById(R.id.recycler_title_textView);
        locationTextView = itemView.findViewById(R.id.recycler_location_textView);
        ratingTextView = itemView.findViewById(R.id.recycler_rating_textView);
        mItemView = itemView;
    }
}
