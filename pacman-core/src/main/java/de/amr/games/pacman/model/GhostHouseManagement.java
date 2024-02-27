/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.House;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkLevelNotNull;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.actors.GhostState.LOCKED;

/**
 * @author Armin Reichert
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
    private final House house;
    private final long pacStarvingTicksLimit;
    private final byte[] globalGhostDotLimits;
    private final byte[] privateGhostDotLimits;
    private final int[] ghostDotCounters;
    private int globalDotCounter;
    private boolean globalDotCounterEnabled;

    public GhostHouseManagement(GameLevel level, House house) {
        checkLevelNotNull(level);
        checkNotNull(house);
        this.level = level;
        this.house = house;

        pacStarvingTicksLimit = level.number() < 5 ? 4 * GameModel.FPS : 3 * GameModel.FPS;
        globalGhostDotLimits = new byte[]{-1, 7, 17, -1};
        privateGhostDotLimits = switch (level.number()) {
            case 1 -> new byte[]{0, 0, 30, 60};
            case 2 -> new byte[]{0, 0, 0, 50};
            default -> new byte[]{0, 0, 0, 0};
        };
        ghostDotCounters = new int[]{0, 0, 0, 0};
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
            level.ghosts(LOCKED)
                .filter(ghost -> ghost.insideHouse(house))
                .findFirst()
                .ifPresent(this::increaseGhostDotCounter);
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
        Ghost candidate = Stream.of(RED_GHOST, PINK_GHOST, CYAN_GHOST, ORANGE_GHOST)
            .map(level::ghost)
            .filter(ghost -> ghost.is(LOCKED))
            .findFirst()
            .orElse(null);

        if (candidate == null) {
            return Optional.empty();
        }
        // Blinky always gets unlocked immediately
        if (candidate.id() == RED_GHOST) {
            return GhostUnlockInfo.of(candidate, "Red ghost gets unlocked immediately");
        }
        // check private dot counter first (if enabled)
        if (!globalDotCounterEnabled && ghostDotCounters[candidate.id()] >= privateGhostDotLimits[candidate.id()]) {
            return GhostUnlockInfo.of(candidate, "Private dot counter at limit (%d)", privateGhostDotLimits[candidate.id()]);
        }
        // check global dot counter
        var globalDotLimit = globalGhostDotLimits[candidate.id()] == -1 ? Integer.MAX_VALUE : globalGhostDotLimits[candidate.id()];
        if (globalDotCounter >= globalDotLimit) {
            return GhostUnlockInfo.of(candidate, "Global dot counter at limit (%d)", globalDotLimit);
        }
        // check Pac-Man starving time
        if (level.pac().starvingTicks() >= pacStarvingTicksLimit) {
            level.pac().endStarving(); // TODO change pac state here?
            Logger.trace("Pac-Man starving timer reset to 0");
            return GhostUnlockInfo.of(candidate, "%s reached starving limit (%d ticks)", level.pac().name(), pacStarvingTicksLimit);
        }
        return Optional.empty();
    }
}