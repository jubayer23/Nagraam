package com.fli_alpha_solutions_ltd.word_unlimited.appdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by comsol on 10/27/2015.
 */

public class AppConstant {


    public static final int LANGUAGE_ENG = 0;

    public static final int LANGUAGE_POR = 1;

    public static int LANGUAGE = LANGUAGE_POR;


    public static List<String> word_length_3 = new ArrayList<String>();

    public static HashMap<String,Integer> map_word_is_correct= new HashMap<String,Integer>();

    public static List<String> word_length_4 = new ArrayList<String>();

    public static List<String> word_length_5 = new ArrayList<String>();


    public static List<String> word_length_6 = new ArrayList<String>();


    public static HashMap<String, ArrayList<String>> similarWordMap = new HashMap<String,  ArrayList<String>>();

    public static int GAMEOVER_COUNTER = 0;

    public final static String APP_PNAME = "com.fli_alpha_solutions_ltd.word_unlimited";// Package


    public static int[] levelCircleLimit =
            {
                    0,
                    5,
                    10,
                    15,
                    100
            };

    public static String[] complementList_eng =
            {
                    "correct",
                    "easy",
                    "good",
                    "is_this",
                    "nice",
                    "right"
            };

    public static String[][] complementList =
            {
                    {
                            "correct",
                            "easy",
                            "good",
                            "is_this",
                            "nice",
                            "right"
                    },
                    {
                            "correto",
                            "boa",
                            "facil",
                            "incrivel",
                            "isso_ai",
                            "otimo"
                    }
            };


    public static String[] wordFileName =
            {
                    "word_english",
                    "word_por"
            };

    public   static int powerUpCircle = 10;

    public  static final int powerUpLimitation = 3;


}
