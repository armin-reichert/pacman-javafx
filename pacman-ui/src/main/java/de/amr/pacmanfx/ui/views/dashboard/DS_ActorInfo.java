/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.actors.MovingActor;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.uilib.rendering.SpriteAnimationMap;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.basics.timer.TickTimer.ticksToString;

public class DS_ActorInfo extends GameDashboardSection {

    public DS_ActorInfo() {
        super(DashboardID.ACTOR_INFO);
    }

    @Override
    public void connect(PacManGamesCollection game) {
        addDynamicInfo("Pac Name",  supplyPacText(game, (_, pac) -> pac.name()));
        addDynamicInfo("Lives",     supplyLivesCount(game));
        addDynamicInfo("Movement",  supplyPacText(game, this::actorMovementText));
        addDynamicInfo("Tile",      supplyPacText(game, this::actorLocationText));
        addDynamicInfo("Power",     supplyPacPowerText(game));
        addDynamicInfo("Animation", supplyPacAnimationText(game));
        emptyRow();
        addGhostInfo(game, GameModel.RED_GHOST_SHADOW);
        emptyRow();
        addGhostInfo(game, GameModel.PINK_GHOST_SPEEDY);
        emptyRow();
        addGhostInfo(game, GameModel.CYAN_GHOST_BASHFUL);
        emptyRow();
        addGhostInfo(game, GameModel.ORANGE_GHOST_POKEY);
    }

    private Supplier<String> supplyLivesCount(PacManGamesCollection game) {
        return supplyGameLevelInfo(game, level -> "%d".formatted(level.gameModel().lives().count()));
    }

    private void addGhostInfo(PacManGamesCollection game, byte personality) {
        addDynamicInfo(ghostName(personality), supplyGhostText(game, this::ghostNameAndStateText, personality));
        addDynamicInfo("Movement",  supplyGhostText(game, this::actorMovementText,  personality));
        addDynamicInfo("Tile",      supplyGhostText(game, this::actorLocationText,  personality));
        addDynamicInfo("Animation", supplyGhostText(game, this::ghostAnimationText, personality));
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
        var speed = movingActor.computeSpeed() * GameClock.DEFAULT_TICKS_PER_SECOND;
        var blocked = !movingActor.moveInfo().moved;
        var reverseText = movingActor.turnBackRequested() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, movingActor.moveDir(), movingActor.wishDir(), reverseText);
    }

    private Supplier<String> supplyPacPowerText(PacManGamesCollection game) {
        return () -> game.context().model().optLevel()
            .map(level -> level.entities().pac())
            .map(this::pacPowerText)
            .orElse(NO_INFO);
    }

    private String pacPowerText(Pac pac) {
        return pac.powerTimer().isRunning()
            ? "Remaining: %s".formatted(ticksToString(pac.powerTimer().remainingTicks()))
            : "No Power";
    }

    private Supplier<String> supplyPacText(PacManGamesCollection game, BiFunction<GameLevel, Pac, String> infoSupplier) {
        return supplyGameLevelInfo(game, level -> infoSupplier.apply(level, level.entities().pac()));
    }

    private Supplier<String> supplyPacAnimationText(PacManGamesCollection game) {
        return () -> game.context().model().optLevel().map(level -> {
            final Pac pac = level.entities().pac();
            if (pac.animations() instanceof SpriteAnimationMap<?> sam && sam.selectedAnimationID() != null) {
                return "%s:%d".formatted(sam.selectedAnimationID(), pac.animations().currentFrame());
            }
            return NO_INFO;
        }).orElse(NO_INFO);
    }

    private Supplier<String> supplyGhostText(PacManGamesCollection game, BiFunction<GameLevel, Ghost, String> infoSupplier, byte personality) {
        return supplyGameLevelInfo(game, level -> {
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