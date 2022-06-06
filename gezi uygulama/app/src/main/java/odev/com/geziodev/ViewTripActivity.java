package odev.com.geziodev;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;

import odev.com.geziodev.models.Trip;

public class ViewTripActivity extends AppCompatActivity  implements EventListener<DocumentSnapshot>  {

    private static final String TAG = "TripDetail";

    private ImageView tripImage;
    private TextView destination;
    private TextView tripName;
    private TextView tripType;
    private RatingBar ratingBar;
    private TextView startDate;
    private TextView endDate;
    private TextView price;


    private FirebaseFirestore mFirestore;
    private DocumentReference mTripRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trip);

        initFirestore();

        initViews();
    }

    void initFirestore() {
        String tripId= getIntent().getExtras().getString(NavigationDrawerActivity.TRIP_ID);
        String dbId = getIntent().getExtras().getString(NavigationDrawerActivity.DB_ID);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the trip
        mTripRef = mFirestore.collection(dbId).document(tripId);
    }

    void initViews() {
        tripImage = findViewById(R.id.trip_image_viewTrip);
        destination = findViewById(R.id.trip_location_viewTrip);
        tripName = findViewById(R.id.trip_name_viewTrip);
        tripType = findViewById(R.id.trip_type_viewTrip);
        ratingBar = findViewById(R.id.trip_rating_viewTrip);
        startDate = findViewById(R.id.trip_start_date_viewTrip);
        endDate = findViewById(R.id.trip_end_date_viewTrip);
        price = findViewById(R.id.trip_price_viewTrip);
    }


    @Override
    public void onStart() {
        super.onStart();
        mTripRef.addSnapshotListener(this);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "trip:onEvent", e);
            return;
        }

        onTripLoaded(snapshot.toObject(Trip.class));
    }

    private void onTripLoaded(Trip trip) {
        File imageFile = new File(trip.getImagePath());
        if(imageFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(trip.getImagePath());
            tripImage.setImageBitmap(myBitmap);
            destination.setText(trip.getDestination());
            tripName.setText(trip.getTripName());
//            tripType.setText(trip.getTripType());
            ratingBar.setRating((float)trip.getRating());
            String[] startDateParts = trip.getStartDate().toString().split(" ");
            String[] endDateParts = trip.getEndDate().toString().split(" ");
            startDate.setText(startDateParts[2] + " " + startDateParts[1] + " " + startDateParts[5]);
            endDate.setText(endDateParts[2] + " " + endDateParts[1] + " " + endDateParts[5]);
            price.setText(trip.getPrice() + " TL");
        }
    }

}
