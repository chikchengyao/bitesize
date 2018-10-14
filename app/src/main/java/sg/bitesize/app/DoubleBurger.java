package sg.bitesize.app;

import com.google.ar.sceneform.rendering.ModelRenderable;

public class DoubleBurger extends Food {

    public DoubleBurger(MainActivity a) {
        uri = "burger.sfb";
        minScale = 1f;
        maxScale = 1.001f;
        build(a);
    }

    public DoubleBurger(MainActivity a, float size) {
        uri = "burger.sfb";
        minScale = size;
        maxScale = size + 0.001f;
        build(a);
    }

    public ModelRenderable getRenderable(){
        return genericRenderable;
    }
}
