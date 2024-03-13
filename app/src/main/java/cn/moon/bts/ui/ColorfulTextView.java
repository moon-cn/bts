package cn.moon.bts.ui;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.List;

public class ColorfulTextView extends AppCompatTextView {
    private List<String> mTextLines;
    private int[] mTextColors;

    public ColorfulTextView(Context context) {
        super(context);
    }

    public ColorfulTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorfulTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setTextLines(List<String> textLines) {
        mTextLines = textLines;
        mTextColors = new int[textLines.size()];
        updateText();
    }

    public void setTextColors(int lineIndex, int color) {
        if(lineIndex >=0){
            mTextColors[lineIndex] = color;
            updateText();
        }

    }

    private void updateText() {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < mTextLines.size(); i++) {
            String line = mTextLines.get(i);
            int start = builder.length();
            int end = start + line.length();

            builder.append(line);
            builder.append("\n");


            int color = mTextColors[i];
            if(color != 0){
                builder.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            }
        }

        setText(builder);
    }
}