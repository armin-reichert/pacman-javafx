/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.GameContext;

import java.util.Map;

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        addLabeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));
        addLabeledValue("Demo Level", ifLevelPresent(gameScene -> context.game().isDemoLevel() ? "Yes" : "No"));
        addLabeledValue("Game State", () -> "%s".formatted(context.gameState()));
        addLabeledValue("State Timer", this::stateTimerInfo);
        addLabeledValue("Level Number", ifLevelPresent(level -> "%d".formatted(level.number)));
        addLabeledValue("World Map", ifLevelPresent(level -> {
            String url = level.world().map().url().toString();
            return url.substring(url.lastIndexOf("/") + 1);
        }));
        addLabeledValue("Color Scheme", ifLevelPresent(level -> {
            if (context.gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
                return InfoText.NO_INFO;
            }
            Map<String, String> mapColorScheme = level.mapConfig().colorScheme();
            if (mapColorScheme == null) {
                return InfoText.NO_INFO;
            }
            return "fill/stroke/food: %s/%s/%s".formatted(
                mapColorScheme.get("fill"), mapColorScheme.get("stroke"), mapColorScheme.get("pellet"));
        }));

        addLabeledValue("Lives",           ifLevelPresent(level -> "%d".formatted(context.game().lives())));

        addLabeledValue("Hunting Phase",   ifLevelPresent(this::fmtHuntingPhase));
        addLabeledValue("",                ifLevelPresent(this::fmtHuntingTicksRunning));
        addLabeledValue("",                ifLevelPresent(this::fmtHuntingTicksRemaining));

        addLabeledValue("Pac-Man speed",   ifLevelPresent(this::fmtPacNormalSpeed));
        addLabeledValue("- empowered",     ifLevelPresent(this::fmtPacSpeedPowered));
        addLabeledValue("Power Duration",  ifLevelPresent(this::fmtPacPowerTime));
        addLabeledValue("Pellets",         ifLevelPresent(this::fmtPelletCount));
        addLabeledValue("Ghost speed",     ifLevelPresent(this::fmtGhostAttackSpeed));
        addLabeledValue("- frightened",    ifLevelPresent(this::fmtGhostSpeedFrightened));
        addLabeledValue("- in tunnel",     ifLevelPresent(this::fmtGhostSpeedTunnel));
        addLabeledValue("Maze flashings",  ifLevelPresent(this::fmtNumFlashes));
    }

    private String stateTimerInfo() {
        TickTimer t = context.gameState().timer();
        boolean indefinite = t.durationTicks() == TickTimer.INDEFINITE;
        if (t.isStopped()) {
            return "Stopped at tick %s of %s".formatted(t.tickCount(), indefinite ? "∞" : t.durationTicks());
        }
        if (indefinite) {
            return "Tick %s of ∞".formatted(t.tickCount());
        }
        return "Tick %d of %d. Remaining: %d".formatted(t.tickCount(), t.durationTicks(), t.remainingTicks());
    }

    private String fmtHuntingPhase(GameLevel level) {
        var game = context.game();
        HuntingControl huntingControl = game.huntingControl();
        return "%s #%d%s".formatted(
            huntingControl.phaseType().name(),
            huntingControl.phaseType() == HuntingControl.PhaseType.CHASING
                ? huntingControl.currentChasingPhaseIndex().orElse(42)
                : huntingControl.currentScatterPhaseIndex().orElse(42),
            huntingControl.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(context.game().huntingControl().currentTick());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(context.game().huntingControl().remaining()));
    }

    private String fmtPelletCount(GameLevel level) {
        return "%d of %d (%d energizers)".formatted(
                level.world().uneatenFoodCount(),
                level.world().totalFoodCount(),
                level.world().energizerTiles().count()
        );
    }

    private String fmtGhostAttackSpeed(GameLevel level) {
        // use Pinky because Blinky could be in Elroy mode
        Ghost pinky = level.ghost(GameModel.PINK_GHOST);
        return (pinky != null) ? "%.4f px/s".formatted(context.game().ghostAttackSpeed(pinky) * 60) : InfoText.NO_INFO;
    }

    private String fmtGhostSpeedFrightened(GameLevel level) {
        Ghost blinky = level.ghost(GameModel.RED_GHOST);
        return (blinky != null) ? "%.4f px/s".formatted(context.game().ghostFrightenedSpeed(blinky) * 60) : InfoText.NO_INFO;
    }

    private String fmtGhostSpeedTunnel(GameLevel level) {
        Ghost blinky = level.ghost(GameModel.RED_GHOST);
        return (blinky != null) ? "%.4f px/s".formatted(context.game().ghostTunnelSpeed(blinky) * 60) : InfoText.NO_INFO;
    }

    private String fmtPacNormalSpeed(GameLevel level) {
        return "%.4f px/s".formatted(context.game().pacNormalSpeed() * 60);
    }

    private String fmtPacSpeedPowered(GameLevel level) {
        return "%.4f px/s".formatted(context.game().pacPowerSpeed() * 60);
    }

    private String fmtPacPowerTime(GameLevel unused) {
        return "%.2f sec (%d ticks)".formatted(context.game().pacPowerTicks() / 60f, context.game().pacPowerTicks());
    }

    private String fmtNumFlashes(GameLevel level) {
        return "%d".formatted(context.game().numFlashes());
    }
}