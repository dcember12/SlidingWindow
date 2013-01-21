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
package net.callmeike.android.widget.slidingmenu;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ListView;


/**
 * SlidingMenu
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
public class SlidingMenu extends ListView {
    private static final String TAG = "SLIDER";


    private final int ANIMATION_MILLIS;
    private final PointF DISPLACEMENT = new PointF();

    private final Rect sliderMargins = new Rect();

    private View menuRoot;
    private View slidingView;
    private ViewGroup rootView;

    private boolean animating;
    private boolean visible;

    /**
     * Ctor: from super
     *
     * @param context
     */
    public SlidingMenu(Context context) {
        this(context, null);
    }

    /**
     * Ctor: from super
     *
     * @param context
     * @param attrs
     */
    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Ctor: from super
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int animationMillis = 500;
        float deltaX = 0.75f;
        float deltaY = 0.75f;

        if (null != attrs) {
            TypedArray atts
                = context.obtainStyledAttributes(attrs, R.styleable.sliding_menu, defStyle, 0);
            final int n = atts.getIndexCount();
            for (int i = 0; i < n; i++) {
                try {
                    int attr = atts.getIndex(i);
                    switch (attr) {
                        case R.styleable.sliding_menu_sliding_menu_animation_millis:
                            animationMillis = atts.getDimensionPixelSize(attr, animationMillis);
                            break;

                        case R.styleable.sliding_menu_sliding_menu_x_displacement:
                            deltaX = atts.getFloat(attr, deltaX);
                            break;

                        case R.styleable.sliding_menu_sliding_menu_y_displacement:
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

        ANIMATION_MILLIS = animationMillis;
        DISPLACEMENT.x = deltaX;
        DISPLACEMENT.y = deltaY;
    }

    /**
     * @param root a parent of this view that represents the menu: allows for titles and such
     * @param slider the view that will be moved aside, to make room for the menu
     */
    public void setContainerView(View root, View slider) {
        this.menuRoot = root;
        this.slidingView = slider;
        this.rootView = (ViewGroup) slidingView.getParent();

        ViewGroup.MarginLayoutParams lp
            = (ViewGroup.MarginLayoutParams) slidingView.getLayoutParams();
        sliderMargins.set(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
    }

    /**
     * @return boolean true if menu is visible
     */
    public boolean getVisible() { return visible; }

    /**
     * @param vis true == visible
     */
    public void setVisible(boolean vis) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "visible: " + vis + "(" + visible + ")"); }
        if (null == slidingView) {
            throw new IllegalStateException(
                "slidingMenu.setVisible called before setContainerView");
        }

        if ((vis == visible) || (animating)) { return; }

        if (vis) { show(); }
        else { hide(); }
    }

    /**
     * forcibly remove the menu: in onPause(), e.g.,
     */
    public void reset() {
         if (animating) { visible = false; }
         else if (visible) { removeMenu(); }
    }

    void onShowComplete() {
        animating = false;
        if (!visible) { removeMenu(); }
        else { setEnabled(true); }
    }

    // !!! There's a disturbing flash at the end of the animation...
    void onHideComplete() {
        animating = false;
        removeMenu();
    }

    private void hide() {
        Log.d(TAG, "hide");

        Point delta = getDisplacement();
        animate(
            new TranslateAnimation(0, -delta.x, 0, -delta.y),
            new Animation.AnimationListener() {
                @Override public void onAnimationEnd(Animation a) { onHideComplete(); }
                @Override public void onAnimationRepeat(Animation a) { }
                @Override public void onAnimationStart(Animation a) { }
            });
    }

    private void show() {
        Log.d(TAG, "show");

        // this is a great place to make the slider opaque
        Point delta = getDisplacement();
        moveSlider(delta.x, delta.y);
        addMenu();

        animate(
            new TranslateAnimation(-delta.x, 0, -delta.y, 0),
            new Animation.AnimationListener() {
                @Override public void onAnimationEnd(Animation a) { onShowComplete(); }
                @Override public void onAnimationRepeat(Animation a) { }
                @Override public void onAnimationStart(Animation a) { }
            });
    }

    private Point getDisplacement() {
        return new Point(
            Math.round((rootView.getRight() - rootView.getLeft()) * DISPLACEMENT.x),
            Math.round((rootView.getBottom() - rootView.getTop()) * DISPLACEMENT.y));
    }

    private void moveSlider(int deltaX, int deltaY) {
        Log.d(TAG, "move: " + deltaX + ", " + deltaY);
        ViewGroup.MarginLayoutParams lp
            = (ViewGroup.MarginLayoutParams) slidingView.getLayoutParams();
        lp.setMargins(
            sliderMargins.left + deltaX,
            sliderMargins.top + deltaY,
            sliderMargins.right - deltaX,
            sliderMargins.bottom - deltaY);
        slidingView.setLayoutParams(lp);
    }

    private void addMenu() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.LEFT);

        params.setMargins(
            sliderMargins.left,
            sliderMargins.top,
            sliderMargins.right,
            sliderMargins.bottom);
        setLayoutParams(params);

        rootView.addView(menuRoot);
        rootView.bringChildToFront(slidingView);

        visible = true;
    }

    private void removeMenu() {
        rootView.removeView(menuRoot);
        moveSlider(0, 0);
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
