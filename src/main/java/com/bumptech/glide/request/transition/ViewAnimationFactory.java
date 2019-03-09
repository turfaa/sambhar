package com.bumptech.glide.request.transition;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.bumptech.glide.load.DataSource;

public class ViewAnimationFactory<R> implements TransitionFactory<R> {
    private Transition<R> transition;
    private final ViewTransitionAnimationFactory viewTransitionAnimationFactory;

    private static class ConcreteViewTransitionAnimationFactory implements ViewTransitionAnimationFactory {
        private final Animation animation;

        ConcreteViewTransitionAnimationFactory(Animation animation) {
            this.animation = animation;
        }

        public Animation build(Context context) {
            return this.animation;
        }
    }

    private static class ResourceViewTransitionAnimationFactory implements ViewTransitionAnimationFactory {
        private final int animationId;

        ResourceViewTransitionAnimationFactory(int i) {
            this.animationId = i;
        }

        public Animation build(Context context) {
            return AnimationUtils.loadAnimation(context, this.animationId);
        }
    }

    public ViewAnimationFactory(Animation animation) {
        this(new ConcreteViewTransitionAnimationFactory(animation));
    }

    public ViewAnimationFactory(int i) {
        this(new ResourceViewTransitionAnimationFactory(i));
    }

    ViewAnimationFactory(ViewTransitionAnimationFactory viewTransitionAnimationFactory) {
        this.viewTransitionAnimationFactory = viewTransitionAnimationFactory;
    }

    public Transition<R> build(DataSource dataSource, boolean z) {
        if (dataSource == DataSource.MEMORY_CACHE || !z) {
            return NoTransition.get();
        }
        if (this.transition == null) {
            this.transition = new ViewTransition(this.viewTransitionAnimationFactory);
        }
        return this.transition;
    }
}
