package odev.com.geziodev.room;

import android.arch.persistence.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {

    @TypeConverter
    public static Date toDate(String stringDate){
        SimpleDateFormat format = new SimpleDateFormat("d/M/y");
        Date date = null;
        try {
            date = format.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @TypeConverter
    public static String fromDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/y");
        return sdf.format(date);
    }
}