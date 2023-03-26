/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.dashboard;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.shell.GameUI;

/**
 * Keyboard shortcuts.
 * 
 * @author Armin Reichert
 */
public class SectionKeys extends Section {

	public SectionKeys(GameUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		addInfo("F1", "Dashboard On/Off");
		addInfo("F2", "PiP View On/Off");
		addInfo("F3", "Reboot");
		addInfo("Alt+A", "Autopilot On/Off");
		addInfo("Alt+B", "Dashboard On/Off");
		addInfo("Alt+E", "Eat all normal pellets").available(() -> gc.game().isPlaying());
		addInfo("Alt+I", "Player immunity On/Off");
		addInfo("Alt+L", "Add 3 player lives").available(() -> gc.game().isPlaying());
		addInfo("Alt+N", "Next Level").available(() -> gc.game().isPlaying());
		addInfo("Alt+X", "Kill hunting ghosts").available(() -> gc.game().isPlaying());
		addInfo("Alt+Z", "Play Intermission Scenes").available(() -> gc.state() == GameState.INTRO);
		addInfo("Alt+LEFT", () -> Env.d3perspectivePy.get().prev().name()).available(() -> gameScene().is3D());
		addInfo("Alt+RIGHT", () -> Env.d3perspectivePy.get().next().name()).available(() -> gameScene().is3D());
		addInfo("Alt+3", "3D Play Scene On/Off");
		addInfo("P", "Pause On/Off");
		addInfo("SHIFT+P/SPACE", "Single Step");
		addInfo("Q", "Return to Intro Scene");
		addInfo("V", "Switch Pac-Man / Ms. Pac-Man");
		addInfo("1", "Start Playing (credit required)");
		addInfo("5", "Add credit");
	}
}