package sg.bitesize.app;

import android.app.Activity;
import android.view.View;

class Util {
    private static boolean isFirstStartup = true;

    static void showSplashScreen(Activity activity){
        View splashScreen = activity.findViewById(R.id.splash_screen_layout);

        if (!isFirstStartup) {
            splashScreen.setVisibility(View.GONE);
            return;
        }


    }
}
