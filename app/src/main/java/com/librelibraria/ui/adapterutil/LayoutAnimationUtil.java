package com.librelibraria.ui.adapterutil;

import android.content.Context;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.librelibraria.R;

public class LayoutAnimationUtil {

    public static void runLayoutAnimation(androidx.recyclerview.widget.RecyclerView recyclerView) {
        if (recyclerView == null) return;
        
        Context context = recyclerView.getContext();
        LayoutAnimationController animationController = 
                AnimationUtils.loadLayoutAnimation(context, R.anim.item_slide_up);
        
        recyclerView.setLayoutAnimation(animationController);
        recyclerView.scheduleLayoutAnimation();
    }

    public static void runFadeInAnimation(androidx.recyclerview.widget.RecyclerView recyclerView) {
        if (recyclerView == null) return;
        
        Context context = recyclerView.getContext();
        LayoutAnimationController animationController = 
                AnimationUtils.loadLayoutAnimation(context, R.anim.fade_in);
        
        recyclerView.setLayoutAnimation(animationController);
        recyclerView.scheduleLayoutAnimation();
    }
}