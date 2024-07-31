package edu.northeastern.numad24su_group9;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Pattern;

public class CurrencyInputFilter implements InputFilter {
    private final Pattern mPattern;

    public CurrencyInputFilter() {
        mPattern = Pattern.compile("^[0-9]{1,7}+(\\.[0-9]{0,2})?$");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String result = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend);
        if (!mPattern.matcher(result).matches()) {
            return "";
        }
        return null;
    }
}
