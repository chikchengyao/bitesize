package sg.bitesize.app;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;

abstract class Food {
    static String uri;
    public static ModelRenderable genericRenderable;
    public float maxScale;
    public float minScale;
    public AnchorNode anchorNode;
    public TransformableNode transformableNode;
    public CompletableFuture buildCF;

    public void build(MainActivity a) {
        if (genericRenderable == null) {
            buildCF =  a.buildRenderable(uri).thenAccept(r -> genericRenderable = r);
        }
    }

    public abstract ModelRenderable getRenderable();

    public void swap(MainActivity a, Food food) {
        if (anchorNode == null) {
            a.showToast("No anchorNode");
            return;
        }
        if (food.getRenderable() != null) {
            transformableNode.setRenderable(food.getRenderable());
            transformableNode.getScaleController().setMaxScale(food.maxScale);
            transformableNode.getScaleController().setMinScale(food.minScale);
        } else {
            food.buildCF.thenRun(() -> {
                transformableNode.setRenderable(food.getRenderable());
                transformableNode.getScaleController().setMaxScale(food.maxScale);
                transformableNode.getScaleController().setMinScale(food.minScale);
            });
        }
    }
}
