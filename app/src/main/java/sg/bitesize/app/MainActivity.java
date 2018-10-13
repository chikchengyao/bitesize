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
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import sg.bitesize.app.R;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  private ArFragment arFragment;
  private Anchor targetArrowAnchor;
  private boolean isTracking;
  private boolean isHitting;
  private FloatingActionButton addButton;
  private ModelRenderable foodRenderable;
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
        renderFoodModel();
        openOrderMenu();
    });

    portion_button_add = (FloatingActionButton)findViewById(R.id.portion_button_add);
    portion_button_add.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) { showToast("Increase portion size"); }
    });

    portion_button_remove = (FloatingActionButton)findViewById(R.id.portion_button_remove);
    portion_button_remove.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) { showToast("Decrease portion size"); }
    });

    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    buildRenderable("burger.sfb").thenAccept(renderable -> foodRenderable = renderable);
    buildRenderable("arrow.sfb").thenAccept(renderable -> targetArrowRenderable = renderable);

    arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
        arFragment.onUpdate(frameTime);
        updateTracking();
        updateHitTest();
        updateTargetArrowNode();
    });
  }

    private void renderFoodModel() {
        Frame frame = arFragment.getArSceneView().getArFrame();
        android.graphics.Point pt = getScreenCenter();
        List<HitResult> hits;
        if (frame != null) {
            hits = frame.hitTest(pt.x, pt.y);
            for (HitResult hit : hits) {
                renderModel(hit, foodRenderable);
                break;
            }
        }
    }

    private void openOrderMenu() {
        //View orderMenu = findViewById(R.id.)
    }

    protected void onResume(){
      super.onResume();
      Util.showSplashScreen(this);
  }

  private CompletableFuture<ModelRenderable> buildRenderable(String uri) {
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
  private void showToast(String message) {
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

  private void updateTargetArrowNode() {
      if (isTracking && isHitting) {
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
   *
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
      return new android.graphics.Point(vw.getWidth()/2, vw.getHeight()/2);
  }

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
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
}
