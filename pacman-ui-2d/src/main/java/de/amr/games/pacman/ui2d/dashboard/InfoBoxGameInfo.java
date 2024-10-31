/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.LevelData;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameContext;

import java.util.Map;

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        labeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));
        labeledValue("Demo Level", ifGameScenePresent(gameScene -> context.game().isDemoLevel() ? "Yes" : "No"));
        labeledValue("Game State", () -> "%s".formatted(context.gameState()));
        labeledValue("State Timer", this::stateTimerInfo);
        labeledValue("Level Number", ifLevelPresent(level -> "%d".formatted(context.game().currentLevelNumber())));
        labeledValue("World Map", ifWorldPresent(world -> {
            String url = world.map().url().toString();
            return url.substring(url.lastIndexOf("/") + 1);
        }));
        labeledValue("Color Scheme",            ifWorldPresent(world -> {
            if (context.gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
                return InfoText.NO_INFO;
            }
            TengenMsPacManGame game = (TengenMsPacManGame) context.game();
            Map<String, String> mapColorScheme = game.currentMapColorScheme();
            if (mapColorScheme == null) {
                return InfoText.NO_INFO;
            }
            return "fill/stroke/food: %s %s %s".formatted(
                mapColorScheme.get("fill"), mapColorScheme.get("stroke"), mapColorScheme.get("pellet"));
        }));

        labeledValue("Lives",           ifLevelPresent(level -> "%d".formatted(context.game().lives())));

        labeledValue("Hunting Phase",   ifLevelPresent(this::fmtHuntingPhase));
        labeledValue("",                ifLevelPresent(this::fmtHuntingTicksRunning));
        labeledValue("",                ifLevelPresent(this::fmtHuntingTicksRemaining));

        labeledValue("Pac-Man speed",   ifLevelPresent(this::fmtPacNormalSpeed));
        labeledValue("- empowered",     ifLevelPresent(this::fmtPacSpeedPowered));
        labeledValue("Power Duration",  ifLevelPresent(this::fmtPacPowerTime));
        labeledValue("Pellets",         ifWorldPresent(this::fmtPelletCount));
        labeledValue("Ghost speed",     ifLevelPresent(this::fmtGhostAttackSpeed));
        labeledValue("- frightened",    ifLevelPresent(this::fmtGhostSpeedFrightened));
        labeledValue("- in tunnel",     ifLevelPresent(this::fmtGhostSpeedTunnel));
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

    private String fmtHuntingPhase(LevelData level) {
        var game = context.game();
        HuntingControl huntingControl = game.huntingControl();
        return "%s #%d%s".formatted(
            huntingControl.phaseType().name(),
            huntingControl.phaseType() == HuntingControl.PhaseType.CHASING
                ? huntingControl.currentChasingPhaseIndex().orElse(42)
                : huntingControl.currentScatterPhaseIndex().orElse(42),
            huntingControl.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(LevelData level) {
        return "Running:   %d".formatted(context.game().huntingControl().currentTick());
    }

    private String fmtHuntingTicksRemaining(LevelData level) {
        return "Remaining: %s".formatted(ticksToString(context.game().huntingControl().remaining()));
    }

    private String fmtPelletCount(GameWorld world) {
        return "%d of %d (%d energizers)".formatted(world.uneatenFoodCount(), world.totalFoodCount(), world.energizerTiles().count());
    }

    private String fmtGhostAttackSpeed(LevelData level) {
        // use Pinky because Blinky could be in Elroy mode
        Ghost pinky = context.game().ghost(GameModel.PINK_GHOST);
        if (pinky != null) {
            return "%.4f px/s (%d%%)".formatted(context.game().ghostAttackSpeed(pinky) * 60f, level.pacSpeedPercentage());
        }
        return InfoText.NO_INFO;
    }

    private String fmtGhostSpeedFrightened(LevelData level) {
        Ghost blinky = context.game().ghost(GameModel.RED_GHOST);
        if (blinky != null) {
            return "%.4f px/s (%d%%)".formatted(context.game().ghostFrightenedSpeed(blinky) * 60f, level.pacSpeedPercentage());
        }
        return InfoText.NO_INFO;
    }

    private String fmtGhostSpeedTunnel(LevelData level) {
        Ghost blinky = context.game().ghost(GameModel.RED_GHOST);
        if (blinky != null) {
            return "%.4f px/s (%d%%)".formatted(context.game().ghostTunnelSpeed(blinky) * 60f, level.pacSpeedPercentage());
        }
        return InfoText.NO_INFO;
    }

    private String fmtPacNormalSpeed(LevelData level) {
        return "%.4f px/s (%d%%)".formatted(context.game().pacNormalSpeed() * 60f, level.pacSpeedPercentage());
    }

    private String fmtPacSpeedPowered(LevelData level) {
        return "%.4f px/s (%d%%)".formatted(context.game().pacPowerSpeed() * 60f, level.pacSpeedPoweredPercentage());
    }

    private String fmtPacPowerTime(LevelData unused) {
        return "%.2f sec (%d ticks)".formatted(context.game().pacPowerTicks() / 60f, context.game().pacPowerTicks());
    }

    private String fmtNumFlashes(LevelData level) {
        return "%d".formatted(level.numFlashes());
    }
}