package com.fli_alpha_solutions_ltd.word_unlimited.appdata;

import android.app.Application;
import android.util.DisplayMetrics;

import com.fli_alpha_solutions_ltd.word_unlimited.sharedPref.PrefManager;


/**
 * Created by comsol on 10/26/2015.
 */
public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();

    public static final String SCREENSIZE_LDPI = "ldpi";
    public static final String SCREENSIZE_MDPI = "mdpi";
    public static final String SCREENSIZE_HDPI = "hdpi";
    public static final String SCREENSIZE_XHDPI = "xhdpi";
    public static final String SCREENSIZE_XXHDPI = "xxhdpi";
    public static final String SCREENSIZE_XXXHDPI = "xxxhdpi";

    private static AppController mInstance;


    private float scale;

    private int density;

    private PrefManager pref;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        pref = new PrefManager(this);

        this.density = getResources().getDisplayMetrics().densityDpi;
        this.scale = getResources().getDisplayMetrics().density;


        // startService(new Intent(this, TrackLocation.class));

    }

    public PrefManager getPrefManger() {
        if (pref == null) {
            pref = new PrefManager(this);
        }

        return pref;
    }


    public static synchronized AppController getInstance() {
        return mInstance;
    }


    public int getPixelValue(int dps) {
        int pixels = (int) (dps * scale + 0.5f);
        return pixels;
    }

    public String getDeviceScreenSize() {
        // TODO Auto-generated method stub


        String device_screen_size = SCREENSIZE_HDPI;

        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
                // Log.d("DEBUG", "Density Low" );
                device_screen_size = SCREENSIZE_LDPI;

                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                // Log.d("DEBUG", "Density Medium" );
                device_screen_size = SCREENSIZE_MDPI;

                break;
            case DisplayMetrics.DENSITY_HIGH:
                // Toast.makeText(context, "HDPI", Toast.LENGTH_SHORT).show();
                device_screen_size = SCREENSIZE_HDPI;
                // setting upper weight

                break;
            case DisplayMetrics.DENSITY_XHIGH:

                device_screen_size = SCREENSIZE_XHDPI;


                break;

            case DisplayMetrics.DENSITY_XXHIGH:
                // Log.d("DEBUG", "it 2");
                // Toast.makeText(context, "xXHDPI", Toast.LENGTH_SHORT).show();
                device_screen_size = SCREENSIZE_XXHDPI;

                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                // Log.d("DEBUG", "it 3");
                // Toast.makeText(context, "xXHDPI", Toast.LENGTH_SHORT).show();
                device_screen_size = SCREENSIZE_XXXHDPI;

                break;

        }
        return device_screen_size;
    }


}