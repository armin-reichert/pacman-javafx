/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.timer.TickTimer.ticksToString;
import static de.amr.pacmanfx.ui.dashboard.InfoText.NO_INFO;

/**
 * @author Armin Reichert
 */
public class InfoBoxActorInfo extends InfoBox {

    public InfoBoxActorInfo(GameContext gameContext) {
        super(gameContext);
    }

    public void init(GameUI ui) {
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
            ? fnPacInfo.apply(gameContext.theGame(), level.pac())
            : NO_INFO);
    }

    private String locationInfo(GameModel game, MovingActor movingActor) {
        Vector2i tile = movingActor.tile();
        Vector2f offset = movingActor.offset();
        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            offset.x(), offset.y(),
            movingActor.isNewTileEntered() ? " NEW" : "");
    }

    private String movementInfo(GameModel game, MovingActor movingActor) {
        var speed = movingActor.velocity().length() * 60f;
        var blocked = !movingActor.moveInfo().moved;
        var reverseText = movingActor.gotReverseCommand() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, movingActor.moveDir(), movingActor.wishDir(), reverseText);
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
                return fnGhostInfo.apply(gameContext.theGame(), level.ghost(personality));
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
        return String.format("%s (%s)", ghost.name(), ghostState(ghost));
    }

    private String ghostAnimation(GameModel game, Ghost ghost) {
        if (ghost.animationMap().isEmpty()) {
            return NO_INFO;
        }
        var animationMap = (SpriteAnimationMap<?>) ghost.animationMap().get();
        return animationMap.selectedAnimationID() != null ? animationMap.selectedAnimationID() : NO_INFO;
    }

    private String ghostState(Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = gameContext.theGame().huntingTimer().phase().name();
        }
        return stateText;
    }
}