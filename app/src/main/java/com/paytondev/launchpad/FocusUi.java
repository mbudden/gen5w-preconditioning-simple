package com.paytondev.launchpad;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Native view styling. */
final class FocusUi {
    final Context context;
    final Palette p;

    FocusUi(Context context, Palette palette) { this.context = context; this.p = palette; }
    int dp(float value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
    LinearLayout column() { return linear(LinearLayout.VERTICAL); }
    LinearLayout row() { return linear(LinearLayout.HORIZONTAL); }
    private LinearLayout linear(int orientation) {
        LinearLayout v = new LinearLayout(context); v.setOrientation(orientation); return v;
    }
    TextView text(String value, int size, int color, boolean bold) {
        TextView v = new TextView(context);
        v.setText(value); v.setTextSize(size); v.setTextColor(color);
        v.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        v.setIncludeFontPadding(false);
        return v;
    }
    GradientDrawable rounded(int color, int radius, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color); shape.setCornerRadius(dp(radius));
        if (stroke != 0) shape.setStroke(dp(stroke == p.accent ? 2 : 1), stroke);
        return shape;
    }
    /** Solid fills of any color; the knob draws a blue ring outside the fill. */
    Drawable pill(int fill, int radius) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, ring(fill & 0xccffffff, radius));
        states.addState(new int[]{android.R.attr.state_activated}, ring(fill, radius));
        states.addState(new int[]{}, rounded(fill, radius, 0));
        return states;
    }
    private Drawable ring(int fill, int radius) {
        LayerDrawable ring = new LayerDrawable(new Drawable[]{rounded(p.background, radius, p.accent),
            rounded(fill, radius - 4, 0)});
        ring.setLayerInset(1, dp(4), dp(4), dp(4), dp(4));
        return ring;
    }
    @SuppressWarnings("deprecation")
    void background(View view, Drawable drawable) { view.setBackgroundDrawable(drawable); }
}
