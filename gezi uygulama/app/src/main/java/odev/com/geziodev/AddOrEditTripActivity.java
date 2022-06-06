package odev.com.geziodev;

import android.Manifest;
import android.app.Activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import odev.com.geziodev.models.Trip;
import odev.com.geziodev.utils.CustomDatePickerFragment;


public class AddOrEditTripActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, EventListener<DocumentSnapshot> {

    public static final int SELECTPHOTO_REQUEST_CODE = 100;
    public static final int TAKEPHOTO_REQUEST_CODE = 110;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private static final String TAG = "TripDetail";

    private EditText tripName;
    private EditText destination;
    private RadioGroup tripType;
    private Button startDateButton;
    private Button endDateButton;
    private TextView priceTextView;
    private SeekBar price;
    private RatingBar rating;
    private TextView imagePathTextView;


    private String mStartDate;
    private String mEndDate;
    private String mPhotoPath;
    private boolean currentDatePick;
    private Uri photoUri;

    private FirebaseFirestore mFirestore;
    private DocumentReference mTripRef;
    private String dbId;
    private String tripId;


    public static final String TRIPNAME = "tripname";
    public static final String DESTINATION = "destination";
    public static final String TRIPTYPE = "triptype";
    public static final String STARTDATE = "startdate";
    public static final String ENDDATE = "enddate";
    public static final String PRICE = "price";
    public static final String RATING = "rating";
    public static final String PHOTOPATH = "path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);
        reqCameraAccess();

        initFirestore();

        initViews();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "trip:onEvent", e);
            return;
        }

        onTripLoaded(snapshot.toObject(Trip.class));
    }

    @Override
    public void onBackPressed() {
        if(dbId == null) {
            finish();
        }
        Intent intent = new Intent(AddOrEditTripActivity.this, NavigationDrawerActivity.class);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    //Inits
    private void initFirestore() {
        tripId= getIntent().getExtras().getString(NavigationDrawerActivity.TRIP_ID);
        dbId = getIntent().getExtras().getString(NavigationDrawerActivity.DB_ID);
        mFirestore = null;
        mTripRef = null;
        if(dbId != null && tripId != null) {
            // Initialize Firestore
            mFirestore = FirebaseFirestore.getInstance();
            // Get reference to the trip
            mTripRef = mFirestore.collection(dbId).document(tripId);
            mTripRef.addSnapshotListener(this);
        }
    }

    private void initViews() {
        tripName = findViewById(R.id.trip_name_editText);
        destination = findViewById(R.id.destination_editText);
        tripType = findViewById(R.id.trip_type_radioGroup);
        priceTextView = findViewById(R.id.seek_bar_textView);
        price = findViewById(R.id.price_seekBar);
        startDateButton = findViewById(R.id.start_date_button);
        endDateButton = findViewById(R.id.end_date_button);
        rating = findViewById(R.id.rating_bar);
        imagePathTextView = findViewById(R.id.image_path_textView);
        price.setMax(200);
        price.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                priceTextView.setText("Fiyat (" + price.getProgress() * 10 + " TL)");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mStartDate = null;
        mEndDate = null;
        mPhotoPath = null;
    }

    private void onTripLoaded(Trip trip) {
        tripName.setText(trip.getTripName());
        destination.setText(trip.getDestination());
        if(trip.getTripType().equals("City Break")) {
            tripType.check(R.id.city_break_radioButton);
        } else if(trip.getTripType().equals("Seaside")) {
            tripType.check(R.id.seaside_radioButton);
        } else if(trip.getTripType().equals("Mountains")) {
            tripType.check(R.id.mountains_radioButton);
        }
        priceTextView.setText("Fiyat (" + trip.getPrice() + " TL)");
        price.setProgress(trip.getPrice() / 10);
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/y");
        mStartDate = sdf.format(trip.getStartDate());
        mEndDate = sdf.format(trip.getEndDate());
        startDateButton.setHint(mStartDate);
        endDateButton.setHint(mEndDate);
        rating.setRating((float)trip.getRating());
        mPhotoPath = trip.getImagePath();
        imagePathTextView.setText(mPhotoPath);
    }


    //Save data
    public void btnSaveOnClick(View view) {
        if(!checkDataIntegrity()) {
            return;
        }
        String mTripName = tripName.getText().toString();
        String mDestination = destination.getText().toString();
        String mTripType = null;
        switch (tripType.getCheckedRadioButtonId()) {
            case R.id.city_break_radioButton:
                mTripType = "Şehir";
                break;
            case R.id.seaside_radioButton:
                mTripType = "Sahil";
                break;
            case R.id.mountains_radioButton:
                mTripType = "Dağlar";
                break;
        }
        String mPrice = Integer.toString(price.getProgress() * 10);
        String mRating = Float.toString(rating.getRating());

        //Edit
        if(mTripRef != null) {
            SimpleDateFormat format = new SimpleDateFormat("d/M/y");
            Date sDate = null;
            Date eDate = null;
            try {
                sDate = format.parse(mStartDate);
                eDate = format.parse(mEndDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mTripRef.update(
                    "destination", mDestination,
                    "endDate", eDate,
                    "imagePath", mPhotoPath,
                    "price", price.getProgress() * 10,
                    "rating", (double)rating.getRating(),
                    "startDate", sDate,
                    "tripName", mTripName,
                    "tripType", mTripType);

            Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Add
        Intent intent = new Intent(AddOrEditTripActivity.this, NavigationDrawerActivity.class);
        intent.putExtra(TRIPNAME, mTripName);
        intent.putExtra(DESTINATION, mDestination);
        intent.putExtra(TRIPTYPE, mTripType);
        intent.putExtra(STARTDATE, mStartDate);
        intent.putExtra(ENDDATE, mEndDate);
        intent.putExtra(PRICE, mPrice);
        intent.putExtra(RATING, mRating);
        intent.putExtra(PHOTOPATH, mPhotoPath);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private boolean checkDataIntegrity() {
        if(tripName.getText().toString() == null || tripName.getText().toString().isEmpty()) {
            Toast.makeText(AddOrEditTripActivity.this, "Boş Olamaz", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(destination.getText().toString() == null || destination.getText().toString().isEmpty()) {
            Toast.makeText(AddOrEditTripActivity.this, "Boş Olamaz", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(tripType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(AddOrEditTripActivity.this, "Boş Olamaz", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(price.getProgress() == 0) {
            Toast.makeText(AddOrEditTripActivity.this, "Boş Olamaz", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mStartDate == null) {
            Toast.makeText(AddOrEditTripActivity.this, "Boş Olamaz", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mEndDate == null) {
            Toast.makeText(AddOrEditTripActivity.this, "Boş Olamaz", Toast.LENGTH_SHORT).show();
            return false;
        }
        SimpleDateFormat format = new SimpleDateFormat("d/M/y");
        try {
            Date sDate = format.parse(mStartDate);
            Date eDate = format.parse(mEndDate);
            if (eDate.compareTo(sDate) < 0) {
                Toast.makeText(AddOrEditTripActivity.this, "Tarih Tutarsızlığı", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(mPhotoPath == null) {
            Toast.makeText(AddOrEditTripActivity.this, "Lütfen Fotoğraf Seçin", Toast.LENGTH_SHORT).show();
            return false;

        }

        return true;
    }


    //Date related
    public void btnStartDatePickerOnClick(View view) {
        currentDatePick = false;
        DialogFragment newFragment = new CustomDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void btnEndDatePickerOnClick(View view) {
        currentDatePick = true;
        DialogFragment newFragment = new CustomDatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {

        if (currentDatePick == false) {
            mStartDate = day + "/" + (month + 1) + "/" + year;
            startDateButton.setHint(mStartDate);

        } else {
            mEndDate = day + "/"  + (month + 1) + "/" + year;
            endDateButton.setHint(mEndDate);
        }

    }


    //Trip's image related
    public void btnSelectPhotoOnClick(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, SELECTPHOTO_REQUEST_CODE);
    }

    public void btnTakePhotoOnClick(View view) {

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        photoUri  = FileProvider.getUriForFile(this,"odev.com.geziodev.provider", photoFile);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoUri);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        startActivityForResult(intent, TAKEPHOTO_REQUEST_CODE);

    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mPhotoPath = image.getAbsolutePath();
        return image;
    }


    //Get image / taken photo
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == SELECTPHOTO_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mPhotoPath = cursor.getString(columnIndex);
                cursor.close();

                imagePathTextView.setText(mPhotoPath);
            }
        } else if(requestCode == TAKEPHOTO_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                imagePathTextView.setText(mPhotoPath);
            }
        }
    }


    //Camera permission
    private void reqCameraAccess() {
        if (checkPermission()) {
            //main logic or main code
            // . write your main code to execute, It will execute if the permission is already given.
        } else {
            requestPermission();
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
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
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
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
        new AlertDialog.Builder(AddOrEditTripActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }



}
