package com.fli_alpha_solutions_ltd.word_unlimited;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;

import com.fli_alpha_solutions_ltd.word_unlimited.Utils.langDict;
import com.fli_alpha_solutions_ltd.word_unlimited.appdata.AppConstant;
import com.fli_alpha_solutions_ltd.word_unlimited.appdata.AppController;


public class SplashActivity extends AppCompatActivity {

    private langDict word_obj;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //menyembunyikan title bar di layar acitivy
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        //membuat activity menjadi fullscreen
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /** Sets a layout for this activity */
        setContentView(R.layout.activity_splash);

        new LoadViewTask().execute();



    }

    // To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Boolean> {
        // Before running code in separate thread
        @Override
        protected void onPreExecute() {
            // Create a new progress dialog
            //progressbar.setVisibility(View.VISIBLE);

            // load_animation();
            AppConstant.LANGUAGE = AppController.getInstance().getPrefManger().getLanguage();

        }

        // The code to be executed in a background thread.
        @Override
        protected Boolean doInBackground(Void... params) {

            word_obj = new langDict();

            if (word_obj.readDict(SplashActivity.this.getResources().openRawResource(
                    SplashActivity.this.getResources().getIdentifier(AppConstant.wordFileName[AppConstant.LANGUAGE], "raw", getPackageName())))) {
            } else {
                return false;
            }


            return true;
        }

        // Update the progress

        // after executing the code in the thread
        @Override
        protected void onPostExecute(Boolean result) {


            if (result)
            {
                /** Creates a count down timer, which will be expired after 5000 milliseconds */
                new CountDownTimer(4000,1000) {

                    /** This method will be invoked on finishing or expiring the timer */
                    @Override
                    public void onFinish() {
                        /** Creates an intent to start new activity */
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);

                        //memulai activity baru ketika waktu timer habis
                        startActivity(intent);

                        //menutup layar activity
                        finish();

                    }

                    /** This method will be invoked in every 1000 milli seconds until
                     * this timer is expired.Because we specified 1000 as tick time
                     * while creating this CountDownTimer
                     */
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }
                }.start();
            }

        }

    }
}
