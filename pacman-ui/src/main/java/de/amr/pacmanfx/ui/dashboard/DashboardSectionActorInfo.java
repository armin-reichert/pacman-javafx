/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.dashboard;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.TickTimer.ticksToString;

public class DashboardSectionActorInfo extends DashboardSection {

    public DashboardSectionActorInfo(Dashboard dashboard) {
        super(dashboard);
    }

    public void init(GameUI ui) {
        final Supplier<Game> gameSupplier = ui.context()::currentGame;

        addDynamicLabeledValue("Pac Name", supplyPacInfo(gameSupplier, (_, pac) -> pac.name()));
        addDynamicLabeledValue("Lives",    ifGameLevel(gameSupplier, _ -> "%d".formatted(ui.context().currentGame().lifeCount())));
        addDynamicLabeledValue("Movement", supplyPacInfo(gameSupplier, this::actorMovementInfo));
        addDynamicLabeledValue("Tile",     supplyPacInfo(gameSupplier, this::actorLocationInfo));
        addDynamicLabeledValue("Power",    ifGameLevel(gameSupplier, gameLevel -> {
            TickTimer powerTimer = gameLevel.pac().powerTimer();
            return powerTimer.isRunning()
                ? "Remaining: %s".formatted(ticksToString(powerTimer.remainingTicks()))
                : "No Power";
        }));
        addEmptyRow();
        addGhostInfo(gameSupplier, RED_GHOST_SHADOW);
        addEmptyRow();
        addGhostInfo(gameSupplier, PINK_GHOST_SPEEDY);
        addEmptyRow();
        addGhostInfo(gameSupplier, CYAN_GHOST_BASHFUL);
        addEmptyRow();
        addGhostInfo(gameSupplier, ORANGE_GHOST_POKEY);
    }

    private String actorLocationInfo(GameLevel level, MovingActor movingActor) {
        if (movingActor == null) return NO_INFO;
        Vector2i tile = movingActor.tile();
        Vector2f offset = movingActor.offset();
        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            offset.x(), offset.y(),
            movingActor.isNewTileEntered() ? " NEW" : "");
    }

    private String actorMovementInfo(GameLevel level, MovingActor movingActor) {
        if (movingActor == null) return NO_INFO;
        var speed = movingActor.velocity().length() * 60f;
        var blocked = !movingActor.moveInfo().moved;
        var reverseText = movingActor.turnBackRequested() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, movingActor.moveDir(), movingActor.wishDir(), reverseText);
    }

    private Supplier<String> supplyPacInfo(
        Supplier<Game> gameSupplier,
        BiFunction<GameLevel, Pac, String> detailInfoSupplier)
    {
        return ifGameLevel(gameSupplier, level -> detailInfoSupplier.apply(level, level.pac()));
    }

    private void addGhostInfo(Supplier<Game> gameSupplier, byte personality) {
        String name = switch (personality) {
            case RED_GHOST_SHADOW   -> "Red Ghost";
            case PINK_GHOST_SPEEDY  -> "Pink Ghost";
            case CYAN_GHOST_BASHFUL -> "Cyan Ghost";
            case ORANGE_GHOST_POKEY -> "Orange Ghost";
            default -> "Unknown Ghost";
        };
        addDynamicLabeledValue(name,        supplyGhostInfo(gameSupplier, this::ghostNameAndState, personality));
        addDynamicLabeledValue("Movement",  supplyGhostInfo(gameSupplier, this::actorMovementInfo, personality));
        addDynamicLabeledValue("Tile",      supplyGhostInfo(gameSupplier, this::actorLocationInfo, personality));
        addDynamicLabeledValue("Animation", supplyGhostInfo(gameSupplier, this::ghostAnimationInfo, personality));
    }

    private Supplier<String> supplyGhostInfo(
        Supplier<Game> gameSupplier,
        BiFunction<GameLevel, Ghost, String> detailInfoSupplier,
        byte personality)
    {
        return ifGameLevel(gameSupplier, level -> {
            if (level.ghosts().findAny().isPresent()) {
                return detailInfoSupplier.apply(level, level.ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostNameAndState(GameLevel level, Ghost ghost) {
        return String.format("%s (%s)", ghost.name(), ghostState(level, ghost));
    }

    private String ghostAnimationInfo(GameLevel level, Ghost ghost) {
        if (ghost.optAnimationManager().isPresent()
                && ghost.optAnimationManager().get() instanceof SpriteAnimationManager<?> spriteAnimations
                && spriteAnimations.selectedID() != null) {
            return String.valueOf(spriteAnimations.selectedID());
        }
        return NO_INFO;
    }

    private String ghostState(GameLevel level, Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = level.huntingTimer().phase().name();
        }
        return stateText;
    }
}