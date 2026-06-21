/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.basics.timer.TickTimer.ticksToString;

public class DashboardSectionActorInfo extends DashboardSection {

    public DashboardSectionActorInfo(Dashboard dashboard) {
        super(dashboard);
    }

    @Override
    public void connect(Game game) {
        final Supplier<GameModel> gameSupplier = game.currentGameContext()::model;

        addDynamicLabeledValue("Pac Name", supplyPacInfo(gameSupplier, (_, pac) -> pac.name()));
        addDynamicLabeledValue("Lives",    ifGameLevel(gameSupplier, _ -> "%d".formatted(gameSupplier.get().lives().count())));
        addDynamicLabeledValue("Movement", supplyPacInfo(gameSupplier, this::actorMovementInfo));
        addDynamicLabeledValue("Tile",     supplyPacInfo(gameSupplier, this::actorLocationInfo));
        addDynamicLabeledValue("Power",    ifGameLevel(gameSupplier, gameLevel -> {
            TickTimer powerTimer = gameLevel.entities().pac().powerTimer();
            return powerTimer.isRunning()
                ? "Remaining: %s".formatted(ticksToString(powerTimer.remainingTicks()))
                : "No Power";
        }));
        addEmptyRow();
        addGhostInfo(gameSupplier, GameModel.RED_GHOST_SHADOW);
        addEmptyRow();
        addGhostInfo(gameSupplier, GameModel.PINK_GHOST_SPEEDY);
        addEmptyRow();
        addGhostInfo(gameSupplier, GameModel.CYAN_GHOST_BASHFUL);
        addEmptyRow();
        addGhostInfo(gameSupplier, GameModel.ORANGE_GHOST_POKEY);
    }

    private String actorLocationInfo(GameLevel level, MovingActor actor) {
        if (actor == null) return NO_INFO;

        final Vector2i tile = actor.computeTile();
        final float offsetX = actor.computeOffsetX();
        final float offsetY = actor.computeOffsetY();

        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            offsetX, offsetY,
            actor.isNewTileEntered() ? " NEW" : "");
    }

    private String actorMovementInfo(GameLevel level, MovingActor movingActor) {
        if (movingActor == null) return NO_INFO;
        var speed = movingActor.computeSpeed() * GameClock.DEFAULT_TICKS_PER_SECOND;
        var blocked = !movingActor.moveInfo().moved;
        var reverseText = movingActor.turnBackRequested() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, movingActor.moveDir(), movingActor.wishDir(), reverseText);
    }

    private Supplier<String> supplyPacInfo(
        Supplier<GameModel> gameSupplier,
        BiFunction<GameLevel, Pac, String> detailInfoSupplier)
    {
        return ifGameLevel(gameSupplier, level -> detailInfoSupplier.apply(level, level.entities().pac()));
    }

    private void addGhostInfo(Supplier<GameModel> gameSupplier, byte personality) {
        String name = switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> "Red Ghost";
            case GameModel.PINK_GHOST_SPEEDY  -> "Pink Ghost";
            case GameModel.CYAN_GHOST_BASHFUL -> "Cyan Ghost";
            case GameModel.ORANGE_GHOST_POKEY -> "Orange Ghost";
            default -> "Unknown Ghost";
        };
        addDynamicLabeledValue(name,        supplyGhostInfo(gameSupplier, this::ghostNameAndState, personality));
        addDynamicLabeledValue("Movement",  supplyGhostInfo(gameSupplier, this::actorMovementInfo, personality));
        addDynamicLabeledValue("Tile",      supplyGhostInfo(gameSupplier, this::actorLocationInfo, personality));
        addDynamicLabeledValue("Animation", supplyGhostInfo(gameSupplier, this::ghostAnimationInfo, personality));
    }

    private Supplier<String> supplyGhostInfo(
        Supplier<GameModel> gameSupplier,
        BiFunction<GameLevel, Ghost, String> detailInfoSupplier,
        byte personality)
    {
        return ifGameLevel(gameSupplier, level -> {
            if (!level.entities().ghosts().isEmpty()) {
                return detailInfoSupplier.apply(level, level.ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostNameAndState(GameLevel level, Ghost ghost) {
        return String.format("%s (%s)", ghost.name(), ghostState(level, ghost));
    }

    private String ghostAnimationInfo(GameLevel level, Ghost ghost) {
        if (ghost.animations() instanceof SpriteAnimationMap<?> spriteAnimations) {
            return spriteAnimations.selectedAnimationID() != null
                ? String.valueOf(spriteAnimations.selectedAnimationID())
                : NO_INFO;
        }
        return NO_INFO;
    }

    private String ghostState(GameLevel level, Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = level.huntingTimer().currentHuntingPhase().name();
        }
        return stateText;
    }
}