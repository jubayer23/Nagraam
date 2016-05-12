package com.fli_alpha_solutions_ltd.word_unlimited.Utils;

import com.fli_alpha_solutions_ltd.word_unlimited.appdata.AppConstant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/*
 * This could be implemented with a SQLite db instead
 * but allow it.
 */
public class langDict {

    private  Set<String> wordSet;

    Set<String> wordSet_length_3;
    Set<String> wordSet_length_4;
    Set<String> wordSet_length_5;
    Set<String> wordSet_length_6;


    public langDict() {
        wordSet = new TreeSet<String>();
        wordSet_length_3 =  new TreeSet<String>();
        wordSet_length_4 =  new TreeSet<String>();
        wordSet_length_5 =  new TreeSet<String>();
        wordSet_length_6 =  new TreeSet<String>();

    }

    public boolean readDict(InputStream I) {
        wordSet.clear();
        wordSet_length_3.clear();
        wordSet_length_4.clear();
        wordSet_length_5.clear();
        wordSet_length_6.clear();

        Reader iR = new InputStreamReader(I, Charset.forName("ISO-8859-1"));
        BufferedReader R = new BufferedReader(iR);
        String line;

        try {
            while ((line = R.readLine()) != null) {



                String s[] = line.split(",");

                if(s.length>1)
                {
                    ArrayList<String> temp = new ArrayList<String>();
                    for(int i=0;i<s.length;i++)
                    {
                        temp.add(s[i]);
                    }
                    AppConstant.similarWordMap.put(s[0],temp);
                }


                wordSet.add(line);

                if (s[0].length() == 3) {
                    wordSet_length_3.add(s[0]);
                } else if (s[0].length() == 4) {
                    wordSet_length_4.add(s[0]);
                } else if (s[0].length() == 5) {
                    wordSet_length_5.add(s[0]);
                } else if (s[0].length() == 6) {
                    wordSet_length_6.add(s[0]);
                }
                AppConstant.map_word_is_correct.put(s[0],1);

            }

            AppConstant.word_length_3.clear();
            AppConstant.word_length_3.addAll(wordSet_length_3);
            AppConstant.word_length_4.clear();
            AppConstant.word_length_4.addAll(wordSet_length_4);
            AppConstant.word_length_5.clear();
            AppConstant.word_length_5.addAll(wordSet_length_5);
            AppConstant.word_length_6.clear();
            AppConstant.word_length_6.addAll(wordSet_length_6);
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    public boolean isWord(String s) {
        return wordSet.contains(s.toLowerCase());
    }

    public Set<String> getWordList() {
        return wordSet;
    }
}