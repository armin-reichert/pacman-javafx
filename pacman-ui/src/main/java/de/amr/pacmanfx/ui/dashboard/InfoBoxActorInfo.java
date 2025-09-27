/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import java.util.function.Function;
import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.timer.TickTimer.ticksToString;

public class InfoBoxActorInfo extends InfoBox {

    public InfoBoxActorInfo(GameUI ui) {
        super(ui);
    }

    public void init(GameUI ui) {
        addDynamicLabeledValue("Pac Name", supplyPacInfo(Pac::name));
        addDynamicLabeledValue("Lives",    ifGameLevelPresent(gameLevel -> "%d".formatted(ui.gameContext().game().lifeCount())));
        addDynamicLabeledValue("Movement", supplyPacInfo(this::actorMovementInfo));
        addDynamicLabeledValue("Tile",     supplyPacInfo(this::actorLocationInfo));
        addDynamicLabeledValue("Power",    ifGameLevelPresent(gameLevel -> {
            TickTimer powerTimer = gameLevel.pac().powerTimer();
            return powerTimer.isRunning()
                ? "Remaining: %s".formatted(ticksToString(powerTimer.remainingTicks()))
                : "No Power";
        }));
        addEmptyRow();
        addGhostInfo(RED_GHOST_SHADOW);
        addEmptyRow();
        addGhostInfo(PINK_GHOST_SPEEDY);
        addEmptyRow();
        addGhostInfo(CYAN_GHOST_BASHFUL);
        addEmptyRow();
        addGhostInfo(ORANGE_GHOST_POKEY);
    }

    private String actorLocationInfo(MovingActor movingActor) {
        if (movingActor == null) return NO_INFO;
        Vector2i tile = movingActor.tile();
        Vector2f offset = movingActor.offset();
        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            offset.x(), offset.y(),
            movingActor.isNewTileEntered() ? " NEW" : "");
    }

    private String actorMovementInfo(MovingActor movingActor) {
        if (movingActor == null) return NO_INFO;
        var speed = movingActor.velocity().length() * 60f;
        var blocked = !movingActor.moveInfo().moved;
        var reverseText = movingActor.turnBackRequested() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, movingActor.moveDir(), movingActor.wishDir(), reverseText);
    }

    private Supplier<String> supplyPacInfo(Function<Pac, String> detailInfoSupplier) {
        return ifGameLevelPresent(gameLevel -> detailInfoSupplier.apply(gameLevel.pac()));
    }

    private void addGhostInfo(byte personality) {
        String name = switch (personality) {
            case RED_GHOST_SHADOW   -> "Red Ghost";
            case PINK_GHOST_SPEEDY  -> "Pink Ghost";
            case CYAN_GHOST_BASHFUL -> "Cyan Ghost";
            case ORANGE_GHOST_POKEY -> "Orange Ghost";
            default -> "Unknown Ghost";
        };
        addDynamicLabeledValue(name,        supplyGhostInfo(this::ghostNameAndState, personality));
        addDynamicLabeledValue("Movement",  supplyGhostInfo(this::actorMovementInfo, personality));
        addDynamicLabeledValue("Tile",      supplyGhostInfo(this::actorLocationInfo, personality));
        addDynamicLabeledValue("Animation", supplyGhostInfo(this::ghostAnimationInfo, personality));
    }

    private Supplier<String> supplyGhostInfo(Function<Ghost, String> detailInfoSupplier, byte personality) {
        return ifGameLevelPresent(gameLevel -> {
            if (gameLevel.ghosts().findAny().isPresent()) {
                return detailInfoSupplier.apply(gameLevel.ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostNameAndState(Ghost ghost) {
        return String.format("%s (%s)", ghost.name(), ghostState(ghost));
    }

    private String ghostAnimationInfo(Ghost ghost) {
        if (ghost.animationManager().isPresent()
                && ghost.animationManager().get() instanceof SpriteAnimationManager<?> spriteAnimations
                && spriteAnimations.selectedID() != null) {
            return spriteAnimations.selectedID();
        }
        return NO_INFO;
    }

    private String ghostState(Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = ui.gameContext().game().huntingTimer().phase().name();
        }
        return stateText;
    }
}