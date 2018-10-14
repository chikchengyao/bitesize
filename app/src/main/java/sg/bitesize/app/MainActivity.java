/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sg.bitesize.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private Anchor targetArrowAnchor;
    private boolean isHitting;
    private boolean isTracking;
    private boolean isOrderMenuVisible = false;
    private boolean isCheckoutVisible = false;
    private FloatingActionButton addButton;

    private ModelRenderable bigBurgerRenderable;
    private ModelRenderable smallBurgerRenderable;
    private ModelRenderable targetArrowRenderable;

    FloatingActionButton portion_button_add, portion_button_remove;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener((View view) -> {
            Log.i("APP: addButton_OnClick", String.format("isTracking: %s; isHitting: %s", isTracking, isHitting));

            toggleOrderMenu();
        });

        portion_button_add = (FloatingActionButton) findViewById(R.id.portion_button_add);
        portion_button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Increase portion size");
            }
        });

        portion_button_remove = (FloatingActionButton) findViewById(R.id.portion_button_remove);
        portion_button_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToast("Decrease portion size");
            }
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        buildRenderable("burger.sfb").thenAccept(renderable -> bigBurgerRenderable = renderable);
        buildRenderable("single-patty-burger.sfb").thenAccept(renderable -> smallBurgerRenderable = renderable);
        buildRenderable("arrow.sfb").thenAccept(renderable -> targetArrowRenderable = renderable);
        //buildChickenRiceRenderable();

        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            arFragment.onUpdate(frameTime);
            updateTracking();
            updateHitTest();
            updateTargetArrowNode();
        });
    }

    private void toggleOrderMenu() {
        View orderMenu = findViewById(R.id.order_menu_layout);
        if (isOrderMenuVisible) {
            orderMenu.setVisibility(View.INVISIBLE);
        } else {
            orderMenu.setVisibility(View.VISIBLE);
            orderMenu.bringToFront();
        }
        isOrderMenuVisible = !isOrderMenuVisible;
    }

    protected void toggleCheckout(View orderMenuView) {
        View checkoutView = findViewById(R.id.checkout);
        if (isOrderMenuVisible) {
            toggleOrderMenu();
        }
        if (isCheckoutVisible) {
            checkoutView.setVisibility(View.INVISIBLE);
        } else {
            checkoutView.setVisibility(View.VISIBLE);
            checkoutView.bringToFront();
        }
        isCheckoutVisible = !isCheckoutVisible;
    }

    protected void onResume() {
        super.onResume();
        Util.showSplashScreen(this);
    }

    CompletableFuture<ModelRenderable> buildRenderable(String uri) {
        return ModelRenderable.builder()
                .setSource(this, Uri.parse(uri))
                .build()
                .exceptionally(
                        throwable -> {
                            showToast("Unable to load food model");
                            return null;
                        });
    }

    /**
     * Renders a toast of LENGTH_LONG at the bottom center of the screen
     */
    void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    /**
     * Renders a ModelRenderable model on a HitResult.
     */
    private Anchor renderModel(HitResult hitResult, ModelRenderable model, boolean should_select) {
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable node and add it to the anchor.
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.getScaleController().setSensitivity(0);  // disable pinch-and-scale
        node.setParent(anchorNode);
        node.setRenderable(model);
        if (should_select) {
            node.select();
        }

        return anchor;
    }

    private Anchor renderModel(HitResult hitResult, ModelRenderable model) {
        return renderModel(hitResult, model, true);
    }

    private void renderModel(ModelRenderable renderable) {
        Frame frame = arFragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                renderModel(hit, renderable);
                break;
            }
        }
    }

    private Anchor renderFood(HitResult hitResult, Food food, boolean should_select) {
        Anchor anchor = hitResult.createAnchor();
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the transformable node and add it to the anchor.
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.getScaleController().setSensitivity(0);  // disable pinch-and-scale
        node.getScaleController().setMaxScale(food.maxScale);
        node.getScaleController().setMinScale(food.minScale);
        node.setParent(anchorNode);
        node.setRenderable(food.getRenderable());
        if (should_select) {
            node.select();
        }

        return anchor;
    }

    private void renderFood(Food food) {
        if (food.getRenderable() == null) {
            food.build(this);
        }

        Frame frame = arFragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                renderFood(hit, food, true);
                break;
            }
        }
    }

    private void updateTargetArrowNode() {
        if (isTracking && isHitting) {
            if (!isOrderMenuVisible && targetArrowAnchor != null) {
                targetArrowAnchor.detach();
                return;
            }
            Frame frame = arFragment.getArSceneView().getArFrame();
            android.graphics.Point pt = getScreenCenter();
            List<HitResult> hits;
            if (frame != null) {
                hits = frame.hitTest(pt.x, pt.y);
                for (HitResult hit : hits) {
                    if (targetArrowAnchor != null) {
                        targetArrowAnchor.detach();
                    }
                    targetArrowAnchor = renderModel(hit, targetArrowRenderable, false);
                    break;
                }
            }
        }
    }

    /**
     * Updates the value of isTracking. isTracking is something like whether or not a plane has been
     * detected and is currently being tracked by sceneform.
     */
    private boolean updateTracking() {
        Frame frame = arFragment.getArSceneView().getArFrame();
        boolean wasTracking = isTracking;
        isTracking = frame != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING;
        return isTracking != wasTracking;
    }

    /**
     * Updates the value of isHitting. isHitting is something like whether or not the current center
     * of the screen lies on a plane that is tracked.
     * <p>
     * Also reinstantiates a AnchorNode for the targetArrowNode
     */
    private boolean updateHitTest() {
        Frame frame = arFragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        boolean wasHitting = isHitting;
        isHitting = false;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                Trackable trackable = hit.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                    isHitting = true;
                    break;
                }
            }
        }
        return wasHitting != isHitting;
    }

    /**
     * Returns the current center of the screen. Used in the onClick of addButton to figure out
     * where the new model should be spawned.
     */
    private android.graphics.Point getScreenCenter() {
        View vw = findViewById(android.R.id.content);
        return new android.graphics.Point(vw.getWidth() / 2, vw.getHeight() / 2);
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     * <p>
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     * <p>
     * <p>Finishes the activity if Sceneform can not run
     */
    private static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    public void renderBigBurger(View view) {
        renderFood(new DoubleBurger(this));
    }

    public void renderSmallBurger(View view) {
        renderModel(smallBurgerRenderable);
    }

    ModelRenderable temp;

    public void renderChickenRice(View view) {
        //buildRenderable("rice1_meat1_veg0.sfb").thenAccept(renderable -> temp = renderable);
        //renderModel(temp);
        renderFood(new ChickenRice(this, 1,1,1));
    }

    public void renderBandung(View view) {
        renderFood(new Drink(this, "bandung-beng"));
    }

    public void renderKopi(View view) {
        renderFood(new Drink(this, "kopi-o-kosing"));
    }

    public void renderTeh(View view) {
        renderFood(new Drink(this, "teh-beng"));
    }
}
