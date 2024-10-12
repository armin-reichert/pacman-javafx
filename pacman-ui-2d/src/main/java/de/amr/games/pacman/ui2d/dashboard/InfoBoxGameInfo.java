/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.GameContext;

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        labeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));
        labeledValue("Game State", () -> "%s".formatted(context.gameState()));
        labeledValue("State Timer", this::stateTimerInfo);
        labeledValue("Level Number", ifLevelPresent(level -> "%d".formatted(context.game().levelNumber())));
        labeledValue("World Map", ifWorldPresent(world -> {
            String url = world.map().url().toString();
            return url.substring(url.lastIndexOf("/") + 1);
        }));

        labeledValue("Lives",           ifLevelPresent(level -> "%d".formatted(context.game().lives())));

        labeledValue("Hunting Phase",   ifLevelPresent(this::fmtHuntingPhase));
        labeledValue("",                ifLevelPresent(this::fmtHuntingTicksRunning));
        labeledValue("",                ifLevelPresent(this::fmtHuntingTicksRemaining));

        labeledValue("Pac-Man speed",   ifLevelPresent(this::fmtPacSpeed));
        labeledValue("- empowered",     ifLevelPresent(this::fmtPacSpeedPowered));
        labeledValue("Pellets",         ifWorldPresent(this::fmtPelletCount));
        labeledValue("Ghost speed",     ifLevelPresent(this::fmtGhostSpeed));
        labeledValue("- frightened",    ifLevelPresent(this::fmtGhostSpeedFrightened));
        labeledValue("- in tunnel",     ifLevelPresent(this::fmtGhostSpeedTunnel));
        labeledValue("Frightened time", ifLevelPresent(this::fmtPacPowerSeconds));
        labeledValue("Maze flashings",  ifLevelPresent(this::fmtNumFlashes));
    }

    private String stateTimerInfo() {
        TickTimer t = context.gameState().timer();
        boolean indefinite = t.duration() == TickTimer.INDEFINITE;
        if (t.isStopped()) {
            return "Stopped at tick %s of %s".formatted(t.currentTick(), indefinite ? "∞" : t.duration());
        }
        if (indefinite) {
            return "Tick %s of ∞".formatted(t.currentTick());
        }
        return "Tick %d of %d. Remaining: %d".formatted(t.currentTick(), t.duration(), t.remaining());
    }

    private String fmtHuntingPhase(GameLevel level) {
        var game = context.game();
        var huntingTimer = game.huntingTimer();
        return "%s #%d%s".formatted(
            game.isChasingPhase(game.huntingPhaseIndex()) ? "Chasing" : "Scattering",
            game.scatterPhase().orElse(game.chasingPhase().orElse(42)),
            huntingTimer.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(context.game().huntingTimer().currentTick());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(context.game().huntingTimer().remaining()));
    }

    private String fmtPelletCount(GameWorld world) {
        return "%d of %d (%d energizers)".formatted(world.uneatenFoodCount(), world.totalFoodCount(), world.energizerTiles().count());
    }

    private String fmtGhostSpeed(GameLevel level) {
        return fmtSpeed(level.ghostSpeedPct());
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        return fmtSpeed(level.ghostSpeedFrightenedPct());
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        return fmtSpeed(level.ghostSpeedTunnelPct());
    }

    private String fmtPacSpeed(GameLevel level) {
        return fmtSpeed(level.pacSpeedPercentage());
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return fmtSpeed(level.pacSpeedPoweredPercentage());
    }

    private String fmtPacPowerSeconds(GameLevel level) {
        return "%d sec".formatted(level.pacPowerSeconds());
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(level.numFlashes());
    }
}