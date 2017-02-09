package faskteam.faskandroid.utilities.progress_spinner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import faskteam.faskandroid.R;
import faskteam.faskandroid.utilities.CommonFunctions;

/**
 * Simplest custom view possible, using CircularProgressDrawable
 */
public class ProgressSpinnerView extends View {

    private CircularProgressDrawable mDrawable;

    private static final int STROKE_WIDTH = 6;

    public ProgressSpinnerView(Context context) {
        this(context, null);
    }

    public ProgressSpinnerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressSpinnerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int strokeWidth = CommonFunctions.scaleToDisplay(context, STROKE_WIDTH);
        mDrawable = new CircularProgressDrawable(ContextCompat.getColor(context, R.color.color_accent), strokeWidth);
        mDrawable.setCallback(this);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            mDrawable.start();
        } else {
            mDrawable.stop();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, w, h);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        mDrawable.draw(canvas);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mDrawable || super.verifyDrawable(who);
    }
}