/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package net.callmeike.android.widget.slidingwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import net.callmeike.android.widget.slidingwindow.BuildConfig;


/**
 * SlidingWindow
 *
 * @version $Revision: $
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 *
 * When I use this, I frequently see the error:
 *     10-06 18:11:45.746: E/AndroidRuntime(701): java.lang.RuntimeException:
 *     Unable to instantiate application android.app.Application: java.lang.NullPointerException
 * Word has it that this is an Eclipse problem.  It is being addressed here:
 *     http://code.google.com/p/android/issues/detail?id=25869
 */
public class SlidingWindow extends FrameLayout {
    private static final String TAG = "SLIDER";


    private final int ANIMATION_MILLIS;
    private final PointF DISPLACEMENT = new PointF();

    private View slidingView;
    private ViewGroup rootView;

    private boolean animating;
    private boolean visible;

    /**
     * Ctor: from super
     *
     * @param context
     */
    public SlidingWindow(Context context) {
        this(context, null);
    }

    /**
     * Ctor: from super
     *
     * @param context
     * @param attrs
     */
    public SlidingWindow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Ctor: from super
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SlidingWindow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int animationMillis = 500;
        float deltaX = 0.75f;
        float deltaY = 0.75f;

        if (null != attrs) {
            TypedArray atts
                = context.obtainStyledAttributes(attrs, R.styleable.sliding_window, defStyle, 0);
            try {
                final int n = atts.getIndexCount();
                for (int i = 0; i < n; i++) {
                    try {
                        int attr = atts.getIndex(i);
                        switch (attr) {
                            case R.styleable.sliding_window_sliding_window_animation_millis:
                                animationMillis = atts.getInt(attr, animationMillis);
                                break;

                            case R.styleable.sliding_window_sliding_window_x_displacement:
                                deltaX = atts.getFloat(attr, deltaX);
                                break;

                            case R.styleable.sliding_window_sliding_window_y_displacement:
                                deltaY = atts.getFloat(attr, deltaY);
                                break;
                        }
                    }
                    catch (UnsupportedOperationException e) {
                        Log.w(TAG, "Failed parsing attribute: " + atts.getString(i), e);
                    }
                    catch (Resources.NotFoundException e) {
                        Log.w(TAG, "Failed parsing attribute: " + atts.getString(i), e);
                    }
                }
            }
            finally {
                if (null != atts) { atts.recycle(); }
            }
        }

        ANIMATION_MILLIS = animationMillis;
        DISPLACEMENT.x = deltaX;
        DISPLACEMENT.y = deltaY;
    }

    /**
     * @param slider the view that will be moved aside, to make room for this one
     */
    public void setContainerView(View slider) {
        this.slidingView = slider;
        this.rootView = (ViewGroup) slidingView.getParent();

        ViewGroup.MarginLayoutParams lp
            = (ViewGroup.MarginLayoutParams) slidingView.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);

        rootView.addView(this);

        setVisibility(View.GONE);
    }

    /**
     * @return boolean true if this slider is visible
     */
    public boolean getVisible() { return visible; }

    /**
     * @param vis true == visible
     */
    public void setVisible(boolean vis) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "visible: " + vis + "(" + visible + ")"); }
        if (null == slidingView) {
            throw new IllegalStateException(
                "slidingWindow.setVisible called before setContainerView");
        }

        if ((vis == visible) || (animating)) { return; }

        if (vis) { show(); }
        else { hide(); }
    }

    /**
     * forcibly remove this slider: in onPause(), e.g.,
     */
    public void reset() {
         if (animating) { visible = false; }
         else if (visible) { hideLowerView(); }
    }

    void onShowComplete(Point delta) {
        animating = false;
        if (!visible) { hideLowerView(); }
        else { moveSlider(delta.x, delta.y); }
    }

    void onHideComplete() {
        animating = false;
        moveSlider(0, 0);
        hideLowerView();
    }

    private void hide() {
        Log.d(TAG, "hide");

        Point delta = getDisplacement();
        moveSlider(0, 0);

        animate(
            new TranslateAnimation(delta.x, 0, delta.y, 0),
            new Animation.AnimationListener() {
                @Override public void onAnimationEnd(Animation a) { onHideComplete(); }
                @Override public void onAnimationRepeat(Animation a) { }
                @Override public void onAnimationStart(Animation a) { }
            });
    }

    private void show() {
        Log.d(TAG, "show");

        // this is a great place to make the slider opaque

        showLowerView();

        final Point delta = getDisplacement();
        moveSlider(delta.x, delta.y);

        animate(
            new TranslateAnimation(-delta.x, 0, -delta.y, 0),
            new Animation.AnimationListener() {
                @Override public void onAnimationEnd(Animation a) { onShowComplete(delta); }
                @Override public void onAnimationRepeat(Animation a) { }
                @Override public void onAnimationStart(Animation a) { }
            });
    }

    private Point getDisplacement() {
        return new Point(
            Math.round((rootView.getRight() - rootView.getLeft()) * DISPLACEMENT.x),
            Math.round((rootView.getBottom() - rootView.getTop()) * DISPLACEMENT.y));
    }

    @SuppressLint("NewApi")
    private void moveSlider(int deltaX, int deltaY) {
        Log.d(TAG, "move: " + deltaX + ", " + deltaY);
        slidingView.setTranslationX(deltaX);
        slidingView.setTranslationY(deltaY);
    }

    private void showLowerView() {
        setVisibility(View.VISIBLE);
        rootView.bringChildToFront(slidingView);
        visible = true;
    }

    private void hideLowerView() {
        setVisibility(View.GONE);
        // this is a great place to restore the original slider color
        visible = false;
    }

    private void animate(
        TranslateAnimation animation,
        Animation.AnimationListener animationListener)
    {
        animating = true;
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setAnimationListener(animationListener);
        animation.setDuration(ANIMATION_MILLIS);
        slidingView.startAnimation(animation);
    }
}
