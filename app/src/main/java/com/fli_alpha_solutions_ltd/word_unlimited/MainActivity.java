package com.fli_alpha_solutions_ltd.word_unlimited;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fli_alpha_solutions_ltd.word_unlimited.Utils.langDict;
import com.fli_alpha_solutions_ltd.word_unlimited.appdata.AppConstant;
import com.fli_alpha_solutions_ltd.word_unlimited.appdata.AppController;
import com.fli_alpha_solutions_ltd.word_unlimited.service.MusicService;
import com.fli_alpha_solutions_ltd.word_unlimited.view.MyTextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private ImageView btn_setting, btn_conquitas, btn_ranking;

    private RelativeLayout btn_play;

    private LinearLayout main_setting_layout;

    private langDict word_obj;

    private ImageView traingle, logo;

    private Timer timer;

    private Animation move_down, move_up, move_up_title;

    // indicates whether the activity is linked to service player.
    private boolean mIsBound = false;

    // Saves the binding instance with the service.
    private MusicService mServ;

    private boolean flag_music = false, temp = false;

    private Dialog dialog;

    private boolean FLAG_LOADING = false;


    /***********************
     * IN APP PURCHASE
     ********************************/

    // Client used to interact with Google APIs
    private GoogleApiClient mGoogleApiClient;

    // Has the user clicked the sign-in button?

    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // request codes we use when invoking an external activity
    @SuppressWarnings("unused")
    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the Google API Client with access to Plus and Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN).addApi(Games.API)
                .addScope(Games.SCOPE_GAMES).build();


        AppConstant.LANGUAGE = AppController.getInstance().getPrefManger().getLanguage();


        // Starting the service of the player, if not already started.
        Intent music = new Intent(this, MusicService.class);
        // music.putExtra("music_name", "tired");
        startService(music);

        doBindService();

        loadAnimation();

        init();

       // new LoadViewTask().execute();


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isNetworkAvailable()) {
            // start the sign-in flow
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    private void loadAnimation() {
        move_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.move_down);
        move_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.move_up);
        move_up_title = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.move_up_title);


        move_up_title.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                btn_play.setVisibility(View.VISIBLE);

                YoYo.with(Techniques.FadeIn).duration(400).playOn(btn_play);

                btn_play.setOnClickListener(MainActivity.this);


                main_setting_layout.setVisibility(View.VISIBLE);

                YoYo.with(Techniques.FadeIn).duration(400).playOn(main_setting_layout);

                btn_setting.setOnClickListener(MainActivity.this);
                btn_conquitas.setOnClickListener(MainActivity.this);
                btn_ranking.setOnClickListener(MainActivity.this);


                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                YoYo.with(Techniques.Tada)
                                        .duration(500)
                                        .playOn(traingle);
                            }
                        });
                    }
                }, 0, 3000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        move_up.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (dialog != null) {
                    dialog.dismiss();
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }


    // To use the AsyncTask, it must be subclassed
    private class LoadViewTask extends AsyncTask<Void, Integer, Boolean> {
        // Before running code in separate thread
        @Override
        protected void onPreExecute() {
            // Create a new progress dialog
            //progressbar.setVisibility(View.VISIBLE);

            // load_animation();
            FLAG_LOADING = true;

        }

        // The code to be executed in a background thread.
        @Override
        protected Boolean doInBackground(Void... params) {

            word_obj = new langDict();

            if (word_obj.readDict(MainActivity.this.getResources().openRawResource(
                    MainActivity.this.getResources().getIdentifier(AppConstant.wordFileName[AppConstant.LANGUAGE], "raw", getPackageName())))) {
            } else {
                return false;
            }


            return true;
        }

        // Update the progress

        // after executing the code in the thread
        @Override
        protected void onPostExecute(Boolean result) {


            if (result) FLAG_LOADING = false;

        }

    }


    private void init() {

        btn_play = (RelativeLayout) findViewById(R.id.main_btn_play);


        btn_setting = (ImageView) findViewById(R.id.main_setting);
        btn_conquitas = (ImageView) findViewById(R.id.main_conquistas);
        btn_ranking = (ImageView) findViewById(R.id.main_ranking);


        traingle = (ImageView) findViewById(R.id.main_traingle);
        logo = (ImageView) findViewById(R.id.logo);

        logo.startAnimation(move_up_title);

        main_setting_layout = (LinearLayout) findViewById(R.id.main_setting_layout);


    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (temp && !FLAG_LOADING) {


            if (AppController.getInstance().getPrefManger().getMusicOnOff())
                mServ.start2("click", false);


            if (id == R.id.main_btn_play) {

                temp = false;
                AppController.getInstance().getPrefManger().setLevelNum(1);

                Intent intent = new Intent(MainActivity.this, GamePlay.class);
                startActivity(intent);

            }

            if (id == R.id.main_setting) {
                dialogSetting2();
            }

            if (id == R.id.main_ranking) {



                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id,
                        R.string.leaderboard_highestscore)) {
                    // Log.w(TAG,
                    // "*** Warning: setup problems detected. Sign in may not work!");
                }

                // start the sign-in flow
                // mSignInClicked = true;
                // mGoogleApiClient.connect();
                if (isSignedIn()) {
                    // startActivityForResult(
                    // Games.Leaderboards
                    // .getAllLeaderboardsIntent(mGoogleApiClient),
                    // RC_UNUSED);
                    saveScoreToLeaderBoard();

                    startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                            mGoogleApiClient,
                            getString(R.string.leaderboard_highestscore)),
                            RC_UNUSED);
                } else {
                    BaseGameUtils.makeSimpleDialog(this,
                            getString(R.string.leaderboards_not_available)).show();
                }
            }

            if(id == R.id.main_conquistas)
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("https://play.google.com/store/apps/details?id="
                                + AppConstant.APP_PNAME)));
            }
        }

    }


    private void dialogSetting() {

        final Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_setting);


        final RadioGroup radioSexGroup = (RadioGroup) dialog.findViewById(R.id.radioLanguge);


        RadioButton r1 = (RadioButton) dialog.findViewById(R.id.radioButtonEng);
        RadioButton r2 = (RadioButton) dialog.findViewById(R.id.radioButtonPor);

        if (AppController.getInstance().getPrefManger().getLanguage() == 0) {
            r1.setChecked(true);
        } else {
            r2.setChecked(true);
        }


        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                // get selected radio button from radioGroup
                int selectedId = radioSexGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                RadioButton languague_btn = (RadioButton) dialog.findViewById(selectedId);

                if (languague_btn.getText().toString().equalsIgnoreCase(getResources().getString(R.string.lan_eng))) {


                    if (AppConstant.LANGUAGE != AppConstant.LANGUAGE_ENG) {
                        AppController.getInstance().getPrefManger().setLanguage(AppConstant.LANGUAGE_ENG);

                        AppConstant.LANGUAGE = AppConstant.LANGUAGE_ENG;
                        AppController.getInstance().getPrefManger().setLevelNum(1);

                        new LoadViewTask().execute();

                    }
                } else {
                    if (AppConstant.LANGUAGE != AppConstant.LANGUAGE_POR) {
                        AppController.getInstance().getPrefManger().setLanguage(AppConstant.LANGUAGE_POR);

                        AppConstant.LANGUAGE = AppConstant.LANGUAGE_POR;
                        AppController.getInstance().getPrefManger().setLevelNum(1);

                        new LoadViewTask().execute();

                    }
                }


            }
        });


        dialog.show();
    }

    private void dialogSetting2() {

        dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_setting_2);

        final RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.dialogsetting_layout);

        layout.startAnimation(move_down);

        final MyTextView title = (MyTextView) dialog.findViewById(R.id.dialogsetting_title);
        final MyTextView audiotitle = (MyTextView) dialog.findViewById(R.id.dialogsetting_audiotitle);
        final MyTextView langtitle = (MyTextView) dialog.findViewById(R.id.dialogsetting_langtitle);
        final MyTextView checkbox_title = (MyTextView) dialog.findViewById(R.id.dialogsetting_checkbox_tv);


        CheckBox checkbox_audio = (CheckBox) dialog.findViewById(R.id.dialogsetting_checkbox);

        if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
            checkbox_audio.setChecked(false);
        } else {
            checkbox_audio.setChecked(true);
        }

        checkbox_audio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {

                    AppController.getInstance().getPrefManger().setMusicOnOff(false);
                    flag_music = false;

                    stopMusic();

                } else {
                    AppController.getInstance().getPrefManger().setMusicOnOff(true);
                    flag_music = true;
                    playMusic("menu_page");
                }
            }
        });


        Spinner spinner_language = (Spinner) dialog.findViewById(R.id.dialogsetting_spinner);

        List<String> list = new ArrayList<String>();

        if (AppController.getInstance().getPrefManger().getLanguage() == 0) {
            list.add(getResources().getString(R.string.lan_eng));
            list.add(getResources().getString(R.string.lan_por));

            title.setText(getResources().getString(R.string.setting_eng_title));
            audiotitle.setText(getResources().getString(R.string.setting_eng_audiotitle));
            langtitle.setText(getResources().getString(R.string.setting_eng_langtitle));
            checkbox_title.setText(getResources().getString(R.string.setting_eng_checkbox));

        } else {
            list.add(getResources().getString(R.string.lan_por));
            list.add(getResources().getString(R.string.lan_eng));

            title.setText(getResources().getString(R.string.setting_por_title));
            audiotitle.setText(getResources().getString(R.string.setting_por_audiotitle));
            langtitle.setText(getResources().getString(R.string.setting_por_langtitle));
            checkbox_title.setText(getResources().getString(R.string.setting_por_checkbox));
        }


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, R.layout.spinner_item, list);


        spinner_language.setAdapter(dataAdapter);

        spinner_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                String item = parent.getItemAtPosition(position).toString();


                if (item.equalsIgnoreCase(getResources().getString(R.string.lan_eng))) {


                    if (AppConstant.LANGUAGE != AppConstant.LANGUAGE_ENG) {
                        AppController.getInstance().getPrefManger().setLanguage(AppConstant.LANGUAGE_ENG);

                        AppConstant.LANGUAGE = AppConstant.LANGUAGE_ENG;
                        AppController.getInstance().getPrefManger().setLevelNum(1);

                        title.setText(getResources().getString(R.string.setting_eng_title));
                        audiotitle.setText(getResources().getString(R.string.setting_eng_audiotitle));
                        langtitle.setText(getResources().getString(R.string.setting_eng_langtitle));
                        checkbox_title.setText(getResources().getString(R.string.setting_eng_checkbox));

                        new LoadViewTask().execute();

                    }
                } else {
                    if (AppConstant.LANGUAGE != AppConstant.LANGUAGE_POR) {
                        AppController.getInstance().getPrefManger().setLanguage(AppConstant.LANGUAGE_POR);

                        AppConstant.LANGUAGE = AppConstant.LANGUAGE_POR;
                        AppController.getInstance().getPrefManger().setLevelNum(1);

                        title.setText(getResources().getString(R.string.setting_por_title));
                        audiotitle.setText(getResources().getString(R.string.setting_por_audiotitle));
                        langtitle.setText(getResources().getString(R.string.setting_por_langtitle));
                        checkbox_title.setText(getResources().getString(R.string.setting_por_checkbox));

                        new LoadViewTask().execute();

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //dialog = null;

            }
        });
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK &&
                        event.getAction() == KeyEvent.ACTION_UP &&
                        !event.isCanceled()) {
                    layout.startAnimation(move_up);
                    return true;
                }
                return false;
            }
        });

        dialog.show();

    }

    private void playMusic(String music_name) {
        mServ.start(music_name, true);
    }

    private void stopMusic() {
        mServ.stop();
        mServ.stop2();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {


        mServ = ((MusicService.ServiceBinder) iBinder).getService();

        flag_music = AppController.getInstance().getPrefManger().getMusicOnOff();

        if (flag_music) {
            playMusic("menu_page");
        }


    }


    @Override
    public void onServiceDisconnected(ComponentName componentName) {

        mServ = null;
    }

    public void doBindService() {
        // activity connects to the service.
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();
        mServ.stop();
        mServ.stop2();
        doUnbindService();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (flag_music) {
            playMusic("menu_page");
        }
        temp = true;

    }

    @Override
    public void onPause() {
        super.onPause();
        if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
            mServ.stop();
            mServ.stop2();
        }
        temp = false;

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        doUnbindService();
    }


    public void doUnbindService() {
        // disconnects the service activity.
        if (mIsBound) {
            unbindService(this);
            mIsBound = false;
        }
    }


    /*******************************
     * IN APP PURCHASE
     **************************/


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    public void saveScoreToLeaderBoard() {

        int high_score = AppController.getInstance().getPrefManger().getHighScoreInt();

        if (high_score != 0) {
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    getString(R.string.leaderboard_highestscore), high_score);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO Auto-generated method stub
        // Log.d("DEBUG", "onConnectionFailed(): attempting to resolve");
        if (mResolvingConnectionFailure) {
            Log.d("DEBUG", "onConnectionFailed(): already resolving");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN,
                    getString(R.string.signin_other_error))) {
                mResolvingConnectionFailure = false;
            }
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        // TODO Auto-generated method stub
        // Log.d("DEBUG", "onConnected(): connected to Google APIs");

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        // TODO Auto-generated method stub
        // Log.d("DEBUG", "onConnectionSuspended(): attempting to connect");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode,
                        resultCode, R.string.signin_other_error);
            }
        }
    }


}
