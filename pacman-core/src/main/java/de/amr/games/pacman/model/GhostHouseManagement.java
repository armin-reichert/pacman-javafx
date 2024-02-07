/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkLevelNotNull;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.actors.GhostState.LOCKED;

/**
 * @author Armin Reichert
 * 
 * @see <a href="https://pacman.holenet.info/">Pac-Man Dossier by Jamey Pittman</a>
 */
class GhostHouseManagement {

	public static class GhostUnlockInfo {

		private static Optional<GhostUnlockInfo> of(Ghost ghost, String reason, Object... args) {
			return Optional.of(new GhostUnlockInfo(ghost, String.format(reason, args)));
		}

		private final Ghost ghost;
		private final String reason;

		public GhostUnlockInfo(Ghost ghost, String reason) {
			this.ghost = ghost;
			this.reason = reason;
		}

		public Ghost ghost() {
			return ghost;
		}

		public String reason() {
			return reason;
		}
	}

	private final GameLevel level;
	private final long      pacStarvingTicksLimit;
	private final byte[]    globalGhostDotLimits;
	private final byte[]    privateGhostDotLimits;
	private final int[]     ghostDotCounters;
	private int             globalDotCounter;
	private boolean         globalDotCounterEnabled;

	public GhostHouseManagement(GameLevel level) {
		checkLevelNotNull(level);
		this.level = level;
		pacStarvingTicksLimit = level.number() < 5 ? 4 * GameModel.FPS : 3 * GameModel.FPS;
		globalGhostDotLimits = new byte[] { -1, 7, 17, -1 };
		privateGhostDotLimits = switch (level.number()) {
			case 1  -> new byte[] { 0, 0, 30, 60 };
			case 2  -> new byte[] { 0, 0,  0, 50 };
			default -> new byte[] { 0, 0,  0,  0 };
		};
		ghostDotCounters = new int[] { 0, 0, 0, 0 };
		globalDotCounter = 0;
		globalDotCounterEnabled = false;
	}

	public void onFoodFound() {
		if (globalDotCounterEnabled) {
			if (level.ghost(ORANGE_GHOST).is(LOCKED) && globalDotCounter == 32) {
				Logger.trace("{} inside house when counter reached 32", level.ghost(ORANGE_GHOST).name());
				resetGlobalDotCounterAndSetEnabled(false);
			} else {
				globalDotCounter++;
				Logger.trace("Global dot counter = {}", globalDotCounter);
			}
		} else {
			level.ghosts(LOCKED).filter(Creature::insideHouse).findFirst().ifPresent(this::increaseGhostDotCounter);
		}
	}

	public void onPacKilled() {
		resetGlobalDotCounterAndSetEnabled(true);
	}

	private void resetGlobalDotCounterAndSetEnabled(boolean enabled) {
		globalDotCounter = 0;
		globalDotCounterEnabled = enabled;
		Logger.trace("Global dot counter reset to 0 and {}", enabled ? "enabled" : "disabled");
	}

	private void increaseGhostDotCounter(Ghost ghost) {
		ghostDotCounters[ghost.id()]++;
		Logger.trace("{} dot counter = {}", ghost.name(), ghostDotCounters[ghost.id()]);
	}

	public Optional<GhostUnlockInfo> checkIfNextGhostCanLeaveHouse() {
		var unlockedGhost = Stream.of(RED_GHOST, PINK_GHOST, CYAN_GHOST, ORANGE_GHOST)
			.map(level::ghost)
			.filter(ghost -> ghost.is(LOCKED))
			.findFirst().orElse(null);

		if (unlockedGhost == null) {
			return Optional.empty();
		}

		if (!unlockedGhost.insideHouse()) {
			return GhostUnlockInfo.of(unlockedGhost, "Already outside house");
		}
		var id = unlockedGhost.id();
		// check private dot counter first (if enabled)
		if (!globalDotCounterEnabled && ghostDotCounters[id] >= privateGhostDotLimits[id]) {
			return GhostUnlockInfo.of(unlockedGhost, "Private dot counter at limit (%d)", privateGhostDotLimits[id]);
		}
		// check global dot counter
		var globalDotLimit = globalGhostDotLimits[id] == -1 ? Integer.MAX_VALUE : globalGhostDotLimits[id];
		if (globalDotCounter >= globalDotLimit) {
			return GhostUnlockInfo.of(unlockedGhost, "Global dot counter at limit (%d)", globalDotLimit);
		}
		// check Pac-Man starving time
		if (level.pac().starvingTicks() >= pacStarvingTicksLimit) {
			level.pac().endStarving(); // TODO change pac state here?
			Logger.trace("Pac-Man starving timer reset to 0");
			return GhostUnlockInfo.of(unlockedGhost, "%s reached starving limit (%d ticks)", level.pac().name(), pacStarvingTicksLimit);
		}
		return Optional.empty();
	}
}