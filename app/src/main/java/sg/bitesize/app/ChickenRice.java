package sg.bitesize.app;

import android.app.Activity;

import com.google.ar.sceneform.rendering.ModelRenderable;

class ChickenRice extends Food {
    public int rice, meat, veg;

    public static ModelRenderable[][][] renderables = new ModelRenderable[3][3][3];

    public ChickenRice(MainActivity a, int r, int m, int v) {
        uri = String.format("rice%d_meat%d_veg%d.sfb", r + 1, m + 1, v);
        minScale = 0.035f;
        maxScale = 0.0351f;
        rice = r;
        meat = m;
        veg = v;
        build(a);
    }

    @Override
    public void build(MainActivity a) {
        if (renderables[rice][meat][veg] == null) {
            a.buildRenderable(uri).thenAccept(rend -> renderables[rice][meat][veg] = rend);
        }
    }

    @Override
    public ModelRenderable getRenderable() {
        return renderables[rice][meat][veg];
    }
}
