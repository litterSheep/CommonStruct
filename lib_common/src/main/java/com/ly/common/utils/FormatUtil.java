package com.ly.common.utils;

import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.orhanobut.logger.Logger;

import java.text.DecimalFormat;

/**
 * Created by ly on 2017/5/10 15:24.
 */

public class FormatUtil {

    /**
     * 保留2位小数点
     *
     * @param decimal
     * @return
     */
    public static String formatDecimal(float decimal) {
        DecimalFormat df = new DecimalFormat("#,###.##");
        return df.format(decimal);
    }

    public static String formatScience(int number) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(number);
    }

    public static int strToInt(String str) {
        int i = 0;
        try {
            i = Integer.valueOf(str);
        } catch (Exception e) {
            Logger.e(e.toString());
        }
        return i;
    }

    public static float strToFloat(String str) {
        float f = 0;
        try {
            f = Float.valueOf(str);
        } catch (Exception e) {
            Logger.e(e.toString());
        }
        return f;
    }

    /**
     * 余额转换  如 "2,000.05" --> 2000.05
     * Created by ly on 2017/5/10 15:36
     */
    public static float StringToFloat(String key) {
        float result = 0;
        if (key != null && key.length() > 0) {
            if (key.contains(","))
                key = key.replace(",", "");
            result = Float.parseFloat(key);
        }
        return result;
    }

    public static String insertSpace2Phone(String phone) {
        if (TextUtils.isEmpty(phone) || phone.length() < 11)
            return phone;
        String a = phone.substring(0, 3);
        String b = phone.substring(3, 7);
        String c = phone.substring(7);
        return a + " " + b + " " + c;
    }

    public static String handlerCardNumber(String card) {
        return card.substring(0, 6) + "********" + card.substring(14);
    }


    public static void main(String arg[]) {

        System.out.print(formatDecimal(StringToFloat("2,000.05")));
    }

    public static void bankCardNumAddSpace(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            int beforeTextLength = 0;
            int onTextLength = 0;
            boolean isChanged = false;

            int location = 0;//记录光标的位置
            int konggeNumberB = 0;
            private char[] tempChar;
            private StringBuffer buffer = new StringBuffer();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeTextLength = s.length();
                if (buffer.length() > 0) {
                    buffer.delete(0, buffer.length());
                }
                konggeNumberB = 0;
                for (int i = 0; i < s.length(); i++) {
                    if (s.charAt(i) == ' ') {
                        konggeNumberB++;
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onTextLength = s.length();
                buffer.append(s.toString());
                if (onTextLength == beforeTextLength || onTextLength <= 3 || isChanged) {
                    isChanged = false;
                    return;
                }
                isChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isChanged) {
                    location = editText.getSelectionEnd();
                    int index = 0;
                    while (index < buffer.length()) {
                        if (buffer.charAt(index) == ' ') {
                            buffer.deleteCharAt(index);
                        } else {
                            index++;
                        }
                    }

                    index = 0;
                    int konggeNumberC = 0;
                    while (index < buffer.length()) {
                        //银行卡号的话需要改这里
                        if ((index == 4 || index == 9 || index == 14 || index == 19 || index == 24 || index == 29 || index == 34)) {
                            buffer.insert(index, ' ');
                            konggeNumberC++;
                        }
                        index++;
                    }

                    if (konggeNumberC > konggeNumberB) {
                        location += (konggeNumberC - konggeNumberB);
                    }

                    tempChar = new char[buffer.length()];
                    buffer.getChars(0, buffer.length(), tempChar, 0);
                    String str = buffer.toString();
                    if (location > str.length()) {
                        location = str.length();
                    } else if (location < 0) {
                        location = 0;
                    }

                    editText.setText(str);
                    Editable etable = editText.getText();
                    Selection.setSelection(etable, location);
                    isChanged = false;
                }
            }
        });
    }

}
