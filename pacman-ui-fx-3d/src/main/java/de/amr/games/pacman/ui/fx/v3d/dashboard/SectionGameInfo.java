/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dUI;

import static de.amr.games.pacman.lib.TickTimer.ticksToString;

/**
 * Game related settings.
 * 
 * @author Armin Reichert
 */
public class SectionGameInfo extends Section {

	public SectionGameInfo(PacManGames3dUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);

		addInfo("Game scene", () -> gameScene().getClass().getSimpleName());
		//addInfo("", () -> "w=%.0f h=%.0f".formatted(gameScene().root().getWidth(), gameScene().root().getHeight()));
		addInfo("Game State", () -> "%s".formatted(GameController.it().state()));
		addInfo("", () -> "Running:   %s%s".formatted(GameController.it().state().timer().tick(),
				GameController.it().state().timer().isStopped() ? " (STOPPED)" : ""));
		addInfo("", () -> "Remaining: %s".formatted(ticksToString(GameController.it().state().timer().remaining())));

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
		return String.format("%d of %d (%d energizers)", world.foodStorage().uneatenCount(),
				world.foodStorage().totalCount(), world.foodStorage().energizerTiles().count());
	}

	private String fmtGhostSpeed(GameLevel level) {
		return fmtSpeed(level.ghostSpeed);
	}

	private String fmtGhostSpeedFrightened(GameLevel level) {
		return fmtSpeed(level.ghostSpeedFrightened);
	}

	private String fmtGhostSpeedTunnel(GameLevel level) {
		return fmtSpeed(level.ghostSpeedTunnel);
	}

	private String fmtPacSpeed(GameLevel level) {
		return fmtSpeed(level.pacSpeed);
	}

	private String fmtPacSpeedPowered(GameLevel level) {
		return fmtSpeed(level.pacSpeedPowered);
	}

	private String fmtPacPowerSeconds(GameLevel level) {
		return "%d sec".formatted(level.pacPowerSeconds);
	}

	private String fmtNumFlashes(GameLevel level) {
		return "%d".formatted(level.numFlashes);
	}
}