package sg.bitesize.app;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

abstract class Food {
    static String uri;
    public static ModelRenderable genericRenderable;
    public float maxScale;
    public float minScale;
    public AnchorNode anchorNode;
    public TransformableNode transformableNode;

    public void build(MainActivity a) {
        if (genericRenderable == null) {
            a.buildRenderable(uri).thenAccept(r -> genericRenderable = r);
        }
    }

    public abstract ModelRenderable getRenderable();

    public void swap(MainActivity a, Food food) {
        if (anchorNode == null) {
            a.showToast("No anchorNode");
            return;
        }

        transformableNode.setRenderable(food.getRenderable());
        transformableNode.getScaleController().setMaxScale(food.maxScale);
        transformableNode.getScaleController().setMinScale(food.minScale);
    }
}
