/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3d;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dUI;

/**
 * Keyboard shortcuts.
 * 
 * @author Armin Reichert
 */
public class SectionKeys extends Section {

	public SectionKeys(PacManGames3dUI ui, String title) {
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
		addInfo("Alt+LEFT", () -> PacManGames3d.PY_3D_PERSPECTIVE.get().prev().name()).available(() -> gameScene().is3D());
		addInfo("Alt+RIGHT", () -> PacManGames3d.PY_3D_PERSPECTIVE.get().next().name()).available(() -> gameScene().is3D());
		addInfo("Alt+3", "3D Play Scene On/Off");
		addInfo("P", "Pause On/Off");
		addInfo("SHIFT+P/SPACE", "Single Step");
		addInfo("Q", "Return to Intro Scene");
		addInfo("V", "Switch Pac-Man / Ms. Pac-Man");
		addInfo("1", "Start Playing (credit required)");
		addInfo("5", "Add credit");
	}
}