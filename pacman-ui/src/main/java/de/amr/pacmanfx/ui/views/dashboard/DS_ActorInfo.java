/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.actors.MovingActor;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.basics.timer.TickTimer.ticksToString;

public class DS_ActorInfo extends GameDashboardSection {

    public DS_ActorInfo() {
        super(DashboardID.ACTOR_INFO);
    }

    @Override
    public void setGameActionContext(GameAppContext appContext) {
        addDynamicInfo("Pac Name",  supplyPacText(appContext, (_, pac) -> pac.name()));
        addDynamicInfo("Lives",     supplyLivesCount(appContext));
        addDynamicInfo("Movement",  supplyPacText(appContext, this::actorMovementText));
        addDynamicInfo("Tile",      supplyPacText(appContext, this::actorLocationText));
        addDynamicInfo("Power",     supplyPacPowerText(appContext));
        addDynamicInfo("Animation", supplyPacAnimationText(appContext));
        emptyRow();
        addGhostInfo(appContext, GameModel.RED_GHOST_SHADOW);
        emptyRow();
        addGhostInfo(appContext, GameModel.PINK_GHOST_SPEEDY);
        emptyRow();
        addGhostInfo(appContext, GameModel.CYAN_GHOST_BASHFUL);
        emptyRow();
        addGhostInfo(appContext, GameModel.ORANGE_GHOST_POKEY);
    }

    private Supplier<String> supplyLivesCount(GameAppContext appContext) {
        return fnGameLevelInfo(appContext, level -> "%d".formatted(level.gameModel().lives().count()));
    }

    private void addGhostInfo(GameAppContext appContext, byte personality) {
        addDynamicInfo(ghostName(personality), supplyGhostText(appContext, this::ghostNameAndStateText, personality));
        addDynamicInfo("Movement",  supplyGhostText(appContext, this::actorMovementText,  personality));
        addDynamicInfo("Tile",      supplyGhostText(appContext, this::actorLocationText,  personality));
        addDynamicInfo("Animation", supplyGhostText(appContext, this::ghostAnimationText, personality));
    }

    private static String ghostName(byte personality) {
        return switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> "Red Ghost";
            case GameModel.PINK_GHOST_SPEEDY  -> "Pink Ghost";
            case GameModel.CYAN_GHOST_BASHFUL -> "Cyan Ghost";
            case GameModel.ORANGE_GHOST_POKEY -> "Orange Ghost";
            default -> "Unknown Ghost";
        };
    }

    private String actorLocationText(GameLevel level, MovingActor actor) {
        if (actor == null) return NO_INFO;

        final Vector2i tile = actor.computeTile();
        final float offsetX = actor.computeOffsetX();
        final float offsetY = actor.computeOffsetY();

        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            offsetX, offsetY,
            actor.isNewTileEntered() ? " NEW" : "");
    }

    private String actorMovementText(GameLevel level, MovingActor movingActor) {
        if (movingActor == null) return NO_INFO;
        var speed = movingActor.computeSpeed() * GameConstants.SIMULATION_FPS;
        var blocked = !movingActor.moveInfo().moved;
        var reverseText = movingActor.turnBackRequested() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, movingActor.moveDir(), movingActor.wishDir(), reverseText);
    }

    private Supplier<String> supplyPacPowerText(GameAppContext appContext) {
        return () -> appContext.currentGameContext().model().optLevel()
            .map(level -> level.entities().pac())
            .map(this::pacPowerText)
            .orElse(NO_INFO);
    }

    private String pacPowerText(Pac pac) {
        return pac.powerTimer().isRunning()
            ? "Remaining: %s".formatted(ticksToString(pac.powerTimer().remainingTicks()))
            : "No Power";
    }

    private Supplier<String> supplyPacText(GameAppContext appContext, BiFunction<GameLevel, Pac, String> infoSupplier) {
        return fnGameLevelInfo(appContext, level -> infoSupplier.apply(level, level.entities().pac()));
    }

    private Supplier<String> supplyPacAnimationText(GameAppContext appContext) {
        return () -> appContext.currentGameContext().model().optLevel().map(level -> {
            final Pac pac = level.entities().pac();
            if (pac.animations() instanceof SpriteAnimationMap<?> sam && sam.selectedAnimationID() != null) {
                return "%s:%d".formatted(sam.selectedAnimationID(), pac.animations().currentFrame());
            }
            return NO_INFO;
        }).orElse(NO_INFO);
    }

    private Supplier<String> supplyGhostText(GameAppContext appContext,
                                             BiFunction<GameLevel, Ghost, String> infoSupplier, byte personality) {
        return fnGameLevelInfo(appContext, level -> {
            if (!level.entities().ghosts().isEmpty()) {
                return infoSupplier.apply(level, level.ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostNameAndStateText(GameLevel level, Ghost ghost) {
        return String.format("%s (%s)", ghost.name(), ghostStateText(level, ghost));
    }

    private String ghostAnimationText(GameLevel level, Ghost ghost) {
        if (ghost.animations() instanceof SpriteAnimationMap<?> spriteAnimations) {
            return spriteAnimations.selectedAnimationID() != null
                ? "%s:%d".formatted(spriteAnimations.selectedAnimationID(), ghost.animations().currentFrame())
                : NO_INFO;
        }
        return NO_INFO;
    }

    private String ghostStateText(GameLevel level, Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = level.huntingTimer().currentHuntingPhase().name();
        }
        return stateText;
    }
}