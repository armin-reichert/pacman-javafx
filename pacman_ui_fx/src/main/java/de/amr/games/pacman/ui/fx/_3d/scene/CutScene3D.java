/*
MIT License

Copyright (c) 2021-22 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.scene;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx._2d.scene.pacman.PacManCutscene1;
import de.amr.games.pacman.ui.fx._3d.entity.World3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamDrone;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamTotal;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import javafx.beans.binding.DoubleExpression;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.transform.Translate;

/**
 * @author Armin Reichert
 */
public class CutScene3D implements GameScene {

	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);
	private final SubScene fxSubScene;
	private final Group contentRoot = new Group();
	private final AmbientLight light;

	private SceneContext ctx;
	private World3D world3D;
	private GameScene2D embeddedCutScene;

	private double w;
	private double h;

	private void setZoom(double zoom) {
		w = zoom * ArcadeWorld.WORLD_SIZE.x();
		h = zoom * ArcadeWorld.WORLD_SIZE.y();
	}

	public CutScene3D() {
		setZoom(0.5);
		cameraMap.put(Perspective.DRONE, new CamDrone());
		cameraMap.put(Perspective.TOTAL, new CamTotal());

		light = new AmbientLight(Env.lightColorPy.get());
		light.colorProperty().bind(Env.lightColorPy);

		// origin is at center of scene content
		contentRoot.getTransforms().add(new Translate(-DEFAULT_WIDTH / 2, -DEFAULT_HEIGHT / 2));

		// initial size does not matter, subscene is resized automatically
		fxSubScene = new SubScene(new Group(contentRoot, light), 50, 50, true, SceneAntialiasing.BALANCED);
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void init() {

		var content = contentRoot.getChildren();
		content.clear();

		world3D = new World3D(ctx.game(), ctx.model3D, ctx.r2D);
		content.add(world3D);

		changeCamera(Perspective.DRONE);

		embeddedCutScene = new PacManCutscene1();
		embeddedCutScene.resize(h);
		embeddedCutScene.setSceneContext(ctx);
		embeddedCutScene.init();

		var root = embeddedCutScene.getRoot();
		root.setTranslateX(0.5 * (DEFAULT_WIDTH - w));
		root.setTranslateY(0.5 * (DEFAULT_HEIGHT - h));
		root.setTranslateZ(-h / 2);
//		root.rotationAxisProperty().bind(getCamera().rotationAxisProperty());
//		root.rotateProperty().bind(getCamera().rotateProperty());
		content.add(root);
	}

	@Override
	public void updateAndRender() {
		world3D.update(ctx.game());
		embeddedCutScene.updateAndRender();
	}

	@Override
	public void setSceneContext(SceneContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	@Override
	public void setResizeBehavior(DoubleExpression width, DoubleExpression height) {
		fxSubScene.widthProperty().bind(width);
		fxSubScene.heightProperty().bind(height);
	}

	public GameSceneCamera getCameraForPerspective(Perspective perspective) {
		return cameraMap.get(perspective);
	}

	public GameSceneCamera getCamera() {
		return (GameSceneCamera) fxSubScene.getCamera();
	}

	private void changeCamera(Perspective perspective) {
		var oldCamera = fxSubScene.getCamera();
		var camera = cameraMap.get(perspective);
		if (camera != oldCamera) {
			fxSubScene.setCamera(camera);
			fxSubScene.setOnKeyPressed(camera::onKeyPressed);
			fxSubScene.requestFocus();
			camera.reset();
		}
		if (world3D != null && world3D.getScores3D() != null) {
			var scores3D = world3D.getScores3D();
			scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
			scores3D.rotateProperty().bind(camera.rotateProperty());
		}
	}
}