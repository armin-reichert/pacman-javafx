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

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Game related settings.
 * 
 * @author Armin Reichert
 */
public class SectionGameInfo extends Section {

	private static String fmtSpeed(float fraction) {
		return String.format("%.2f px/sec", GameModel.BASE_SPEED * fraction);
	}

	public SectionGameInfo(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);
		addInfo("Game scene", () -> gameScene().getClass().getSimpleName());
		addInfo("",
				() -> "w=%.0f h=%.0f".formatted(gameScene().fxSubScene().getWidth(), gameScene().fxSubScene().getHeight()));
		addInfo("Game State", () -> "%s".formatted(gc.state()));
		addInfo("", () -> "Running:   %s%s".formatted(gc.state().timer().tick(),
				gc.state().timer().isStopped() ? " (STOPPED)" : ""));
		addInfo("", () -> "Remaining: %s".formatted(gc.state().timer().ticksToString(gc.state().timer().remaining())));

		addInfo("Hunting Phase",
				() -> "%s #%d%s".formatted(gc.game().huntingTimer.phaseName(),
						gc.game().huntingTimer.inScatterPhase() ? gc.game().huntingTimer.scatterPhase()
								: gc.game().huntingTimer.chasingPhase(),
						gc.game().huntingTimer.isStopped() ? " STOPPED" : ""));
		addInfo("", () -> "Running:   %d".formatted(gc.game().huntingTimer.tick()));
		addInfo("", () -> "Remaining: %s".formatted(gc.state().timer().ticksToString(gc.game().huntingTimer.remaining())));

		addInfo("Pellets",
				() -> String.format("%d of %d (%d energizers)", game().level().world().foodRemaining(),
						game().level().world().tiles().filter(game().level().world()::isFoodTile).count(),
						game().level().world().energizerTiles().count()));
		addInfo("Ghost speed", () -> fmtSpeed(game().level().ghostSpeed()));
		addInfo("- frightened", () -> fmtSpeed(game().level().ghostSpeedFrightened()));
		addInfo("- in tunnel", () -> fmtSpeed(game().level().ghostSpeedTunnel()));
		addInfo("Pac-Man speed", () -> fmtSpeed(game().level().playerSpeed()));
		addInfo("- empowered", () -> fmtSpeed(game().level().playerSpeedPowered()));
		addInfo("Frightened time", () -> String.format("%d sec", game().level().ghostFrightenedSeconds()));
		addInfo("Maze flashings", () -> game().level().numFlashes());
	}
}