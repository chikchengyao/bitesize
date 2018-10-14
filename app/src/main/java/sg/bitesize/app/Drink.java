package sg.bitesize.app;

import com.google.ar.sceneform.rendering.ModelRenderable;

class Drink extends Food {
    public String type;

    public Drink(MainActivity a, String drink_type) {
        uri = String.format("%s.sfb", drink_type);
        minScale = 0.065f;
        maxScale = 0.0651f;
        build(a);
    }

    @Override
    public ModelRenderable getRenderable() { return genericRenderable; }
}