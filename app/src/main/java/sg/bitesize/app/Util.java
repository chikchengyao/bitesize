package sg.bitesize.app;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

class Util {
    private static boolean isFirstStartup = true;

    static void showSplashScreen(Activity activity){
        View splashScreen = activity.findViewById(R.id.splash_screen_layout);
            splashScreen.setElevation(1000);

        if (!isFirstStartup) {
            splashScreen.setVisibility(View.GONE);
            return;
        }

        //isFirstStartup = false;

        Animation fadeSplashScreen = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
        fadeSplashScreen.setStartOffset(5000);
        fadeSplashScreen.setDuration(500);

        fadeSplashScreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashScreen.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        splashScreen.startAnimation(fadeSplashScreen);
    }
}
