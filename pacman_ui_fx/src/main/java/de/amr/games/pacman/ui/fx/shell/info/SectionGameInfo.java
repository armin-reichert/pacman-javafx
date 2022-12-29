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

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

import java.util.function.Function;
import java.util.function.Supplier;

import de.amr.games.pacman.model.common.GameLevel;
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
		return String.format("%.2f px/sec", GameModel.SPEED_100_PERCENT_PX * fraction);
	}

	public SectionGameInfo(GameUI ui, String title, int minLabelWidth, Color textColor, Font textFont, Font labelFont) {
		super(ui, title, minLabelWidth, textColor, textFont, labelFont);
		addInfo("Game scene", () -> gameScene().getClass().getSimpleName());
		addInfo("",
				() -> "w=%.0f h=%.0f".formatted(gameScene().fxSubScene().getWidth(), gameScene().fxSubScene().getHeight()));
		addInfo("Game State", () -> "%s".formatted(gc.state()));
		addInfo("", () -> "Running:   %s%s".formatted(gc.state().timer().tick(),
				gc.state().timer().isStopped() ? " (STOPPED)" : ""));
		addInfo("", () -> "Remaining: %s".formatted(ticksToString(gc.state().timer().remaining())));

		addInfo("Hunting Phase", levelInfo(this::fmtHuntingPhase));
		addInfo("", levelInfo(this::fmtHuntingTicksRunning));
		addInfo("", levelInfo(this::fmtHuntingTicksRemaining));

		addInfo("Pellets", levelInfo(this::fmtPelletCount));
		addInfo("Ghost speed", levelInfo(this::fmtGhostSpeed));
		addInfo("- frightened", levelInfo(this::fmtGhostSpeedFrightened));
		addInfo("- in tunnel", levelInfo(this::fmtGhostSpeedTunnel));
		addInfo("Pac-Man speed", levelInfo(this::fmtPlayerSpeed));
		addInfo("- empowered", levelInfo(this::fmtPlayerSpeedPowered));
		addInfo("Frightened time", levelInfo(this::fmtPacPowerSeconds));
		addInfo("Maze flashings", levelInfo(this::fmtNumFlashes));
	}

	private Supplier<String> levelInfo(Function<GameLevel, String> infoSupplier) {
		return () -> {
			if (gc.game().level().isEmpty() || !gc.game().isPlaying()) {
				return "n/a";
			}
			return infoSupplier.apply(gc.game().level().get());
		};
	}

	private String fmtHuntingPhase(GameLevel level) {
		var huntingTimer = level.huntingTimer();
		return "%s #%d%s".formatted(level.currentHuntingPhaseName(),
				level.scatterPhase().isPresent() ? level.scatterPhase().getAsInt() : level.chasingPhase().getAsInt(),
				huntingTimer.isStopped() ? " STOPPED" : "");
	}

	private String fmtHuntingTicksRunning(GameLevel level) {
		return "Running:   %d".formatted(level.huntingTimer().tick());
	}

	private String fmtHuntingTicksRemaining(GameLevel level) {
		return "Remaining: %s".formatted(ticksToString(level.huntingTimer().remaining()));
	}

	private String fmtPelletCount(GameLevel level) {
		var world = level.world();
		return String.format("%d of %d (%d energizers)", world.foodRemaining(),
				world.tiles().filter(world::isFoodTile).count(), world.energizerTiles().count());
	}

	private String fmtGhostSpeed(GameLevel level) {
		return fmtSpeed(level.params().ghostSpeed());
	}

	private String fmtGhostSpeedFrightened(GameLevel level) {
		return fmtSpeed(level.params().ghostSpeedFrightened());
	}

	private String fmtGhostSpeedTunnel(GameLevel level) {
		return fmtSpeed(level.params().ghostSpeedTunnel());
	}

	private String fmtPlayerSpeed(GameLevel level) {
		return fmtSpeed(level.params().playerSpeed());
	}

	private String fmtPlayerSpeedPowered(GameLevel level) {
		return fmtSpeed(level.params().playerSpeedPowered());
	}

	private String fmtPacPowerSeconds(GameLevel level) {
		return "%d sec".formatted(level.params().pacPowerSeconds());
	}

	private String fmtNumFlashes(GameLevel level) {
		return "%d".formatted(level.params().numFlashes());
	}
}