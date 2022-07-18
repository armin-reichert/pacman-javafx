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
package de.amr.games.pacman.ui.fx.shell.info;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.geometry.Point3D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;

/**
 * 3D related settings.
 * 
 * @author Armin Reichert
 */
public class SectionCamera3D extends Section {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final ComboBox<Perspective> comboPerspective;
	private final ComboBox<Point3D> comboRotationAxis;
	private final Slider sliderTransformX;
	private final Slider sliderTransformY;
	private final Slider sliderTransformZ;
	private final Slider sliderRotate;

	public SectionCamera3D(GameUI ui, GameController gc, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		super(ui, gc, title, minLabelWidth, textColor, textFont, labelFont);

		comboPerspective = addComboBox("Perspective", Perspective.values());
		comboPerspective.setOnAction(e -> Env.perspectivePy.set(comboPerspective.getValue()));
		Env.perspectivePy.addListener((obs, oldVal, newVal) -> onPerspectiveChanged(newVal));

		sliderTransformX = addSlider("Translate X", -500, 500, 0);
		sliderTransformY = addSlider("Translate Y", -500, 500, 0);
		sliderTransformZ = addSlider("Translate Z", -500, 500, 0);

		sliderTransformX.setDisable(true);
		sliderTransformY.setDisable(true);
		sliderTransformZ.setDisable(true);

		comboRotationAxis = addComboBox("Rotation Axis", Rotate.X_AXIS, Rotate.Y_AXIS, Rotate.Z_AXIS);
		comboRotationAxis.setDisable(true);

		sliderRotate = addSlider("Rotate", 0, 360, 0);
		sliderRotate.setDisable(true);

		addInfo("Camera",
				() -> (gameScene() instanceof PlayScene3D playScene3D) ? playScene3D.getCamera().transformInfo() : "")
						.available(() -> gameScene().is3D());
		addInfo("Shift+LEFT/RIGHT", "Camera -X / +X").available(() -> Env.perspectivePy.get() == Perspective.TOTAL);
		addInfo("Shift+PLUS/MINUS", "Camera -Y / +Y").available(() -> Env.perspectivePy.get() == Perspective.TOTAL);
		addInfo("Shift+UP/DOWN", "Camera -Z / +Z")
				.available(() -> Env.perspectivePy.get() == Perspective.TOTAL || Env.perspectivePy.get() == Perspective.DRONE);
		addInfo("Ctrl+Shift+UP/DOWN", "Camera Rotate X").available(() -> Env.perspectivePy.get() == Perspective.TOTAL);
	}

	@Override
	public void update() {
		super.update();
		comboPerspective.setValue(Env.perspectivePy.get());
		comboPerspective.setDisable(!gameScene().is3D());
	}

	private void onPerspectiveChanged(Perspective perspective) {
		LOGGER.info("Perspective changed to %s", perspective);
		var configurableCam = configurableCam(perspective);
		configurableCam.ifPresent(cam -> {
			LOGGER.info("Configurable camera: %s", cam);
			sliderTransformX.setValue(cam.getTranslateX());
			sliderTransformY.setValue(cam.getTranslateY());
			sliderTransformZ.setValue(cam.getTranslateZ());
			comboRotationAxis.setValue(cam.getRotationAxis());
			sliderRotate.setValue(cam.getRotate());
			cam.translateXProperty().bind(sliderTransformX.valueProperty());
			cam.translateYProperty().bind(sliderTransformY.valueProperty());
			cam.translateZProperty().bind(sliderTransformZ.valueProperty());
			cam.rotationAxisProperty().bind(comboRotationAxis.valueProperty());
			cam.rotateProperty().bind(sliderRotate.valueProperty());
		});
		sliderTransformX.setDisable(configurableCam.isEmpty());
		sliderTransformY.setDisable(configurableCam.isEmpty());
		sliderTransformZ.setDisable(configurableCam.isEmpty());
		comboRotationAxis.setDisable(configurableCam.isEmpty());
		sliderRotate.setDisable(configurableCam.isEmpty());
	}

	private Optional<GameSceneCamera> configurableCam(Perspective perspective) {
		if (ui.getSceneManager().getCurrentGameScene() instanceof PlayScene3D playScene3D) {
			var cam = playScene3D.getCameraForPerspective(perspective);
			return cam.isConfigurable() ? Optional.of(cam) : Optional.empty();
		}
		return Optional.empty();
	}
}