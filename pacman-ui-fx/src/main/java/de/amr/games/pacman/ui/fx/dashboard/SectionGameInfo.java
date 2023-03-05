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

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.ui.fx.shell.GameUI;

/**
 * Game related settings.
 * 
 * @author Armin Reichert
 */
public class SectionGameInfo extends Section {

	public SectionGameInfo(GameUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		addInfo("Game scene", () -> gameScene().getClass().getSimpleName());
		addInfo("",
				() -> "w=%.0f h=%.0f".formatted(gameScene().fxSubScene().getWidth(), gameScene().fxSubScene().getHeight()));
		addInfo("Game State", () -> "%s".formatted(gc.state()));
		addInfo("", () -> "Running:   %s%s".formatted(gc.state().timer().tick(),
				gc.state().timer().isStopped() ? " (STOPPED)" : ""));
		addInfo("", () -> "Remaining: %s".formatted(ticksToString(gc.state().timer().remaining())));

		addInfo("Hunting Phase", ifLevelExists(this::fmtHuntingPhase));
		addInfo("", ifLevelExists(this::fmtHuntingTicksRunning));
		addInfo("", ifLevelExists(this::fmtHuntingTicksRemaining));

		addInfo("Pellets", ifLevelExists(this::fmtPelletCount));
		addInfo("Ghost speed", ifLevelExists(this::fmtGhostSpeed));
		addInfo("- frightened", ifLevelExists(this::fmtGhostSpeedFrightened));
		addInfo("- in tunnel", ifLevelExists(this::fmtGhostSpeedTunnel));
		addInfo("Pac-Man speed", ifLevelExists(this::fmtPacSpeed));
		addInfo("- empowered", ifLevelExists(this::fmtPacSpeedPowered));
		addInfo("Frightened time", ifLevelExists(this::fmtPacPowerSeconds));
		addInfo("Maze flashings", ifLevelExists(this::fmtNumFlashes));
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

	private String fmtPacSpeed(GameLevel level) {
		return fmtSpeed(level.params().pacSpeed());
	}

	private String fmtPacSpeedPowered(GameLevel level) {
		return fmtSpeed(level.params().pacSpeedPowered());
	}

	private String fmtPacPowerSeconds(GameLevel level) {
		return "%d sec".formatted(level.params().pacPowerSeconds());
	}

	private String fmtNumFlashes(GameLevel level) {
		return "%d".formatted(level.params().numFlashes());
	}
}