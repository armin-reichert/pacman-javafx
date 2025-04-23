/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.dashboard;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.SpriteAnimationSet;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;
import static de.amr.games.pacman.ui.dashboard.InfoText.NO_INFO;

/**
 * @author Armin Reichert
 */
public class InfoBoxActorInfo extends InfoBox {

    public void init() {
        addLabeledValue("Pac Name", pacInfo((game, pac) -> pac.name()));
        addLabeledValue("Movement", pacInfo(this::movementInfo));
        addLabeledValue("Tile",     pacInfo(this::locationInfo));
        addLabeledValue("Power",    ifLevelPresent(level -> {
            TickTimer powerTimer = level.pac().powerTimer();
            return powerTimer.isRunning()
                ? "Remaining: %s".formatted(ticksToString(powerTimer.remainingTicks()))
                : "No Power";
        }));
        addEmptyRow();
        ghostInfo(RED_GHOST_ID);
        addEmptyRow();
        ghostInfo(PINK_GHOST_ID);
        addEmptyRow();
        ghostInfo(CYAN_GHOST_ID);
        addEmptyRow();
        ghostInfo(ORANGE_GHOST_ID);
    }

    private Supplier<String> pacInfo(BiFunction<GameModel, Pac, String> fnPacInfo) {
        return ifLevelPresent(level -> level.pac() != null
            ? fnPacInfo.apply(THE_GAME_CONTROLLER.game(), level.pac())
            : NO_INFO);
    }

    private String locationInfo(GameModel game, Creature guy) {
        Vector2i tile = guy.tile();
        Vector2f offset = guy.offset();
        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            offset.x(), offset.y(),
            guy.isNewTileEntered() ? " NEW" : "");
    }

    private String movementInfo(GameModel game, Creature guy) {
        var speed = guy.velocity().length() * 60f;
        var blocked = !guy.moveInfo().moved;
        var reverseText = guy.gotReverseCommand() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, guy.moveDir(), guy.wishDir(), reverseText);
    }

    private void ghostInfo(byte ghostID) {
        addLabeledValue(ghostColorName(ghostID) + " Ghost", fnGhostInfo(this::ghostNameAndState, ghostID));
        addLabeledValue("Animation",      fnGhostInfo(this::ghostAnimation, ghostID));
        addLabeledValue("Movement",       fnGhostInfo(this::movementInfo, ghostID));
        addLabeledValue("Tile",           fnGhostInfo(this::locationInfo, ghostID));
    }

    private Supplier<String> fnGhostInfo(
        BiFunction<GameModel, Ghost, String> fnGhostInfo, byte ghostID) {
        return ifLevelPresent(level -> {
            if (level.ghosts().findAny().isPresent()) {
                return fnGhostInfo.apply(THE_GAME_CONTROLLER.game(), level.ghost(ghostID));
            }
            return NO_INFO;
        });
    }

    private String ghostColorName(byte ghostID) {
        return switch (ghostID) {
            case RED_GHOST_ID -> "Red";
            case PINK_GHOST_ID -> "Pink";
            case CYAN_GHOST_ID -> "Cyan";
            case ORANGE_GHOST_ID -> "Orange";
            default -> "";
        };
    }

    protected String ghostNameAndState(GameModel game, Ghost ghost) {
        String name = ghost.name();
        //TODO make this work again
        /*
        if (game instanceof PacManGame pacManGame) {
            if (ghost.id() == GameModel.RED_GHOST && pacManGame.cruiseElroy() > 0) {
                name = "Elroy" + pacManGame.cruiseElroy();
            }
        }
        */
        return String.format("%s (%s)", name, ghostState(game, ghost));
    }

    private String ghostAnimation(GameModel game, Ghost ghost) {
        if (ghost.animations().isEmpty()) {
            return NO_INFO;
        }
        SpriteAnimationSet sa = (SpriteAnimationSet) ghost.animations().get();
        return sa.currentID() != null ? sa.currentID() : NO_INFO;
    }

    private String ghostState(GameModel game, Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = game.huntingTimer().phase().name();
        }
        return stateText;
    }
}