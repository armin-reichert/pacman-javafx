/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Creature;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.SpriteAnimationSet;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.timer.TickTimer.ticksToString;
import static de.amr.pacmanfx.ui.dashboard.InfoText.NO_INFO;

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
        ghostInfo(RED_GHOST_SHADOW);
        addEmptyRow();
        ghostInfo(PINK_GHOST_SPEEDY);
        addEmptyRow();
        ghostInfo(CYAN_GHOST_BASHFUL);
        addEmptyRow();
        ghostInfo(ORANGE_GHOST_POKEY);
    }

    private Supplier<String> pacInfo(BiFunction<GameModel, Pac, String> fnPacInfo) {
        return ifLevelPresent(level -> level.pac() != null
            ? fnPacInfo.apply(theGame(), level.pac())
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

    private void ghostInfo(byte personality) {
        addLabeledValue(ghostColorName(personality) + " Ghost", fnGhostInfo(this::ghostNameAndState, personality));
        addLabeledValue("Animation",      fnGhostInfo(this::ghostAnimation, personality));
        addLabeledValue("Movement",       fnGhostInfo(this::movementInfo, personality));
        addLabeledValue("Tile",           fnGhostInfo(this::locationInfo, personality));
    }

    private Supplier<String> fnGhostInfo(
        BiFunction<GameModel, Ghost, String> fnGhostInfo, byte personality) {
        return ifLevelPresent(level -> {
            if (level.ghosts().findAny().isPresent()) {
                return fnGhostInfo.apply(theGame(), level.ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostColorName(byte personality) {
        return switch (personality) {
            case RED_GHOST_SHADOW -> "Red";
            case PINK_GHOST_SPEEDY -> "Pink";
            case CYAN_GHOST_BASHFUL -> "Cyan";
            case ORANGE_GHOST_POKEY -> "Orange";
            default -> "";
        };
    }

    protected String ghostNameAndState(GameModel game, Ghost ghost) {
        String name = ghost.name();
        if (ghost.cruiseElroy() > 0) {
            name = "%s (Elroy %d)".formatted(name, ghost.cruiseElroy());
        }
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
            stateText = ghost.level().huntingTimer().phase().name();
        }
        return stateText;
    }
}