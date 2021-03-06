package com.mediatek.contacts.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.contacts.R;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

//TODO: the view add for landscape 
public class DialpadAdditionalButtonsLand extends FrameLayout {

    private static final String TAG = "DialpadAdditionalButtonsLand";

    private int mButtonWidth;
    private int mButtonHeight;
    private int mDividerHeight;
    private int mDividerWidth;

    private Drawable mDividerVertical;

    private boolean mLayouted = false;

    public DialpadAdditionalButtonsLand(Context context, AttributeSet attrs) {
        super(context, attrs);

        Resources r = getResources();
		// GBPYL-3 chenbo modify 20130513 (start)
		boolean isLand = r.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int w = dm.widthPixels;
		int h = dm.heightPixels;
		if (w > h) {
			int m = w;
			w = h;
			h = m;
		}
		mButtonWidth = isLand ? h * 5 / 27 : w / 3;
        //mButtonWidth = r.getDimensionPixelSize(R.dimen.dialpad_additional_button_width);
		// GBPYL-3 chenbo modify 20130513 (end)
        mButtonHeight = r.getDimensionPixelSize(R.dimen.dialpad_additional_button_height);
        mDividerHeight = r.getDimensionPixelSize(R.dimen.dialpad_divider_height);
        mDividerWidth = r.getDimensionPixelSize(R.dimen.dialpad_divider_width);
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();

        init();
    }

    protected void init() {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(new int[] {
            android.R.attr.selectableItemBackground
        });
        Drawable itemBackground = typedArray.getDrawable(0);

        typedArray = getContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.dividerVertical});
        mDividerVertical = typedArray.getDrawable(0);

        ImageButton button = new ImageButton(getContext());
        button.setImageResource(R.drawable.ic_dialpad_holo_dark);
        button.setBackgroundDrawable(itemBackground);
        button.setId(R.id.dialpadButton);
        
        button.setVisibility(View.INVISIBLE);
       
        addView(button);

        View divider = new View(getContext());
        divider.setBackgroundDrawable(mDividerVertical);
        divider.setVisibility(View.INVISIBLE);
        addView(divider);

        button = new ImageButton(getContext());
        button.setImageResource(R.drawable.ic_dial_action_call);
        button.setBackgroundResource(R.drawable.btn_call);
        button.setId(R.id.dialButton);
        addView(button);

        divider = new View(getContext());
        divider.setBackgroundDrawable(mDividerVertical);
        divider.setVisibility(View.INVISIBLE);
        addView(divider);

        button = new ImageButton(getContext());
        button.setBackgroundDrawable(itemBackground.getConstantState().newDrawable());
        int id = R.id.overflow_menu;
        int resId = R.drawable.ic_menu_overflow;
        if (ViewConfiguration.get(getContext()).hasPermanentMenuKey()) {
            if (FeatureOption.MTK_VT3G324M_SUPPORT) {
                id = R.id.videoDialButton;
                resId = R.drawable.ic_dial_action_video_call;
            } else {
                id = R.id.addToContact;
                resId = R.drawable.ic_add_contact_holo_dark;
            }
        }
        button.setId(id);
        button.setImageResource(resId);
        
        button.setVisibility(View.INVISIBLE);
        
        addView(button);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mLayouted) {
            return;
        }
        mLayouted = true;

        View child = getChildAt(0);
        child.layout(0, 0, mButtonWidth, mButtonHeight);

        int dividerTop = (mButtonHeight - mDividerHeight) >> 1;
        child = getChildAt(1);
        child.layout(mButtonWidth - mDividerHeight, dividerTop, mButtonWidth + mDividerWidth
                - mDividerHeight, dividerTop + mDividerHeight);

        child = getChildAt(2);
        child.layout(mButtonWidth - mDividerHeight, 0, mButtonWidth << 1, mButtonHeight);

        child = getChildAt(3);
        child.layout(mButtonWidth << 1, dividerTop, (mButtonWidth << 1) + mDividerWidth, dividerTop + mDividerHeight);

        child = getChildAt(4);
        child.layout(mButtonWidth << 1, 0, (mButtonWidth << 1) + mButtonWidth, mButtonHeight);
    }
}
