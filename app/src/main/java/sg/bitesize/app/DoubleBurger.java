package sg.bitesize.app;

import android.app.Activity;

import com.google.ar.sceneform.rendering.ModelRenderable;

public class DoubleBurger extends Food {

    public DoubleBurger(MainActivity a) {
        uri = "burger.sfb";
        minScale = 1f;
        maxScale = 1.001f;
        build(a);
    }

    public ModelRenderable getRenderable(){
        return genericRenderable;
    }
}
