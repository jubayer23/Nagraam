package com.fli_alpha_solutions_ltd.word_unlimited;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fli_alpha_solutions_ltd.word_unlimited.appdata.AppConstant;
import com.fli_alpha_solutions_ltd.word_unlimited.appdata.AppController;
import com.fli_alpha_solutions_ltd.word_unlimited.service.MusicService;
import com.fli_alpha_solutions_ltd.word_unlimited.util.IabHelper;
import com.fli_alpha_solutions_ltd.word_unlimited.util.IabResult;
import com.fli_alpha_solutions_ltd.word_unlimited.util.Inventory;
import com.fli_alpha_solutions_ltd.word_unlimited.util.Purchase;
import com.fli_alpha_solutions_ltd.word_unlimited.view.MyTextView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.nineoldandroids.animation.Animator;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GamePlay extends AppCompatActivity implements View.OnClickListener, ServiceConnection {


    private static int levelNum = 1;

    private static int levelCircle = 0, levelProgress = 1;

    private LinearLayout layout_keyboard, layout_solve_keyboard, layout_pu;

    private List<String> wordList;

    private static String levelQuestString = "";

    private static String shuffleString = "";

    private ArrayList<ImageView> keyboard = new ArrayList<ImageView>();
    private ArrayList<ImageView> keyboard_solve = new ArrayList<ImageView>();

    private static Integer[] string_track;
    private char userWordCharArray[];

    private ImageView correctWordComplement;


    private static HashMap<Integer, Integer> keyboard_map = new HashMap<Integer, Integer>();

    private static HashMap<Integer, Integer> keyboard_inverse_map = new HashMap<Integer, Integer>();

    private DonutProgress donutProgress;

    private Timer timer;

    private CountDownTimer countdown_timer;

    private Animation move_left;
    private Animation move_down, move_up;


    private Button btn_solve_letter, btn_add_time, btn_jump_level;

    private MyTextView pu_notification;

    // indicates whether the activity is linked to service player.
    private boolean mIsBound = false;

    // Saves the binding instance with the service.
    private MusicService mServ;

    private float remaining_sec;

    private static final int COUNTDOWN_SEC = 6;
    private static final int COUNTDOWN_MILI_SEC = COUNTDOWN_SEC * 1000;

    private double timeSeconds = 0.0D;

    private Dialog dialog;

    private ArrayList<String> similarWordList = new ArrayList<String>();

    private boolean FLAG_GAME_RESUME = true;

    private static int numOfJumpPowerUsed = 0;


    /***********************
     * IN APP PURCHASE
     ********************************/


    // OBJECT VARIABLE
    private IabHelper mHelper;

    static final String SKU_RESET = "buy_power";
    private boolean flag_reset;
    // VARIABLE IN APP PURCHASE DIALOG
    private Button btn_reset;

    static final int REQUEST_PURCHASE = 10001;
    private static final String TAG = "Nagram";

    // ADMOB VARIABLE
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        initialAppPurchase();

        //this method is called for the single time
        loadAdView();
        // Starting the service of the player, if not already started.
        Intent music = new Intent(this, MusicService.class);
        // music.putExtra("music_name", "tired");
        startService(music);

        doBindService();

        //this method is called for the single time
        init();
        //this method is called for the single time
        loadAnimation();
        //this method is called for the single time
        setDonutCircleWidthAndHeiht();

        /****/

        FLAG_GAME_RESUME = false;
        VisibleInsvisibleOnGameOver();

        doOnlyFirstTimeInitialization();

        getLevelNum();

        getLevelData();

        getlevelQuest(levelCircle);

        getShuffleString(levelQuestString);

        setupKeyboard();

        updatePowerUp("both");
        levelProgress = 1;
        VisibleInsvisibleOnGameOver();

        /*****/


    }

    private void init() {

        wordList = new ArrayList<String>();

        layout_keyboard = (LinearLayout) findViewById(R.id.layout_keybaord);

        layout_solve_keyboard = (LinearLayout) findViewById(R.id.layout_keybaord_solve);

        correctWordComplement = (ImageView) findViewById(R.id.main_correct_word_complement);
        correctWordComplement.setVisibility(View.INVISIBLE);

        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);

        btn_solve_letter = (Button) findViewById(R.id.btn_solve_letter);
        btn_solve_letter.setOnClickListener(this);
        btn_add_time = (Button) findViewById(R.id.btn_add_time);
        btn_add_time.setOnClickListener(this);
        btn_jump_level = (Button) findViewById(R.id.btn_jump_level);
        btn_jump_level.setOnClickListener(this);

        pu_notification = (MyTextView) findViewById(R.id.pu_notification_tv);

        layout_pu = (LinearLayout) findViewById(R.id.pu_layout);

    }

    public void loadAdView() {
        AdView mAdView = (AdView) findViewById(R.id.adViewMain);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // load INTERTITIAL ADD VIEW


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.intertitial_ad_unit_id));
        requestNewInterstitial();

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();


                VisibleInsvisibleOnGameOver();

                if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
                    mServ.start("errado", false);
                }


                dialogGameOver();
                // beginPlayingGame();
            }
        });


    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(
                "554FD1C059BF37BF1981C59FF9E1DAE0").build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void doOnlyFirstTimeInitialization() {
        levelCircle = 0;
        numOfJumpPowerUsed = 0;
        levelProgress = 1;
        timeSeconds = 0.0D;
    }

    private void getLevelNum() {

        levelNum = AppController.getInstance().getPrefManger().getLevelNum();

    }

    private void getLevelData() {
        wordList.clear();

        if (levelNum == 1) {
            wordList.addAll(AppConstant.word_length_3);
        } else if (levelNum == 2) {
            wordList.addAll(AppConstant.word_length_4);
        } else if (levelNum == 3) {
            wordList.addAll(AppConstant.word_length_5);
        } else if (levelNum == 4) {
            wordList.addAll(AppConstant.word_length_6);
        } else {
            wordList.addAll(AppConstant.word_length_6);
        }

        Collections.shuffle(wordList);
    }

    private void getlevelQuest(int levelCircle) {

        if (levelCircle < wordList.size()) {
            levelQuestString = wordList.get(levelCircle).toLowerCase();
        } else {
            levelQuestString = wordList.get(0).toLowerCase();
        }


        string_track = new Integer[levelQuestString.length()];
        userWordCharArray = new char[levelQuestString.length()];
        // Log.d("DEBUG", String.valueOf(temp.length));
        for (int i = 0; i < levelQuestString.length(); i++) {
            string_track[i] = 0;
            userWordCharArray[i] = '0';
        }

        similarWordList.clear();

        try {
            if (AppConstant.similarWordMap.get(levelQuestString) != null) {
                similarWordList.addAll(AppConstant.similarWordMap.get(levelQuestString));
            }
        } catch (Exception e) {

        }

    }

    private void getShuffleString(String levelQuestString) {

        do {
            shuffleString = shuffle(levelQuestString);

        }
        while (levelQuestString.equalsIgnoreCase(shuffleString) || similarWordList.contains(shuffleString));


    }

    private void setupKeyboard() {
        keyboard.clear();
        keyboard_solve.clear();

        int marginLeft[] = new int[6];
        int marginRight[] = new int[6];

        if (layout_keyboard.getChildCount() > 0)
            layout_keyboard.removeAllViews();

        if (layout_solve_keyboard.getChildCount() > 0)
            layout_solve_keyboard.removeAllViews();

        if (levelQuestString.length() == 3) {
            marginLeft[1] = AppController.getInstance().getPixelValue(50);
            marginRight[1] = AppController.getInstance().getPixelValue(50);

        } else if (levelQuestString.length() == 4) {

            if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_MDPI) || AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_LDPI)
                    || AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_HDPI)) {
                marginLeft[0] = 10;
                marginLeft[1] = 10;
                marginLeft[2] = 10;
                marginLeft[3] = 10;
                marginRight[3] = 16;
            } else {
                marginLeft[0] = 15;
                marginLeft[1] = 20;
                marginLeft[2] = 20;
                marginLeft[3] = 20;
                marginRight[3] = 20;
            }
        } else if (levelQuestString.length() == 5) {


            if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_MDPI) || AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_LDPI)
                    || AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_HDPI)) {
                marginLeft[0] = 5;
                marginLeft[1] = 3;
                marginLeft[2] = 3;
                marginLeft[3] = 3;
                marginLeft[4] = 3;
                marginRight[4] = 18;
            } else {
                marginLeft[0] = 5;
                marginLeft[1] = 10;
                marginLeft[2] = 10;
                marginLeft[3] = 10;
                marginLeft[4] = 10;
                marginRight[4] = 18;
            }

        } else if (levelQuestString.length() == 6) {


            if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_MDPI) || AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_LDPI)
                    || AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_HDPI)) {
                marginLeft[0] = 5;
                marginLeft[1] = 3;
                marginLeft[2] = 3;
                marginLeft[3] = 3;
                marginLeft[4] = 3;
                marginLeft[5] = 3;
                marginRight[5] = 5;
            } else {
                marginLeft[0] = 0;
                marginLeft[1] = 5;
                marginLeft[2] = 5;
                marginLeft[3] = 5;
                marginLeft[4] = 5;
                marginLeft[5] = 5;
                marginRight[5] = 5;
            }
        }


        for (int i = 0; i < levelQuestString.length(); i++) {


            /***************************************KEYBOARD*********************************************/
            ImageView img = new ImageView(this);

            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            imgParams.setMargins(marginLeft[i], 0, marginRight[i], 0);
            img.setLayoutParams(imgParams);
            img.setImageResource(GamePlay.this.getResources()
                    .getIdentifier(getDrawableFileName(shuffleString.charAt(i)), "drawable",
                            getPackageName()));

            img.setOnClickListener(this);

            keyboard.add(img);


            layout_keyboard.addView(img);

            /***************************************SOLVE KEYBOARD*********************************************/
            ImageView img_solve = new ImageView(this);

            LinearLayout.LayoutParams imgParams_solve = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            imgParams_solve.setMargins(marginLeft[i], 0, marginRight[i], 0);
            img_solve.setLayoutParams(imgParams_solve);
            img_solve.setImageResource(R.drawable.letter_blank_background);

            img_solve.setOnClickListener(this);

            keyboard_solve.add(img_solve);


            layout_solve_keyboard.addView(img_solve);
        }


    }


    private void setDonutCircleWidthAndHeiht() {
        int width = AppController.getInstance().getPixelValue(350);
        int height = AppController.getInstance().getPixelValue(340);

        if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_LDPI)) {

            width = AppController.getInstance().getPixelValue(110);
            height = AppController.getInstance().getPixelValue(100);
        }
        if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_HDPI)) {

            width = AppController.getInstance().getPixelValue(270);
            height = AppController.getInstance().getPixelValue(260);

        }
        if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_MDPI)) {
            width = AppController.getInstance().getPixelValue(210);
            height = AppController.getInstance().getPixelValue(200);
        }
        if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_XHDPI)) {

            width = AppController.getInstance().getPixelValue(310);
            height = AppController.getInstance().getPixelValue(300);

        }
        if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_XXHDPI)) {

        }
        if (AppController.getInstance().getDeviceScreenSize().equalsIgnoreCase(AppController.SCREENSIZE_XXXHDPI)) {

        }


        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(width, height);

        donutProgress.setLayoutParams(imgParams);
    }

    public void startCounDownAndTimerForProgessBar(int countdown_sec) {
        donutProgress.setText(String.valueOf(levelProgress));


        donutProgress.setProgress(0);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        donutProgress.setProgress(donutProgress.getProgress() + 1);
                    }
                });
            }
        }, 0, 60);


        countdown_timer = new CountDownTimer(countdown_sec, 1000) {

            public void onTick(long millisUntilFinished) {
                remaining_sec = (float) (millisUntilFinished / 1000);
                // donutProgress.setText(String.valueOf(remaining_sec));
            }

            public void onFinish() {

                AppConstant.GAMEOVER_COUNTER++;
                // Log.d("DEBUG_OnFinish", String.valueOf(FLAG_GAME_RESUME));
                FLAG_GAME_RESUME = false;

                stopMusic();
                if (timer != null) {
                    timer.cancel();
                }

                if (AppConstant.GAMEOVER_COUNTER == 2) {
                    AppConstant.GAMEOVER_COUNTER = 0;
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.show();
                    } else {
                        VisibleInsvisibleOnGameOver();

                        if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
                            mServ.start("errado", false);
                        }


                        dialogGameOver();
                    }

                } else {
                    VisibleInsvisibleOnGameOver();

                    if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
                        mServ.start("errado", false);
                    }


                    dialogGameOver();
                }

            }
        }.start();
    }

    public String shuffle(String input) {
        List<Character> characters = new ArrayList<Character>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while (characters.size() != 0) {
            int randPicker = (int) (Math.random() * characters.size());
            output.append(characters.remove(randPicker));
        }
        return output.toString();
    }

    @Override
    public void onClick(View view) {

        if (FLAG_GAME_RESUME) {
            if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
                mServ.start2("click", false);
            }


            if (keyboard.contains(view)) {
                // Toast.makeText(this,
                //         String.valueOf(keyboardCharArray[keyboard.indexOf(view)]),
                //         Toast.LENGTH_SHORT).show();
                updateSolveKeyboard(shuffleString.charAt(keyboard.indexOf(view)),
                        keyboard.indexOf(view), view, 0);


                // checkarray(userword);

            }
            if (keyboard_solve.contains(view)) {
                if (string_track[keyboard_solve.indexOf(view)] == 1) {
                    manageChangeInSolveKeyboard(keyboard_solve.indexOf(view));
                }

            }


            if (view.getId() == R.id.btn_solve_letter) {
                if (AppController.getInstance().getPrefManger().getPuRemaining() > 0) {


                    solveLetter();

                    updatePowerUp("minus");
                } else {
                    YoYo.with(Techniques.Tada)
                            .duration(500)
                            .playOn(pu_notification);
                }
            }
            if (view.getId() == R.id.btn_jump_level) {
                if (AppController.getInstance().getPrefManger().getPuRemaining() > 0) {
                    jumpLevel2();

                    updatePowerUp("minus");
                } else {
                    YoYo.with(Techniques.Tada)
                            .duration(500)
                            .playOn(pu_notification);
                }
            }
            if (view.getId() == R.id.btn_add_time) {
                if (AppController.getInstance().getPrefManger().getPuRemaining() > 0) {
                    addTime();

                    updatePowerUp("minus");
                } else {
                    YoYo.with(Techniques.Tada)
                            .duration(500)
                            .playOn(pu_notification);
                }
            }
        }


    }

    private void playMusic(String music_name) {
        if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
            mServ.start(music_name, true);
        }

    }

    private void stopMusic() {
        if (mServ != null) {
            mServ.stop();
        }
    }

    private void updateSolveKeyboard(char onSolveChar, int keyboard_pos,
                                     View view, int index_start) {
        // TODO Auto-generated method stub

        for (int index = index_start; index < levelQuestString.length(); index++) {

            if (string_track[index] == 0) {


                keyboard_solve.get(index).setImageResource(GamePlay.this.getResources()
                        .getIdentifier(getDrawableFileName(onSolveChar), "drawable",
                                getPackageName()));

                userWordCharArray[index] = onSolveChar;

                string_track[index] = 1;

                keyboard_map.put(index, keyboard_pos);
                keyboard_inverse_map.put(keyboard_pos, index);

                ImageView v = (ImageView) view;
                v.setImageAlpha(128);
                v.setEnabled(false);


                String userword = String.copyValueOf(userWordCharArray);

                if (userword.equalsIgnoreCase(levelQuestString) || similarWordList.contains(userword)) {

                    if (FLAG_GAME_RESUME) {
                        FLAG_GAME_RESUME = false;

                        // Log.d("DEBUG_solve", String.valueOf(FLAG_GAME_RESUME));

                        //Toast.makeText(this, "Match", Toast.LENGTH_LONG).show();

                        stopMusic();

                        showLevelCompleteComplement();
                    }


                    //    }
                    // }, 3000);
                } else {
                    if (AppConstant.map_word_is_correct.get(userword) != null) {
                        if (AppConstant.map_word_is_correct.get(userword) == 1) {
                            if (FLAG_GAME_RESUME) {

                                FLAG_GAME_RESUME = false;

                                // Log.d("DEBUG_solve", String.valueOf(FLAG_GAME_RESUME));

                                //Toast.makeText(this, "Match", Toast.LENGTH_LONG).show();

                                stopMusic();

                                showLevelCompleteComplement();
                            }
                        }
                    }

                }
                break;

            }

        }

    }


    private void manageChangeInSolveKeyboard(int onSolveKeyboard_pos) {

        Log.d("onSolveKeyboard_pos", String.valueOf(onSolveKeyboard_pos));

        string_track[onSolveKeyboard_pos] = 0;


        keyboard_solve.get(onSolveKeyboard_pos).setImageResource(R.drawable.letter_blank_background);

        keyboard.get(keyboard_map.get(onSolveKeyboard_pos)).setImageAlpha(255);
        keyboard.get(keyboard_map.get(onSolveKeyboard_pos)).setEnabled(true);

    }

    private void showLevelCompleteComplement() {


        /*    stop Timer And CoundDown     */
        if (countdown_timer != null)
            countdown_timer.cancel();
        if (timer != null) {
            timer.cancel();
        }

        timeSeconds += 6.0D * (donutProgress.getProgress() / 100.0D);


        correctWordComplement.setVisibility(View.VISIBLE);

        correctWordComplement.setImageResource(GamePlay.this.getResources()
                .getIdentifier(AppConstant.complementList[AppConstant.LANGUAGE][levelCircle % 6], "drawable",
                        getPackageName()));

        correctWordComplement.startAnimation(move_left);


        if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
            mServ.start("correto", false);
        }


        // Execute some code after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                VisibleInsvisibleOnGameOver();

                startNextLevel();
            }
        }, 2000);

    }

    private void startNextLevel() {
        cleanUp();


        levelCircle++;
        levelProgress++;

        donutProgress.setText(String.valueOf(levelProgress));

        if ((levelProgress - 1) == AppConstant.powerUpCircle) {
            updatePowerUp("plus");
        }


        if (levelCircle < wordList.size() && levelCircle < (AppConstant.levelCircleLimit[levelNum] + numOfJumpPowerUsed)) {

            getlevelQuest(levelCircle);

            getShuffleString(levelQuestString);

            setupKeyboard();


            VisibleInsvisibleOnGameOver();

        } else {
            levelNum++;

            levelCircle = 0;
            numOfJumpPowerUsed = 0;

            saveLevelNum();


            getLevelNum();

            getLevelData();

            getlevelQuest(levelCircle);

            getShuffleString(levelQuestString);

            setupKeyboard();


            VisibleInsvisibleOnGameOver();
        }
    }

    private void cleanUp() {

        correctWordComplement.setVisibility(View.INVISIBLE);

        keyboard.clear();
        keyboard_solve.clear();


    }

    private void saveLevelNum() {
        AppController.getInstance().getPrefManger().setLevelNum(levelNum);
    }


    private String getDrawableFileName(char c) {
        String drawableFileName = "a";

        switch (c) {
            case 'ã':
                drawableFileName = "a2";
                break;
            case 'á':
                drawableFileName = "a1";
                break;
            case 'é':
                drawableFileName = "e1";
                break;

            case 'ô':
                drawableFileName = "o3";
                break;
            case 'í':
                drawableFileName = "i1";
                break;
            case 'ê':
                drawableFileName = "e3";
                break;

            case 'ç':
                drawableFileName = "ce";
                break;
            case 'ó':
                drawableFileName = "o1";
                break;
            case 'õ':
                drawableFileName = "o2";
                break;

            case 'ú':
                drawableFileName = "u1";
                break;
            default:
                drawableFileName = String.valueOf(c);
                break;
        }

        return drawableFileName;

    }

    private void loadAnimation() {
        move_left = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.move_left);

        move_left.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                //FLAG_GAME_RESUME = true;

                correctWordComplement.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        move_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.move_down);
        move_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.move_up);
        move_up.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (dialog != null) {
                    dialog.dismiss();


                    AppController.getInstance().getPrefManger().setLevelNum(1);

                    doOnlyFirstTimeInitialization();

                    getLevelNum();

                    getLevelData();

                    getlevelQuest(levelCircle);

                    getShuffleString(levelQuestString);

                    setupKeyboard();

                    VisibleInsvisibleOnGameOver();


                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void updatePowerUp(String Case) {

        boolean flag = false;

        switch (Case) {
            case "plus":
                flag = true;
                AppController.getInstance().getPrefManger().setPuRemaining(AppController.getInstance().getPrefManger().getPuRemaining() + 1);

                break;
            case "minus":
                flag = true;
                AppController.getInstance().getPrefManger().setPuRemaining(AppController.getInstance().getPrefManger().getPuRemaining() - 1);
                break;
            default:
                break;
        }

        if (AppController.getInstance().getPrefManger().getPuRemaining() <= 0) {
            btn_solve_letter.setBackgroundResource(R.drawable.pu_solve_letter_p);
            btn_jump_level.setBackgroundResource(R.drawable.pu_jump_level_p);
            btn_add_time.setBackgroundResource(R.drawable.pu_add_time_p);
        } else {
            btn_solve_letter.setBackgroundResource(R.drawable.pu_solve_letter_btn_selector);
            btn_jump_level.setBackgroundResource(R.drawable.pu_jump_level_btn_selector);
            btn_add_time.setBackgroundResource(R.drawable.pu_add_time_btn_selector);
        }


        pu_notification.setText(String.valueOf(AppController.getInstance().getPrefManger().getPuRemaining()) + " power available");
        if (flag) YoYo.with(Techniques.Tada)
                .duration(500)
                .playOn(pu_notification);
    }

    public void solveLetter() {


        int random_index = 0;
        Random ran = new Random();
        do {


            random_index = ran.nextInt(levelQuestString.length());

            Log.d("ran", String.valueOf(random_index));

        } while (userWordCharArray[random_index] == levelQuestString.charAt(random_index));


        if (string_track[random_index] == 1) {
            if (userWordCharArray[random_index] != levelQuestString.charAt(random_index)) {
                manageChangeInSolveKeyboard(random_index);


                readyToRevealLetter(levelQuestString.charAt(random_index), random_index);


            }
        } else {
            readyToRevealLetter(levelQuestString.charAt(random_index), random_index);


        }
    }

    private void readyToRevealLetter(char onSolveLetter, int random_index) {

        for (int key_pos = 0; key_pos < shuffleString.length(); key_pos++) {

            if (shuffleString.charAt(key_pos) == onSolveLetter) {
                updateSolveKeyboard(shuffleString.charAt(key_pos),
                        key_pos, keyboard.get(key_pos), random_index);
                break;
            }

        }
    }


    private void jumpLevel() {
        for (int index = 0; index < levelQuestString.length(); index++) {


            if (string_track[index] == 1) {
                if (userWordCharArray[index] != levelQuestString.charAt(index)) {
                    manageChangeInSolveKeyboard(index);


                    readyToRevealLetter(levelQuestString.charAt(index), 0);


                    // break;

                }
            } else {
                readyToRevealLetter(levelQuestString.charAt(index), 0);

                //break;
            }
        }
    }

    private void jumpLevel2() {
        FLAG_GAME_RESUME = false;

        stopMusic();
        /*    stop Timer And CoundDown     */
        if (countdown_timer != null)
            countdown_timer.cancel();
        if (timer != null) {
            timer.cancel();
        }

        donutProgress.setProgress(0);

        VisibleInsvisibleOnGameOver();

        donutProgress.setProgress(0);


        levelCircle++;
        numOfJumpPowerUsed++;


        if (levelCircle < wordList.size()) {

            getlevelQuest(levelCircle);

            getShuffleString(levelQuestString);

            setupKeyboard();


            VisibleInsvisibleOnGameOver();
        } else {
            levelNum++;

            levelCircle = 0;

            saveLevelNum();


            getLevelNum();

            getLevelData();

            getlevelQuest(levelCircle);

            getShuffleString(levelQuestString);

            setupKeyboard();


            VisibleInsvisibleOnGameOver();
        }
    }

    private void addTime() {
        donutProgress.setProgress(0);

        if (timer != null) {
            timer.cancel();
        }

        if (countdown_timer != null) {
            countdown_timer.cancel();
        }

        startCounDownAndTimerForProgessBar(COUNTDOWN_MILI_SEC);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {


        mServ = ((MusicService.ServiceBinder) iBinder).getService();


        if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
            //playMusic("tic_toc");
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

        if (FLAG_GAME_RESUME) {

            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }


            mServ.stop();
            mServ.stop2();
            if (countdown_timer != null) {
                countdown_timer.cancel();
            }
            if (timer != null) {
                timer.cancel();
            }
            doUnbindService();


            super.onBackPressed();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (AppController.getInstance().getPrefManger().getMusicOnOff()) {
            mServ.stop();
            mServ.stop2();
        }

    }


    @Override
    public void onDestroy() {


        doUnbindService();

        super.onDestroy();


    }

    public void doUnbindService() {
        // disconnects the service activity.
        if (mIsBound) {
            unbindService(this);
            mIsBound = false;
        }
    }


    private void dialogGameOver() {

        donutProgress.setProgress(0);

        dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_gameover);

        final RelativeLayout layout = (RelativeLayout) dialog.findViewById(R.id.dialoggameover_layout);

        layout.startAnimation(move_down);

        final MyTextView score_title = (MyTextView) dialog.findViewById(R.id.dialoggameoverscore_title);
        final MyTextView best_score_title = (MyTextView) dialog.findViewById(R.id.dialoggameover_bestscore_title);
        final MyTextView score = (MyTextView) dialog.findViewById(R.id.dialoggameover_score);
        final MyTextView best_score = (MyTextView) dialog.findViewById(R.id.dialoggameover_bestscore);
        final Button btn_play = (Button) dialog.findViewById(R.id.dialoggameoverbtn_playagain);
        final Button btn_menu = (Button) dialog.findViewById(R.id.dialoggameover_btn_menu);
        final ImageView btn_buy = (ImageView) dialog.findViewById(R.id.dialoggameover_buy);
        final ImageView btn_facebook = (ImageView) dialog.findViewById(R.id.dialoggameover_facebook);
        final ImageView btn_twitter = (ImageView) dialog.findViewById(R.id.dialoggameover_twitter);


        String highscore_s[] = AppController.getInstance().getPrefManger().getHighScore().split(",");

        int num_of_solve = Integer.parseInt(highscore_s[0]);

        double needed_time = Double.parseDouble(highscore_s[1]);

        String timestamp = String.valueOf(timeSeconds);
        if (timestamp.length() > 4) {
            timestamp = timestamp.substring(0, 4);
        }

        if ((levelProgress - 1) > num_of_solve) {
            AppController.getInstance().getPrefManger().setHighScoreInt(levelProgress - 1);

            AppController.getInstance().getPrefManger().setHighScore(String.valueOf(levelProgress - 1) + "," + timestamp);
        } else if ((levelProgress - 1) == num_of_solve && timeSeconds < needed_time) {
            AppController.getInstance().getPrefManger().setHighScore(String.valueOf(levelProgress - 1) + "," + timestamp);
        }

       final String highscore_s_2[] = AppController.getInstance().getPrefManger().getHighScore().split(",");

        if (AppController.getInstance().getPrefManger().getLanguage() == 0) {
            score_title.setText(getResources().getString(R.string.gameover_eng_score_title));
            best_score_title.setText(getResources().getString(R.string.gameover_eng_bestscore_title));

            score.setText(String.valueOf(levelProgress - 1) + " in " + timestamp + "s");
            best_score.setText(String.valueOf(highscore_s_2[0]) + " in " + String.valueOf(highscore_s_2[1]) + "s");

            btn_play.setText(getResources().getString(R.string.gameover_eng_btnplay));
            btn_menu.setText(getResources().getString(R.string.gameover_eng_btnmenu));
        } else {
            score_title.setText(getResources().getString(R.string.gameover_por_score_title));
            best_score_title.setText(getResources().getString(R.string.gameover_por_bestscore_title));

            score.setText(String.valueOf(levelProgress - 1) + " em " + timestamp + "s");
            best_score.setText(String.valueOf(highscore_s_2[0]) + " em " + String.valueOf(highscore_s_2[1]) + "s");

            btn_play.setText(getResources().getString(R.string.gameover_por_btnplay));
            btn_menu.setText(getResources().getString(R.string.gameover_por_btnmenu));
        }


        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                layout.startAnimation(move_up);


            }
        });

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.dismiss();
                finish();

            }
        });

        final String timestring1 = timestamp;

        btn_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlToShare = "https://play.google.com/store/apps/details?id="
                        + AppConstant.APP_PNAME;

                String share2 = "Hey i" +
                        " have scored "+String.valueOf(levelProgress - 1) + " in " + timestring1 + "s"+" in Word Unlimited! . Click here to play it." +
                        ""+"&url="+urlToShare;

                String share = "https://www.facebook.com/sharer/sharer.php\n" +
                        "?s=100\n" +
                        "&p[title]=I Made New Score " + String.valueOf(levelProgress - 1) + " in " + timestring1 + "s\n"+
                        "&p[summary]=In Word Unlimited\n" +
                        "&p[url]="+urlToShare;

                //"http://www.facebook.com/sharer.php?s=100&p[title]=titleheresexily&p" +
               //         "[url]=http://www.mysexyurl.com&p[summary]=mysexysummaryhere" +
               //         "&p[images][0]=http://www.urltoyoursexyimage.com"



                boolean facebookAppFound = false;
                Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, share2);

                PackageManager pm = getPackageManager();
                List<ResolveInfo> activityList = pm.queryIntentActivities(shareIntent, 0);
                for (final ResolveInfo app : activityList)
                {
                    if ((app.activityInfo.name).startsWith("com.facebook.katana"))
                    {
                        final ActivityInfo activity = app.activityInfo;
                        final ComponentName name = new ComponentName(activity.applicationInfo.packageName, activity.name);
                        shareIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        shareIntent.setComponent(name);
                         startActivity(shareIntent);
                        facebookAppFound = true;
                        break;
                    }
                }

                if(!facebookAppFound)
                {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    // String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + urlToShare;
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(share));
                    startActivity(intent);
                }

            }
        });




        btn_twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String urlToShare = "https://play.google.com/store/apps/details?id="
                        + AppConstant.APP_PNAME;

                String share2 = "Hey i" +
                        " have scored "+String.valueOf(levelProgress - 1) + " in " + timestring1 + "s"+" in Word Unlimited! . Click here to play it." +
                        ""+"&url="+urlToShare;

                String shareUrl = "https://twitter.com/intent/tweet?text="+"Hey i" +
                        " have scored "+String.valueOf(levelProgress - 1) + " in " + timestring1 + "s"+" in Word Unlimited! . Click here to play it." +
                        ""+"&url="+urlToShare;

                Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                tweetIntent.putExtra(Intent.EXTRA_TEXT, share2);
                tweetIntent.setType("text/plain");

                PackageManager packManager = getPackageManager();
                List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);

                boolean resolved = false;
                for(ResolveInfo resolveInfo: resolvedInfoList){
                    if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")){
                        tweetIntent.setClassName(
                                resolveInfo.activityInfo.packageName,
                                resolveInfo.activityInfo.name );
                        resolved = true;
                        break;
                    }
                }
                if(resolved){
                    startActivity(tweetIntent);
                }else{
                    Intent i = new Intent();
                    i.putExtra(Intent.EXTRA_TEXT, urlToShare);
                    i.setAction(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(shareUrl));
                    startActivity(i);
                    //Toast.makeText(this, "Twitter app isn't found", Toast.LENGTH_LONG).show();
                }



            }
        });


        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                flag_reset = true;

                String payload = "";
                mHelper.launchPurchaseFlow(GamePlay.this, SKU_RESET,
                        REQUEST_PURCHASE, mPurchaseFinishedListener, payload);

            }
        });


        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //dialog = null;

            }
        });

        dialog.show();

    }

    private void VisibleInsvisibleOnGameOver() {
        if (layout_solve_keyboard.getVisibility() == View.VISIBLE) {
            layout_solve_keyboard.setVisibility(View.INVISIBLE);
        } else {
            layout_solve_keyboard.setVisibility(View.VISIBLE);

            YoYo.with(Techniques.FadeIn).duration(600).interpolate(new AccelerateInterpolator()).playOn(layout_solve_keyboard);


        }

        if (layout_keyboard.getVisibility() == View.VISIBLE) {
            layout_keyboard.setVisibility(View.INVISIBLE);
        } else {
            layout_keyboard.setVisibility(View.VISIBLE);

            YoYo.with(Techniques.FadeIn).duration(600).interpolate(new AccelerateInterpolator()).playOn(layout_keyboard);
        }

        if (layout_pu.getVisibility() == View.VISIBLE) {
            layout_pu.setVisibility(View.INVISIBLE);
        } else {
            layout_pu.setVisibility(View.VISIBLE);

            YoYo.with(Techniques.FadeIn).duration(700).interpolate(new AccelerateInterpolator()).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {


                    FLAG_GAME_RESUME = true;

                    startCounDownAndTimerForProgessBar(COUNTDOWN_MILI_SEC);

                    updatePowerUp("both");

                    playMusic("tic_toc");

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).playOn(layout_pu);
        }
        if (pu_notification.getVisibility() == View.VISIBLE) {
            pu_notification.setVisibility(View.INVISIBLE);
        } else {
            pu_notification.setVisibility(View.VISIBLE);

            YoYo.with(Techniques.FadeIn).duration(600).interpolate(new AccelerateInterpolator()).playOn(pu_notification);

        }


        donutProgress.setProgress(0);


    }


    /****************************In App Purchase***********************************/

    /*******************************
     * IN APP PURCHASE
     **************************/


    private void initialAppPurchase() {
        // TODO Auto-generated method stub

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkVj4bX+3OmGCmAh8sxw5aId6v3C2/qcul4bY3s8dWIQ7N23k4DxLQTULe4ZsSSfilfvo/nOfBhIzpo0nfFW9yHxIRc5+K1TnXWQQvItgYuT39IyoEqZ/ECCyxRe/Hm40FHeLzo9bDNW79h0mawxc1go+k8U6hWljtqE/FFHNaCp6R4X6d13F2ama1RXxEavvoGm6pKUy68J7M9QE9WCdbgKy9HyCMiRStoqALf6+1MaITxdDnvK1TCaLPN3RIlOrQNYzriHptSansqgdLbptn2cavP3/PKL/4OkuRs9TO+ZVIL+OLkm65UtxI9I5ddoe5vlqCdbRMaDjULXrLtJuXQIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");

                    mHelper.queryInventoryAsync(mReceivedInventoryListener);
                }
            }
        });

    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null)
                return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

			/*
             * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

            Purchase resetPurchase = inventory.getPurchase(SKU_RESET);
            if (resetPurchase != null && verifyDeveloperPayload(resetPurchase)) {
                Log.d(TAG, "We have gas. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_RESET),
                        mConsumeFinishedListener);
                // return;
            }
        }
        // updateUi();
        // setWaitScreen(false);
        // Log.d(TAG, "Initial inventory query finished; enabling main UI.");
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isSuccess()) {

                Log.d(TAG, "in M consume");

                // clickButton.setEnabled(true);

                if (purchase.getSku().equals(SKU_RESET) && flag_reset) {
                    AppController.getInstance().getPrefManger().setPuRemaining(AppController.getInstance().getPrefManger().getPuRemaining() + 60);

                    // progressbar_inDialog.setVisibility(View.GONE);

                    //setEnableOrDisable();

                }

                flag_reset = false;

            } else {
                // progressbar_inDialog.setVisibility(View.GONE);
                complain("Error while consuming: " + result);
            }
            // updateUi();
            // setWaitScreen(false);
            // Log.d(TAG, "End consumption flow.");
        }
    };

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        //alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

		/*
         * TODO: verify that the developer payload of the purchase is correct.
		 * It will be the same one that you sent when initiating the purchase.
		 *
		 * WARNING: Locally generating a random string when starting a purchase
		 * and verifying it here might seem like a good approach, but this will
		 * fail in the case where the user purchases an item on one device and
		 * then uses your app on a different device, because on the other device
		 * you will not have access to the random string you originally
		 * generated.
		 *
		 * So a good developer payload has these characteristics:
		 *
		 * 1. If two different users purchase an item, the payload is different
		 * between them, so that one user's purchase can't be replayed to
		 * another user.
		 *
		 * 2. The payload must be such that you can verify it even when the app
		 * wasn't the one who initiated the purchase flow (so that items
		 * purchased by the user on one device work on other devices owned by
		 * the user).
		 *
		 * Using your own server to store and verify developer payloads across
		 * app installations is recommended.
		 */

        return true;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: "
                    + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                //progressbar_inDialog.setVisibility(View.GONE);
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                //progressbar_inDialog.setVisibility(View.GONE);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            // buyButton.setEnabled(false);

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...

        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }

    }


    public void buy() {
        flag_reset = true;

        String payload = "";
        mHelper.launchPurchaseFlow(GamePlay.this, SKU_RESET,
                REQUEST_PURCHASE, mPurchaseFinishedListener, payload);
    }


}


