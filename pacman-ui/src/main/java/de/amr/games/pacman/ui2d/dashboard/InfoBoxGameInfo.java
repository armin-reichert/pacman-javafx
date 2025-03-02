/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.uilib.WorldMapColoring;
import javafx.scene.paint.Color;

import java.util.Map;

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

/**
 * @author Armin Reichert
 */
public class InfoBoxGameInfo extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        addLabeledValue("Game Scene", ifGameScenePresent(gameScene -> gameScene.getClass().getSimpleName()));
        addLabeledValue("Game State", () -> "%s".formatted(context.gameState()));
        addLabeledValue("State Timer", this::stateTimerInfo);
        addLabeledValue("Level Number", ifLevelPresent(level -> "%d".formatted(level.number)));
        addLabeledValue("Demo Level", ifLevelPresent(gameScene -> context.game().isDemoLevel() ? "Yes" : "No"));
        addLabeledValue("World Map", ifLevelPresent(level -> {
            String url = level.world().map().url().toString();
            return url.substring(url.lastIndexOf("/") + 1);
        }));
        addLabeledValue("Fill/Stroke/Pellet", ifLevelPresent(level -> {
            WorldMap worldMap = level.world().map();
            if (worldMap.hasConfigValue("nesColorScheme")) {
                var ncs = (NES_ColorScheme) worldMap.getConfigValue("nesColorScheme");
                return "%s %s %s".formatted(ncs.fillColor(), ncs.strokeColor(), ncs.pelletColor());
            } else if (worldMap.hasConfigValue("colorMap")) {
                // Pac-Man XXL game
                Map<String, String> colorMap = worldMap.getConfigValue("colorMap");
                return "%s %s %s".formatted(colorMap.get("fill"), colorMap.get("stroke"), colorMap.get("pellet"));
            } else if (worldMap.hasConfigValue("colorMapIndex")) {
                WorldMapColoring coloring = context.gameConfiguration().worldMapColoring(worldMap);
                return formatColorMap(coloring.fill(), coloring.stroke(), coloring.pellet());
            } else {
                return InfoText.NO_INFO;
            }
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

    private String formatColorMap(Color fillColor, Color strokeColor, Color pelletColor) {
        // chop opacity
        String fill   = "#" + fillColor.toString().substring(2, 8);
        String stroke = "#" + strokeColor.toString().substring(2, 8);
        String pellet = "#" + pelletColor.toString().substring(2, 8);
        return "%s %s %s".formatted(fill, stroke, pellet);
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
        HuntingTimer huntingControl = game.huntingControl();
        return "%s #%d%s".formatted(
            huntingControl.phaseType().name(),
            huntingControl.phaseType() == HuntingTimer.PhaseType.CHASING
                ? huntingControl.currentChasingPhaseIndex().orElse(42)
                : huntingControl.currentScatterPhaseIndex().orElse(42),
            huntingControl.isStopped() ? " STOPPED" : "");
    }

    private String fmtHuntingTicksRunning(GameLevel level) {
        return "Running:   %d".formatted(context.game().huntingControl().tickCount());
    }

    private String fmtHuntingTicksRemaining(GameLevel level) {
        return "Remaining: %s".formatted(ticksToString(context.game().huntingControl().remainingTicks()));
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
        return "%d".formatted(level.numFlashes());
    }
}