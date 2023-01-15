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

import de.amr.games.pacman.model.common.actors.Ghost;
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
		addInfo(color + " Ghost", () -> ghostName(ghostID));
		addInfo("State", () -> ghostState(ghostID));
		addInfo("Killed Index", () -> ghostKilledIndex(ghostID));
		addInfo("Animation", () -> ghostAnimation(ghostID));
		addInfo("Movement", () -> ghostDirections(ghostID));
		addInfo("Tile", () -> ghostTile(ghostID));
	}

	private String ghostName(byte ghostID) {
		var level = game().level();
		if (level.isEmpty()) {
			return "n/a";
		}
		var ghost = level.get().ghost(ghostID);
		return ghost.name();
	}

	private String ghostAnimation(byte ghostID) {
		var level = game().level();
		if (level.isEmpty()) {
			return "n/a";
		}
		var ghost = level.get().ghost(ghostID);
		var anims = ghost.animations();
		if (anims.isEmpty()) {
			return "n/a";
		}
		var animKey = anims.get().selectedKey().name();
		var running = anims.get().selectedAnimation().get().isRunning();
		return "%s %s".formatted(animKey, running ? "running" : "stopped");
	}

	private String ghostTile(byte ghostID) {
		var level = game().level();
		if (level.isEmpty()) {
			return "n/a";
		}
		var ghost = level.get().ghost(ghostID);
		return "%s Offset %s".formatted(ghost.tile(), ghost.offset());
	}

	private String ghostState(byte ghostID) {
		var level = game().level();
		if (level.isEmpty()) {
			return "n/a";
		}
		var ghost = level.get().ghost(ghostID);
		return ghost.state().name();
	}

	private String ghostDirections(byte ghostID) {
		var level = game().level();
		if (level.isEmpty()) {
			return "n/a";
		}
		var ghost = level.get().ghost(ghostID);
		return "moves %s wants %s".formatted(ghost.moveDir(), ghost.wishDir());
	}

	private String ghostKilledIndex(byte ghostID) {
		var level = game().level();
		if (level.isEmpty()) {
			return "n/a";
		}
		var ghost = level.get().ghost(ghostID);
		return "%d".formatted(ghost.killedIndex());
	}

}