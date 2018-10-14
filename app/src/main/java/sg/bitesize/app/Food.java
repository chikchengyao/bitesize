package sg.bitesize.app;

import com.google.ar.sceneform.rendering.ModelRenderable;

abstract class Food {
    static String uri;
    public static ModelRenderable genericRenderable;
    public static float maxScale;
    public static float minScale;

    public void build(MainActivity a) {
        if (genericRenderable == null) {
            a.buildRenderable(uri).thenAccept(r -> genericRenderable = r);
        }
    }

    public abstract ModelRenderable getRenderable();
}
