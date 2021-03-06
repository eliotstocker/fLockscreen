package com.piratemedia.lockscreen;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class RotateView extends View {
    private TextPaint mTextPaint;
    private String mText;
    private int mAscent;
    private Rect text_bounds = new Rect();
    private int rotateVal;

    final static int DEFAULT_TEXT_SIZE = 15;

    public RotateView(Context context) {
        super(context);
        initLabelView();
    }

    public RotateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLabelView();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RotateView);

        CharSequence s = a.getString(R.styleable.RotateView_text);
        if (s != null) setText(s.toString());

        setTextColor(a.getColor(R.styleable.RotateView_textColor, 0xFF000000));

        int textSize = a.getDimensionPixelOffset(R.styleable.RotateView_textSize, 0);
        if (textSize > 0) setTextSize(textSize);

        rotateVal = a.getInt(R.styleable.RotateView_rotate, 90);
        
        a.recycle();
    }

    private final void initLabelView() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE);
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextAlign(Align.CENTER);
        setPadding(0, 0, 0, 0);
    }

    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }

    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mTextPaint.getTextBounds(mText, 0, mText.length(), text_bounds);
        setMeasuredDimension(
                measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = text_bounds.height() + getPaddingLeft() + getPaddingRight();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = text_bounds.width() + getPaddingTop() + getPaddingBottom();

            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float text_horizontally_centered_origin_x = getPaddingLeft() + text_bounds.width()/2f;
        float text_horizontally_centered_origin_y = getPaddingTop() - mAscent - getPaddingBottom();

        canvas.translate(text_horizontally_centered_origin_y, text_horizontally_centered_origin_x);
        canvas.rotate(rotateVal);
        canvas.drawText(mText, 0, 0, mTextPaint);
    }
}