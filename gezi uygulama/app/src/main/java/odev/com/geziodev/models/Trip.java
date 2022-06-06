package odev.com.geziodev.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "trips")
public class Trip {
    @NonNull
    @PrimaryKey
    private String tripUID;
    private String tripName;
    private String destination;
    private String tripType;
    private int price;
    private Date startDate;
    private Date endDate;
    private double rating;
    public String imagePath;
    public boolean isFavourite;

    public Trip(String tripName, String destination, String tripType, int price, Date startDate, Date endDate, double rating, String imagePath, boolean isFavourite) {
        this.tripName = tripName;
        this.destination = destination;
        this.tripType = tripType;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rating = rating;
        this.imagePath = imagePath;
        this.isFavourite = isFavourite;
        tripUID = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
    }

    public Trip() {
    }

    public String getTripName() {
        return tripName;
    }

    public String getDestination() {
        return destination;
    }

    public String getTripType() {
        return tripType;
    }

    public int getPrice() {
        return price;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public double getRating() {
        return rating;
    }

    public String getImagePath() {
        return imagePath;
    }

    public boolean getIsFavourite() {
        return isFavourite;
    }

    public String getTripUID() {
        return tripUID;
    }

    public void setTripUID(String tripUID) {
        this.tripUID = tripUID;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setTripType(String tripType) {
        this.tripType = tripType;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }
}
