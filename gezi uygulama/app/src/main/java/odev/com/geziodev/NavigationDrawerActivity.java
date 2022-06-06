package odev.com.geziodev;

import android.Manifest;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import odev.com.geziodev.recyclerView.TripsAdapter;
import odev.com.geziodev.models.Trip;
import odev.com.geziodev.room.TravelJournalDatabase;


public class NavigationDrawerActivity extends AppCompatActivity implements TripsAdapter.OnTripSelectedListener,NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int PERMISSION_REQUEST_CODE = 201;
    private static final int ADD_NEW_TRIP = 301;
    private static final int LIMIT = 50;
    public static final String ANONYMOUS = "anonymou";
    private static final String TAG = "MainActivity";
    public static final String TRIP_ID = "tripID";
    public static final String DB_ID = "DB_ID";

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirestore;

    //Auth
    private String mUsername;
    private String mEmail;
    private String mPhotoUrl;
    private GoogleApiClient mGoogleApiClient;

    //Room
    public static TravelJournalDatabase tjdb;

    private RecyclerView tripsRecyclerView;
    CollectionReference trips;
    private Query mQuery;
    private TripsAdapter tripsAdapter;

    private ImageView profileImage;
    private TextView profileName;
    private TextView profilEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        reqStoragePerm();

        initFirebase();

        initDrawer();

        initRecyclerView();

        initRoom();
    }

    //Inits
    private void initFirebase() {
        mUsername = ANONYMOUS;
        //Firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Req auth
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, OturumEkran.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            mEmail = mFirebaseUser.getEmail();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mFirestore = FirebaseFirestore.getInstance();
        // Get the 50 highest rated restaurants
        mQuery = mFirestore.collection(mEmail)
                .orderBy("startDate", Query.Direction.ASCENDING)
                .limit(LIMIT);

        trips = mFirestore.collection(mEmail);
    }

    private void initDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        profileImage = headerView.findViewById(R.id.drawer_profile_imageView);
        profileName = headerView.findViewById(R.id.drawer_name_textView);
        profilEmail = headerView.findViewById(R.id.drawer_email_textView);
        Picasso.get().load(mPhotoUrl).fit().into(profileImage);
        profileName.setText(mUsername);
        profilEmail.setText(mEmail);
    }

    private void initRecyclerView() {
        tripsRecyclerView = findViewById(R.id.trips_recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        tripsRecyclerView.setLayoutManager(layoutManager);
        tripsAdapter = new TripsAdapter(mQuery, this);
        tripsRecyclerView.setAdapter(tripsAdapter);
        tripsAdapter.startListening();
    }

    private void initRoom() {
        tjdb = Room.databaseBuilder(getApplicationContext(), TravelJournalDatabase.class, "tripsdb").build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Select from drawer
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (id == R.id.nav_home) {
            mQuery = mFirestore.collection(mEmail)
                    .orderBy("startDate", Query.Direction.ASCENDING)
                    .limit(LIMIT);
            tripsAdapter = new TripsAdapter(mQuery, this);
            tripsRecyclerView.swapAdapter(tripsAdapter, false);
            tripsAdapter.startListening();

        } else if (id == R.id.nav_favourite) {
            mQuery = mFirestore.collection(mEmail).whereEqualTo("isFavourite", true)
                    .orderBy("startDate", Query.Direction.ASCENDING)
                    .limit(LIMIT);
            tripsAdapter = new TripsAdapter(mQuery, this);
            tripsRecyclerView.swapAdapter(tripsAdapter, false);
            tripsAdapter.startListening();

        } else if (id == R.id.nav_info) {

        } else if (id == R.id.nav_email) {

        } else if (id == R.id.nav_signout) {

            mFirebaseAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            mUsername = ANONYMOUS;
            startActivity(new Intent(this, OturumEkran.class));

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Add trip
    public void addTripOnClick(View view) {
        Intent intent = new Intent(NavigationDrawerActivity.this, AddOrEditTripActivity.class);
        intent.putExtra(TRIP_ID, (String)null);
        intent.putExtra(DB_ID, (String)null);
        startActivityForResult(intent, ADD_NEW_TRIP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ADD_NEW_TRIP) {
            if(resultCode == Activity.RESULT_OK) {
                String mTripName = data.getStringExtra(AddOrEditTripActivity.TRIPNAME);
                String mDestination = data.getStringExtra(AddOrEditTripActivity.DESTINATION);
                String mTripType = data.getStringExtra(AddOrEditTripActivity.TRIPTYPE);
                String mStartDate = data.getStringExtra(AddOrEditTripActivity.STARTDATE);
                String mEndDate = data.getStringExtra(AddOrEditTripActivity.ENDDATE);
                String mPrice = data.getStringExtra(AddOrEditTripActivity.PRICE);
                String mRating = data.getStringExtra(AddOrEditTripActivity.RATING);
                String mImagePath = data.getStringExtra(AddOrEditTripActivity.PHOTOPATH);

                int mPriceToInt = Integer.parseInt(mPrice);
                SimpleDateFormat format = new SimpleDateFormat("d/M/y");
                Date mStartDateToDate = null;
                Date mEndDateToDate = null;
                try {
                    mStartDateToDate = format.parse(mStartDate);
                    mEndDateToDate = format.parse(mEndDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                double mRatingToDouble = Double.parseDouble(mRating);

                final Trip t = new Trip(mTripName, mDestination, mTripType, mPriceToInt, mStartDateToDate, mEndDateToDate, mRatingToDouble, mImagePath, false);
                trips.add(t);
                Toast.makeText(this, "Trip added successfully!", Toast.LENGTH_SHORT).show();
            }
        }
    }



    //Recycler item clickables
    @Override
    public void onTripSelected(DocumentSnapshot trip) {
        Intent intent = new Intent(NavigationDrawerActivity.this, ViewTripActivity.class);
        intent.putExtra(TRIP_ID, trip.getId());
        intent.putExtra(DB_ID, mEmail);
        startActivity(intent);
    }

    @Override
    public void onTripLongPressed(DocumentSnapshot trip) {
        Intent intent = new Intent(NavigationDrawerActivity.this, AddOrEditTripActivity.class);
        intent.putExtra(TRIP_ID, trip.getId());
        intent.putExtra(DB_ID, mEmail);
        startActivity(intent);
    }

    @Override
    public void onIconPressed(final DocumentSnapshot trip, ImageView iconView) {
        if((boolean)trip.get("isFavourite")) {
            trips.document(trip.getId()).update("isFavourite", false);
            iconView.setImageResource(R.drawable.ic_bookmarked_not);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    tjdb.tripDao().deleteTrip(trip.toObject(Trip.class));
                }
            });
            Toast.makeText(this, "Favorilerden Kaldırıldı", Toast.LENGTH_SHORT).show();
        } else {
            trips.document(trip.getId()).update("isFavourite", true);
            iconView.setImageResource(R.drawable.ic_bookmarked);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    tjdb.tripDao().addTrip(trip.toObject(Trip.class));
                }
            });
            Toast.makeText(this, "Favorilere Eklendi", Toast.LENGTH_SHORT).show();
        }
    }


    //Storage perm
    private void reqStoragePerm() {

        if (checkPermission()) {

        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(NavigationDrawerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
