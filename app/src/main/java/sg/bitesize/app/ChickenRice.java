package sg.bitesize.app;

import android.app.Activity;

import com.google.ar.sceneform.rendering.ModelRenderable;

class ChickenRice extends Food {
    public int rice, meat, veg;
    public static ModelRenderable[][][] renderables = new ModelRenderable[3][3][3];

    public ChickenRice(MainActivity a, int r, int m, int v) {
        uri = String.format("rice%d_meat%d_veg%d.sfb", r + 1, m + 1, v);
        float c = 1f;
        if (r == 1) c = 1.05f;
        if (r == 2) c = 1.25f;
        minScale = 0.035f * c;
        maxScale = 0.0351f * c;
        rice = r;
        meat = m;
        veg = v;
        build(a);
    }

    @Override
    public void build(MainActivity a) {
        if (renderables[rice][meat][veg] == null) {
            buildCF = a.buildRenderable(uri).thenAccept(rend -> renderables[rice][meat][veg] = rend);
        }
    }

    @Override
    public ModelRenderable getRenderable() {
        return renderables[rice][meat][veg];
    }

    public void changeSize(MainActivity a, int r, int m, int v){
        ChickenRice newCR = new ChickenRice(a, r, m, v);
        this.swap(a, newCR);
        this.rice = r;
        this.meat = m;
        this.veg = v;
    }

    public void upsize(MainActivity a, int type) {
        if (type == 0 && this.rice <= 1) {
            changeSize(a, rice + 1, meat, veg);
        } else if (type == 1 && this.meat <= 1) {
            changeSize(a, rice, meat + 1, veg);
        } else if (type == 2 && this.veg <= 1) {
            changeSize(a, rice, meat, veg + 1);
        }
    }

    public void downsize(MainActivity a, int type) {
        if (type == 0 && this.rice >= 1) {
            changeSize(a, rice - 1, meat, veg);
        } else if (type == 1 && this.meat >= 1) {
            changeSize(a, rice, meat - 1, veg);
        } else if (type == 2 && this.veg >= 1) {
            changeSize(a, rice, meat, veg - 1);
        }
    }

}
