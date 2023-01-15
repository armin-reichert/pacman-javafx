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
package de.amr.games.pacman.ui.fx.dashboard;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class SectionGhostsInfo extends Section {

	public SectionGhostsInfo(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);
		addGhostInfo(Ghost.ID_RED_GHOST);
		addEmptyLine();
		addGhostInfo(Ghost.ID_PINK_GHOST);
		addEmptyLine();
		addGhostInfo(Ghost.ID_CYAN_GHOST);
		addEmptyLine();
		addGhostInfo(Ghost.ID_ORANGE_GHOST);
	}

	private void addGhostInfo(byte ghostID) {
		var color = switch (ghostID) {
		case Ghost.ID_RED_GHOST -> "Red";
		case Ghost.ID_PINK_GHOST -> "Pink";
		case Ghost.ID_CYAN_GHOST -> "Cyan";
		case Ghost.ID_ORANGE_GHOST -> "Orange";
		default -> "";
		};
		addInfo(color + " Ghost", ifLevelExists(this::ghostName, ghostID));
		addInfo("State", ifLevelExists(this::ghostState, ghostID));
		addInfo("Killed Index", ifLevelExists(this::ghostKilledIndex, ghostID));
		addInfo("Animation", ifLevelExists(this::ghostAnimation, ghostID));
		addInfo("Movement", ifLevelExists(this::ghostMovement, ghostID));
		addInfo("Tile", ifLevelExists(this::ghostTile, ghostID));
	}

	private Supplier<String> ifLevelExists(BiFunction<GameLevel, Ghost, String> fnGhostInfo, byte ghostID) {
		return () -> {
			var level = game().level().orElse(null);
			return level != null ? fnGhostInfo.apply(level, level.ghost(ghostID)) : InfoText.NO_INFO;
		};
	}

	private String ghostName(GameLevel level, Ghost ghost) {
		return ghost.name();
	}

	private String ghostAnimation(GameLevel level, Ghost ghost) {
		var anims = ghost.animations();
		if (anims.isEmpty()) {
			return InfoText.NO_INFO;
		}
		var animKey = anims.get().selectedKey().name();
		var selectedAnim = anims.get().selectedAnimation();
		if (selectedAnim.isPresent()) {
			var running = selectedAnim.get().isRunning();
			return "%s %s".formatted(animKey, running ? "running" : "stopped");
		} else {
			return InfoText.NO_INFO;
		}
	}

	private String ghostTile(GameLevel level, Ghost ghost) {
		return "%s Offset %s".formatted(ghost.tile(), ghost.offset());
	}

	private String ghostState(GameLevel level, Ghost ghost) {
		var stateText = ghost.state().name();
		if (ghost.state() == GhostState.HUNTING_PAC) {
			stateText = level.currentHuntingPhaseName();
		}
		return stateText;
	}

	private String ghostMovement(GameLevel level, Ghost ghost) {
		var speed = ghost.velocity().length();
		return "%.2f px %s (%s)".formatted(speed, ghost.moveDir(), ghost.wishDir());
	}

	private String ghostKilledIndex(GameLevel level, Ghost ghost) {
		return "%d".formatted(ghost.killedIndex());
	}
}