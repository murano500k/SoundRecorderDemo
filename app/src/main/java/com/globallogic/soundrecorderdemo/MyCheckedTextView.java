package com.globallogic.soundrecorderdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class MyCheckedTextView extends android.support.v7.widget.AppCompatCheckedTextView {

    Context context;
    Drawable srcEnabled;
    Drawable srcDisabled;
    int drawablePosition;
    boolean isEnabled;

    public MyCheckedTextView(Context context) {
        super(context);
        this.context=context;
    }

    public MyCheckedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MyCheckedTextView);

        try {
            drawablePosition = a
                    .getInteger(R.styleable.MyCheckedTextView_drawable_position, 1);
            isEnabled = a.
                    getBoolean(R.styleable.MyCheckedTextView_is_enabled, true);
            srcEnabled = a
                    .getDrawable(R.styleable.MyCheckedTextView_src_enabled);
            srcDisabled = a
                    .getDrawable(R.styleable.MyCheckedTextView_src_disabled);


        } catch (Exception e) {

        } finally {
            a.recycle();
        }
        if(isEnabled) setChecked(true);
        else setChecked(false);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        if(checked){
            setBackground(getContext().getDrawable(R.drawable.bottomborder_checked));
            setTextColor(getContext().getColor(R.color.colorAccent));
            if(drawablePosition==0){
                setCompoundDrawablesWithIntrinsicBounds(srcEnabled,null, null,null);
            }else {
                setCompoundDrawablesWithIntrinsicBounds(null,null, srcEnabled,null);
            }
        }else {
            setBackground(getContext().getDrawable(R.drawable.bottomborder_unchecked));
            setTextColor(getContext().getColor(R.color.colorPrimary));
            if(drawablePosition==0){
                setCompoundDrawablesWithIntrinsicBounds(srcDisabled,null, null,null);
            }else {
                setCompoundDrawablesWithIntrinsicBounds(null,null, srcDisabled,null);
            }
        }
    }
}
