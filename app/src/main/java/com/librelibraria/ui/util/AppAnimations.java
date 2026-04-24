package com.librelibraria.ui.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

public class AppAnimations {

    private static final String TAG = "AppAnimations";

    public static void shake(View view) {
        if (view == null) return;
        
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.shake);
        view.startAnimation(shake);
    }

    public static void bounce(View view) {
        if (view == null) return;
        
        Animation bounce = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.bounce);
        view.startAnimation(bounce);
    }

    public static void fadeIn(View view) {
        if (view == null) return;
        
        Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(fadeIn);
    }

    public static void fadeOut(View view) {
        if (view == null) return;
        
        Animation fadeOut = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(fadeOut);
    }

    public static void scaleIn(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        Animation scaleIn = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.scale_in);
        view.startAnimation(scaleIn);
    }

    public static void slideInFromBottom(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.slide_in_bottom);
        view.startAnimation(slideIn);
    }

    public static void slideInFromRight(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.slide_in_right);
        view.startAnimation(slideIn);
    }

    public static void slideInFromLeft(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        Animation slideIn = AnimationUtils.loadAnimation(view.getContext(), com.librelibraria.R.anim.slide_in_left);
        view.startAnimation(slideIn);
    }

    public static void scaleInWithElastic(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setScaleX(0f);
        view.setScaleY(0f);
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(new OvershootInterpolator(1.5f))
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }
            })
            .start();
    }

    public static void rotate(View view, float fromDegrees, float toDegrees) {
        if (view == null) return;
        
        view.setRotation(fromDegrees);
        view.animate()
            .rotation(toDegrees)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator())
            .start();
    }

    public static void spin(View view) {
        if (view == null) return;
        
        view.animate()
            .rotation(view.getRotation() + 360f)
            .setDuration(500)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .start();
    }

    public static void pulse(View view) {
        if (view == null) return;
        
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f);
        
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleUp, scaleUpY);
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1.2f, 1f);
                ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.2f, 1f);
                
                AnimatorSet set2 = new AnimatorSet();
                set2.playTogether(scaleDown, scaleDownY);
                set2.setDuration(150);
                set2.setInterpolator(new OvershootInterpolator(1.5f));
                set2.start();
            }
        });
        
        set.start();
    }

    public static void wiggle(View view) {
        if (view == null) return;
        
        ObjectAnimator moveLeft = ObjectAnimator.ofFloat(view, "translationX", 0f, -10f);
        ObjectAnimator moveRight = ObjectAnimator.ofFloat(view, "translationX", -10f, 10f);
        ObjectAnimator center = ObjectAnimator.ofFloat(view, "translationX", 10f, 0f);
        
        AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(moveLeft);
        set1.setDuration(50);
        
        AnimatorSet set2 = new AnimatorSet();
        set2.playTogether(moveRight);
        set2.setDuration(50);
        
        AnimatorSet set3 = new AnimatorSet();
        set3.playTogether(center);
        set3.setDuration(50);
        
        set1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                set2.start();
            }
        });
        
        set2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                set3.start();
            }
        });
        
        set1.start();
    }

    public static void slideUpReveal(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .start();
    }

    public static void slideDownReveal(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(-view.getHeight());
        view.setAlpha(0f);
        
        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .start();
    }

    public static void crossFade(View viewOut, View viewIn) {
        if (viewOut == null || viewIn == null) return;
        
        viewIn.setAlpha(0f);
        viewIn.setVisibility(View.VISIBLE);
        
        viewOut.animate()
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewOut.setVisibility(View.GONE);
                    viewIn.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
                }
            })
            .start();
    }

    public static void cardPress(View view) {
        if (view == null) return;
        
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(new OvershootInterpolator(2f))
                        .start();
                }
            })
            .start();
    }

    public static void fabReveal(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setScaleX(0f);
        view.setScaleY(0f);
        
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
        
        set.playTogether(fadeIn, scaleX, scaleY);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator(2f));
        set.start();
    }

    public static void fabHide(View view) {
        if (view == null) return;
        
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
        
        set.playTogether(fadeOut, scaleX, scaleY);
        set.setDuration(200);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        set.start();
    }

    public static void ripple(View view) {
        if (view == null) return;
        
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1f);
        
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);
        scaleUp.setDuration(100);
        
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);
        scaleDown.setDuration(100);
        
        scaleUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                scaleDown.start();
            }
        });
        
        scaleUp.start();
    }

    public static void zoomIn(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        
        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(400)
            .setInterpolator(new OvershootInterpolator(2f))
            .start();
    }

    public static void zoomOut(View view) {
        if (view == null) return;
        
        view.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            })
            .start();
    }

    public static void popIn(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }

    public static void popOut(View view) {
        if (view == null) return;
        
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        set.start();
    }

    public static void flipIn(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setRotationY(90f);
        view.setAlpha(0f);
        
        view.animate()
            .rotationY(0f)
            .alpha(1f)
            .setDuration(400)
            .setInterpolator(new OvershootInterpolator(1.5f))
            .start();
    }

    public static void flipOut(View view) {
        if (view == null) return;
        
        view.animate()
            .rotationY(-90f)
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.setRotationY(0f);
                }
            })
            .start();
    }

    public static void slideFromRightReveal(View view) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setTranslationX(view.getWidth());
        
        view.animate()
            .translationX(0f)
            .setDuration(300)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .start();
    }

    public static void slideToRightVanish(View view) {
        if (view == null) return;
        
        int width = view.getWidth();
        view.animate()
            .translationX(width)
            .setDuration(200)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.setTranslationX(0f);
                }
            })
            .start();
    }

    public static void seqSlideIn(View[] views, int delayMs) {
        if (views == null) return;
        
        for (int i = 0; i < views.length; i++) {
            final View view = views[i];
            if (view == null) continue;
            
            view.setAlpha(0f);
            view.setTranslationY(50f);
            
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(i * delayMs)
                .setInterpolator(new OvershootInterpolator())
                .start();
        }
    }

    public static void seqFadeIn(View[] views, int delayMs) {
        if (views == null) return;
        
        for (int i = 0; i < views.length; i++) {
            final View view = views[i];
            if (view == null) continue;
            
            view.setAlpha(0f);
            
            view.animate()
                .alpha(1f)
                .setDuration(200)
                .setStartDelay(i * delayMs)
                .start();
        }
    }

    public static void shimmer(View view) {
        if (view == null) return;
        
        view.setAlpha(0.5f);
        view.animate()
            .alpha(1f)
            .setDuration(100)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.animate()
                        .alpha(0.5f)
                        .setDuration(100)
                        .start();
                }
            })
            .start();
    }

    public static void setViewShadows(View view, float elevation) {
        if (view == null) return;
        view.setElevation(elevation);
    }

    public static void animateBackgroundColor(View view, int fromColor, int toColor) {
        if (view == null) return;
        
        ObjectAnimator colorAnim = ObjectAnimator.ofArgb(view, "backgroundColor", fromColor, toColor);
        colorAnim.setDuration(300);
        colorAnim.start();
    }
}