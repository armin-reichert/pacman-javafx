/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;

public class SectionGeneral extends InfoSection {
	private Button[] btnsSimulation;
	private Slider sliderTargetFrameRate;

	private CheckBox cbAutopilot;
	private CheckBox cbImmunity;
	private CheckBox cbUsePlayScene3D;

	public SectionGeneral(GameUI ui) {
		super(ui, "General");

		btnsSimulation = addButtons("Simulation", "Pause", "Step");
		btnsSimulation[0].setOnAction(e -> ui.togglePaused());
		btnsSimulation[1].setOnAction(e -> GameLoop.get().runSingleStep(true));

		sliderTargetFrameRate = addSlider("Target Framerate", 0, 120, 60);
		sliderTargetFrameRate.setSnapToTicks(true);
		sliderTargetFrameRate.setShowTickLabels(true);
		sliderTargetFrameRate.setShowTickMarks(true);
		sliderTargetFrameRate.setMinorTickCount(5);
		sliderTargetFrameRate.setMajorTickUnit(30);
		sliderTargetFrameRate.valueProperty().addListener(($value, _old, _new) -> {
			GameLoop.get().setTargetFrameRate(_new.intValue());
		});
		addInfo("Current Framerate",
				() -> String.format("%d Hz (target: %d Hz)", GameLoop.get().getFPS(), GameLoop.get().getTargetFrameRate()));
		addInfo("Total Ticks", GameLoop.get()::getTotalTicks);

		cbUsePlayScene3D = addCheckBox("Use 3D play scene", ui::toggleUse3DScene);
		cbAutopilot = addCheckBox("Autopilot", ui::toggleAutopilot);
		cbImmunity = addCheckBox("Player immune", ui::toggleImmunity);

		addInfo("Main scene", () -> String.format("w=%.0f h=%.0f", sceneWidth(), sceneHeight()));
	}

	@Override
	public void update() {
		super.update();

		btnsSimulation[0].setText(Env.$paused.get() ? "Resume" : "Pause");
		btnsSimulation[1].setDisable(!Env.$paused.get());
		sliderTargetFrameRate.setValue(GameLoop.get().getTargetFrameRate());

		cbAutopilot.setSelected(ui.gameController.autoControlled);
		cbImmunity.setSelected(ui.gameController.playerImmune);
		cbUsePlayScene3D.setSelected(Env.$3D.get());
	}
}