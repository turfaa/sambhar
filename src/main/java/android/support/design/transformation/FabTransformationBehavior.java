package android.support.design.transformation;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.R;
import android.support.design.animation.AnimationUtils;
import android.support.design.animation.AnimatorSetCompat;
import android.support.design.animation.ArgbEvaluatorCompat;
import android.support.design.animation.ChildrenAlphaProperty;
import android.support.design.animation.DrawableAlphaProperty;
import android.support.design.animation.MotionSpec;
import android.support.design.animation.MotionTiming;
import android.support.design.animation.Positioning;
import android.support.design.circularreveal.CircularRevealCompat;
import android.support.design.circularreveal.CircularRevealHelper;
import android.support.design.circularreveal.CircularRevealWidget;
import android.support.design.circularreveal.CircularRevealWidget.CircularRevealScrimColorProperty;
import android.support.design.circularreveal.CircularRevealWidget.RevealInfo;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.LayoutParams;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.MathUtils;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

public abstract class FabTransformationBehavior extends ExpandableTransformationBehavior {
    private final int[] tmpArray;
    private final Rect tmpRect;
    private final RectF tmpRectF1;
    private final RectF tmpRectF2;

    protected static class FabTransformationSpec {
        public Positioning positioning;
        public MotionSpec timings;

        protected FabTransformationSpec() {
        }
    }

    public abstract FabTransformationSpec onCreateMotionSpec(Context context, boolean z);

    public FabTransformationBehavior() {
        this.tmpRect = new Rect();
        this.tmpRectF1 = new RectF();
        this.tmpRectF2 = new RectF();
        this.tmpArray = new int[2];
    }

    public FabTransformationBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.tmpRect = new Rect();
        this.tmpRectF1 = new RectF();
        this.tmpRectF2 = new RectF();
        this.tmpArray = new int[2];
    }

    @CallSuper
    public boolean layoutDependsOn(CoordinatorLayout coordinatorLayout, View view, View view2) {
        if (view.getVisibility() != 8) {
            boolean z = false;
            if (!(view2 instanceof FloatingActionButton)) {
                return false;
            }
            int expandedComponentIdHint = ((FloatingActionButton) view2).getExpandedComponentIdHint();
            if (expandedComponentIdHint == 0 || expandedComponentIdHint == view.getId()) {
                z = true;
            }
            return z;
        }
        throw new IllegalStateException("This behavior cannot be attached to a GONE view. Set the view to INVISIBLE instead.");
    }

    @CallSuper
    public void onAttachedToLayoutParams(@NonNull LayoutParams layoutParams) {
        if (layoutParams.dodgeInsetEdges == 0) {
            layoutParams.dodgeInsetEdges = 80;
        }
    }

    /* Access modifiers changed, original: protected */
    @NonNull
    public AnimatorSet onCreateExpandedStateChangeAnimation(View view, View view2, boolean z, boolean z2) {
        final boolean z3 = z;
        FabTransformationSpec onCreateMotionSpec = onCreateMotionSpec(view2.getContext(), z3);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        if (VERSION.SDK_INT >= 21) {
            createElevationAnimation(view, view2, z, z2, onCreateMotionSpec, arrayList, arrayList2);
        }
        RectF rectF = this.tmpRectF1;
        View view3 = view;
        View view4 = view2;
        boolean z4 = z;
        boolean z5 = z2;
        FabTransformationSpec fabTransformationSpec = onCreateMotionSpec;
        ArrayList arrayList3 = arrayList;
        ArrayList arrayList4 = arrayList2;
        createTranslationAnimation(view3, view4, z4, z5, fabTransformationSpec, arrayList3, arrayList4, rectF);
        float width = rectF.width();
        float height = rectF.height();
        createIconFadeAnimation(view3, view4, z4, z5, fabTransformationSpec, arrayList3, arrayList4);
        createExpansionAnimation(view3, view4, z4, z5, fabTransformationSpec, width, height, arrayList, arrayList2);
        arrayList3 = arrayList;
        arrayList4 = arrayList2;
        createColorAnimation(view3, view4, z4, z5, fabTransformationSpec, arrayList3, arrayList4);
        createChildrenFadeAnimation(view3, view4, z4, z5, fabTransformationSpec, arrayList3, arrayList4);
        AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSetCompat.playTogether(animatorSet, arrayList);
        view4 = view;
        final View view5 = view2;
        animatorSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                if (z3) {
                    view5.setVisibility(0);
                    view4.setAlpha(0.0f);
                    view4.setVisibility(4);
                }
            }

            public void onAnimationEnd(Animator animator) {
                if (!z3) {
                    view5.setVisibility(4);
                    view4.setAlpha(1.0f);
                    view4.setVisibility(0);
                }
            }
        });
        int size = arrayList2.size();
        for (int i = 0; i < size; i++) {
            animatorSet.addListener((AnimatorListener) arrayList2.get(i));
        }
        return animatorSet;
    }

    @TargetApi(21)
    private void createElevationAnimation(View view, View view2, boolean z, boolean z2, FabTransformationSpec fabTransformationSpec, List<Animator> list, List<AnimatorListener> list2) {
        Animator ofFloat;
        float elevation = ViewCompat.getElevation(view2) - ViewCompat.getElevation(view);
        if (z) {
            if (!z2) {
                view2.setTranslationZ(-elevation);
            }
            ofFloat = ObjectAnimator.ofFloat(view2, View.TRANSLATION_Z, new float[]{0.0f});
        } else {
            ofFloat = ObjectAnimator.ofFloat(view2, View.TRANSLATION_Z, new float[]{-elevation});
        }
        fabTransformationSpec.timings.getTiming("elevation").apply(ofFloat);
        list.add(ofFloat);
    }

    private void createTranslationAnimation(View view, View view2, boolean z, boolean z2, FabTransformationSpec fabTransformationSpec, List<Animator> list, List<AnimatorListener> list2, RectF rectF) {
        MotionTiming timing;
        MotionTiming timing2;
        Animator ofFloat;
        Animator ofFloat2;
        View view3 = view;
        View view4 = view2;
        FabTransformationSpec fabTransformationSpec2 = fabTransformationSpec;
        List<Animator> list3 = list;
        float calculateTranslationX = calculateTranslationX(view3, view4, fabTransformationSpec2.positioning);
        float calculateTranslationY = calculateTranslationY(view3, view4, fabTransformationSpec2.positioning);
        if (calculateTranslationX == 0.0f || calculateTranslationY == 0.0f) {
            timing = fabTransformationSpec2.timings.getTiming("translationXLinear");
            timing2 = fabTransformationSpec2.timings.getTiming("translationYLinear");
        } else if ((!z || calculateTranslationY >= 0.0f) && (z || calculateTranslationY <= 0.0f)) {
            timing = fabTransformationSpec2.timings.getTiming("translationXCurveDownwards");
            timing2 = fabTransformationSpec2.timings.getTiming("translationYCurveDownwards");
        } else {
            timing = fabTransformationSpec2.timings.getTiming("translationXCurveUpwards");
            timing2 = fabTransformationSpec2.timings.getTiming("translationYCurveUpwards");
        }
        MotionTiming motionTiming = timing;
        MotionTiming motionTiming2 = timing2;
        if (z) {
            if (!z2) {
                view4.setTranslationX(-calculateTranslationX);
                view4.setTranslationY(-calculateTranslationY);
            }
            ofFloat = ObjectAnimator.ofFloat(view4, View.TRANSLATION_X, new float[]{0.0f});
            ofFloat2 = ObjectAnimator.ofFloat(view4, View.TRANSLATION_Y, new float[]{0.0f});
            calculateChildVisibleBoundsAtEndOfExpansion(view2, fabTransformationSpec, motionTiming, motionTiming2, -calculateTranslationX, -calculateTranslationY, 0.0f, 0.0f, rectF);
        } else {
            ofFloat = ObjectAnimator.ofFloat(view4, View.TRANSLATION_X, new float[]{-calculateTranslationX});
            ofFloat2 = ObjectAnimator.ofFloat(view4, View.TRANSLATION_Y, new float[]{-calculateTranslationY});
        }
        motionTiming.apply(ofFloat);
        motionTiming2.apply(ofFloat2);
        list3.add(ofFloat);
        list3.add(ofFloat2);
    }

    private void createIconFadeAnimation(View view, final View view2, boolean z, boolean z2, FabTransformationSpec fabTransformationSpec, List<Animator> list, List<AnimatorListener> list2) {
        if ((view2 instanceof CircularRevealWidget) && (view instanceof ImageView)) {
            final CircularRevealWidget circularRevealWidget = (CircularRevealWidget) view2;
            final Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null) {
                ObjectAnimator ofInt;
                drawable.mutate();
                if (z) {
                    if (!z2) {
                        drawable.setAlpha(255);
                    }
                    ofInt = ObjectAnimator.ofInt(drawable, DrawableAlphaProperty.DRAWABLE_ALPHA_COMPAT, new int[]{0});
                } else {
                    ofInt = ObjectAnimator.ofInt(drawable, DrawableAlphaProperty.DRAWABLE_ALPHA_COMPAT, new int[]{255});
                }
                ofInt.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        view2.invalidate();
                    }
                });
                fabTransformationSpec.timings.getTiming("iconFade").apply(ofInt);
                list.add(ofInt);
                list2.add(new AnimatorListenerAdapter() {
                    public void onAnimationStart(Animator animator) {
                        circularRevealWidget.setCircularRevealOverlayDrawable(drawable);
                    }

                    public void onAnimationEnd(Animator animator) {
                        circularRevealWidget.setCircularRevealOverlayDrawable(null);
                    }
                });
            }
        }
    }

    private void createExpansionAnimation(View view, View view2, boolean z, boolean z2, FabTransformationSpec fabTransformationSpec, float f, float f2, List<Animator> list, List<AnimatorListener> list2) {
        View view3 = view;
        View view4 = view2;
        FabTransformationSpec fabTransformationSpec2 = fabTransformationSpec;
        if (view4 instanceof CircularRevealWidget) {
            Animator createCircularReveal;
            MotionTiming motionTiming;
            final CircularRevealWidget circularRevealWidget = (CircularRevealWidget) view4;
            float calculateRevealCenterX = calculateRevealCenterX(view3, view4, fabTransformationSpec2.positioning);
            float calculateRevealCenterY = calculateRevealCenterY(view3, view4, fabTransformationSpec2.positioning);
            ((FloatingActionButton) view3).getContentRect(this.tmpRect);
            float width = ((float) this.tmpRect.width()) / 2.0f;
            MotionTiming timing = fabTransformationSpec2.timings.getTiming("expansion");
            float f3;
            if (z) {
                if (!z2) {
                    circularRevealWidget.setRevealInfo(new RevealInfo(calculateRevealCenterX, calculateRevealCenterY, width));
                }
                f3 = z2 ? circularRevealWidget.getRevealInfo().radius : width;
                createCircularReveal = CircularRevealCompat.createCircularReveal(circularRevealWidget, calculateRevealCenterX, calculateRevealCenterY, MathUtils.distanceToFurthestCorner(calculateRevealCenterX, calculateRevealCenterY, 0.0f, 0.0f, f, f2));
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        RevealInfo revealInfo = circularRevealWidget.getRevealInfo();
                        revealInfo.radius = Float.MAX_VALUE;
                        circularRevealWidget.setRevealInfo(revealInfo);
                    }
                });
                createPreFillRadialExpansion(view2, timing.getDelay(), (int) calculateRevealCenterX, (int) calculateRevealCenterY, f3, list);
                motionTiming = timing;
            } else {
                f3 = circularRevealWidget.getRevealInfo().radius;
                Animator createCircularReveal2 = CircularRevealCompat.createCircularReveal(circularRevealWidget, calculateRevealCenterX, calculateRevealCenterY, width);
                int i = (int) calculateRevealCenterX;
                int i2 = (int) calculateRevealCenterY;
                View view5 = view2;
                int i3 = i2;
                createPreFillRadialExpansion(view5, timing.getDelay(), i, i2, f3, list);
                motionTiming = timing;
                createPostFillRadialExpansion(view5, timing.getDelay(), timing.getDuration(), fabTransformationSpec2.timings.getTotalDuration(), i, i3, width, list);
                createCircularReveal = createCircularReveal2;
            }
            motionTiming.apply(createCircularReveal);
            list.add(createCircularReveal);
            list2.add(CircularRevealCompat.createCircularRevealListener(circularRevealWidget));
        }
    }

    private void createColorAnimation(View view, View view2, boolean z, boolean z2, FabTransformationSpec fabTransformationSpec, List<Animator> list, List<AnimatorListener> list2) {
        if (view2 instanceof CircularRevealWidget) {
            ObjectAnimator ofInt;
            CircularRevealWidget circularRevealWidget = (CircularRevealWidget) view2;
            int backgroundTint = getBackgroundTint(view);
            int i = ViewCompat.MEASURED_SIZE_MASK & backgroundTint;
            if (z) {
                if (!z2) {
                    circularRevealWidget.setCircularRevealScrimColor(backgroundTint);
                }
                ofInt = ObjectAnimator.ofInt(circularRevealWidget, CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR, new int[]{i});
            } else {
                ofInt = ObjectAnimator.ofInt(circularRevealWidget, CircularRevealScrimColorProperty.CIRCULAR_REVEAL_SCRIM_COLOR, new int[]{backgroundTint});
            }
            ofInt.setEvaluator(ArgbEvaluatorCompat.getInstance());
            fabTransformationSpec.timings.getTiming("color").apply(ofInt);
            list.add(ofInt);
        }
    }

    private void createChildrenFadeAnimation(View view, View view2, boolean z, boolean z2, FabTransformationSpec fabTransformationSpec, List<Animator> list, List<AnimatorListener> list2) {
        if (!(view2 instanceof ViewGroup)) {
            return;
        }
        if (!(view2 instanceof CircularRevealWidget) || CircularRevealHelper.STRATEGY != 0) {
            ViewGroup calculateChildContentContainer = calculateChildContentContainer(view2);
            if (calculateChildContentContainer != null) {
                Animator ofFloat;
                if (z) {
                    if (!z2) {
                        ChildrenAlphaProperty.CHILDREN_ALPHA.set(calculateChildContentContainer, Float.valueOf(0.0f));
                    }
                    ofFloat = ObjectAnimator.ofFloat(calculateChildContentContainer, ChildrenAlphaProperty.CHILDREN_ALPHA, new float[]{1.0f});
                } else {
                    ofFloat = ObjectAnimator.ofFloat(calculateChildContentContainer, ChildrenAlphaProperty.CHILDREN_ALPHA, new float[]{0.0f});
                }
                fabTransformationSpec.timings.getTiming("contentFade").apply(ofFloat);
                list.add(ofFloat);
            }
        }
    }

    private float calculateTranslationX(View view, View view2, Positioning positioning) {
        RectF rectF = this.tmpRectF1;
        RectF rectF2 = this.tmpRectF2;
        calculateWindowBounds(view, rectF);
        calculateWindowBounds(view2, rectF2);
        int i = positioning.gravity & 7;
        float centerX = i != 1 ? i != 3 ? i != 5 ? 0.0f : rectF2.right - rectF.right : rectF2.left - rectF.left : rectF2.centerX() - rectF.centerX();
        return centerX + positioning.xAdjustment;
    }

    private float calculateTranslationY(View view, View view2, Positioning positioning) {
        RectF rectF = this.tmpRectF1;
        RectF rectF2 = this.tmpRectF2;
        calculateWindowBounds(view, rectF);
        calculateWindowBounds(view2, rectF2);
        int i = positioning.gravity & 112;
        float centerY = i != 16 ? i != 48 ? i != 80 ? 0.0f : rectF2.bottom - rectF.bottom : rectF2.top - rectF.top : rectF2.centerY() - rectF.centerY();
        return centerY + positioning.yAdjustment;
    }

    private void calculateWindowBounds(View view, RectF rectF) {
        rectF.set(0.0f, 0.0f, (float) view.getWidth(), (float) view.getHeight());
        int[] iArr = this.tmpArray;
        view.getLocationInWindow(iArr);
        rectF.offsetTo((float) iArr[0], (float) iArr[1]);
        rectF.offset((float) ((int) (-view.getTranslationX())), (float) ((int) (-view.getTranslationY())));
    }

    private float calculateRevealCenterX(View view, View view2, Positioning positioning) {
        RectF rectF = this.tmpRectF1;
        RectF rectF2 = this.tmpRectF2;
        calculateWindowBounds(view, rectF);
        calculateWindowBounds(view2, rectF2);
        rectF2.offset(-calculateTranslationX(view, view2, positioning), 0.0f);
        return rectF.centerX() - rectF2.left;
    }

    private float calculateRevealCenterY(View view, View view2, Positioning positioning) {
        RectF rectF = this.tmpRectF1;
        RectF rectF2 = this.tmpRectF2;
        calculateWindowBounds(view, rectF);
        calculateWindowBounds(view2, rectF2);
        rectF2.offset(0.0f, -calculateTranslationY(view, view2, positioning));
        return rectF.centerY() - rectF2.top;
    }

    private void calculateChildVisibleBoundsAtEndOfExpansion(View view, FabTransformationSpec fabTransformationSpec, MotionTiming motionTiming, MotionTiming motionTiming2, float f, float f2, float f3, float f4, RectF rectF) {
        float calculateValueOfAnimationAtEndOfExpansion = calculateValueOfAnimationAtEndOfExpansion(fabTransformationSpec, motionTiming, f, f3);
        float calculateValueOfAnimationAtEndOfExpansion2 = calculateValueOfAnimationAtEndOfExpansion(fabTransformationSpec, motionTiming2, f2, f4);
        Rect rect = this.tmpRect;
        view.getWindowVisibleDisplayFrame(rect);
        RectF rectF2 = this.tmpRectF1;
        rectF2.set(rect);
        RectF rectF3 = this.tmpRectF2;
        calculateWindowBounds(view, rectF3);
        rectF3.offset(calculateValueOfAnimationAtEndOfExpansion, calculateValueOfAnimationAtEndOfExpansion2);
        rectF3.intersect(rectF2);
        rectF.set(rectF3);
    }

    private float calculateValueOfAnimationAtEndOfExpansion(FabTransformationSpec fabTransformationSpec, MotionTiming motionTiming, float f, float f2) {
        long delay = motionTiming.getDelay();
        long duration = motionTiming.getDuration();
        MotionTiming timing = fabTransformationSpec.timings.getTiming("expansion");
        return AnimationUtils.lerp(f, f2, motionTiming.getInterpolator().getInterpolation(((float) (((timing.getDelay() + timing.getDuration()) + 17) - delay)) / ((float) duration)));
    }

    @Nullable
    private ViewGroup calculateChildContentContainer(View view) {
        View findViewById = view.findViewById(R.id.mtrl_child_content_container);
        if (findViewById != null) {
            return toViewGroupOrNull(findViewById);
        }
        if ((view instanceof TransformationChildLayout) || (view instanceof TransformationChildCard)) {
            return toViewGroupOrNull(((ViewGroup) view).getChildAt(0));
        }
        return toViewGroupOrNull(view);
    }

    @Nullable
    private ViewGroup toViewGroupOrNull(View view) {
        return view instanceof ViewGroup ? (ViewGroup) view : null;
    }

    private int getBackgroundTint(View view) {
        ColorStateList backgroundTintList = ViewCompat.getBackgroundTintList(view);
        return backgroundTintList != null ? backgroundTintList.getColorForState(view.getDrawableState(), backgroundTintList.getDefaultColor()) : 0;
    }

    private void createPreFillRadialExpansion(View view, long j, int i, int i2, float f, List<Animator> list) {
        if (VERSION.SDK_INT >= 21 && j > 0) {
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(view, i, i2, f, f);
            createCircularReveal.setStartDelay(0);
            createCircularReveal.setDuration(j);
            list.add(createCircularReveal);
        }
    }

    private void createPostFillRadialExpansion(View view, long j, long j2, long j3, int i, int i2, float f, List<Animator> list) {
        if (VERSION.SDK_INT >= 21) {
            j += j2;
            if (j < j3) {
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(view, i, i2, f, f);
                createCircularReveal.setStartDelay(j);
                createCircularReveal.setDuration(j3 - j);
                list.add(createCircularReveal);
            }
        }
    }
}
