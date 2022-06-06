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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Keyboard shortcuts.
 * 
 * @author Armin Reichert
 */
public class SectionKeys extends Section {

	public SectionKeys(GameUI ui, GameController gc, String title, int minLabelWidth, Color textColor, Font textFont,
			Font labelFont) {
		super(ui, gc, title, minLabelWidth, textColor, textFont, labelFont);
		addInfo("Ctrl+I", "Info Panels On/Off");
		addInfo("Alt+A", "Autopilot On/Off");
		addInfo("Alt+E", "Eat all normal pellets").available(() -> gc.isGameRunning());
		addInfo("Alt+I", "Player immunity On/Off");
		addInfo("Alt+L", "Add 3 player lives").available(() -> gc.isGameRunning());
		addInfo("Alt+M", "Mute/Unmute");
		addInfo("Alt+N", "Next Level").available(() -> gc.isGameRunning());
		addInfo("Alt+X", "Kill hunting ghosts").available(() -> gc.isGameRunning());
		addInfo("Alt+Z", "Play Intermission Scenes").available(() -> gc.state() == GameState.INTRO);
		addInfo("Alt+LEFT", () -> Env.$perspective.get().prev().name()).available(() -> ui.getCurrentGameScene().is3D());
		addInfo("Alt+RIGHT", () -> Env.$perspective.get().next().name()).available(() -> ui.getCurrentGameScene().is3D());
		addInfo("Alt+3", "3D Playscene On/Off");
		addInfo("Q", "Quit Scene").available(() -> gc.state() != GameState.INTRO);
		addInfo("V", "Switch game variant").available(() -> gc.state() == GameState.INTRO);
		addInfo("5", "Add credit");
	}
}