/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.math.Vector2i;
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

public class DS_ActorInfo extends DashboardSection {

    public DS_ActorInfo() {}

    @Override
    public void connect(Game game) {
        dynamicInfo("Pac Name",  pacText(game, (_, pac) -> pac.name()));
        dynamicInfo("Lives",     gameLevelInfo(game, level -> "%d".formatted(level.gameModel().lives().count())));
        dynamicInfo("Movement",  pacText(game, this::actorMovementText));
        dynamicInfo("Tile",      pacText(game, this::actorLocationText));
        dynamicInfo("Power",     pacPowerText(game));
        dynamicInfo("Animation", pacAnimationText(game));
        emptyRow();
        ghostInfo(game, GameModel.RED_GHOST_SHADOW);
        emptyRow();
        ghostInfo(game, GameModel.PINK_GHOST_SPEEDY);
        emptyRow();
        ghostInfo(game, GameModel.CYAN_GHOST_BASHFUL);
        emptyRow();
        ghostInfo(game, GameModel.ORANGE_GHOST_POKEY);
    }

    private void ghostInfo(Game game, byte personality) {
        String name = switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> "Red Ghost";
            case GameModel.PINK_GHOST_SPEEDY  -> "Pink Ghost";
            case GameModel.CYAN_GHOST_BASHFUL -> "Cyan Ghost";
            case GameModel.ORANGE_GHOST_POKEY -> "Orange Ghost";
            default -> "Unknown Ghost";
        };
        dynamicInfo(name,        ghostText(game, this::ghostNameAndState, personality));
        dynamicInfo("Movement",  ghostText(game, this::actorMovementText, personality));
        dynamicInfo("Tile",      ghostText(game, this::actorLocationText, personality));
        dynamicInfo("Animation", ghostText(game, this::ghostAnimationText, personality));
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

    private Supplier<String> pacPowerText(Game game) {
        return () -> game.currentGameContext().optCurrentLevel()
            .map(level -> level.entities().pac())
            .map(this::pacPowerText)
            .orElse(NO_INFO);
    }

    private String pacPowerText(Pac pac) {
        return pac.powerTimer().isRunning()
            ? "Remaining: %s".formatted(ticksToString(pac.powerTimer().remainingTicks()))
            : "No Power";
    }

    private Supplier<String> pacText(Game game, BiFunction<GameLevel, Pac, String> detailInfoSupplier) {
        return gameLevelInfo(game, level -> detailInfoSupplier.apply(level, level.entities().pac()));
    }

    private Supplier<String> pacAnimationText(Game game) {
        return () -> game.currentGameContext().optCurrentLevel().map(level -> {
            final Pac pac = level.entities().pac();
            if (pac.animations() instanceof SpriteAnimationMap<?> spriteAnimations) {
                return spriteAnimations.selectedAnimationID() != null
                    ? "%s:%d".formatted(spriteAnimations.selectedAnimationID(), pac.animations().currentFrame())
                    : NO_INFO;
            }
            return NO_INFO;
        }).orElse(NO_INFO);
    }

    private Supplier<String> ghostText(Game game, BiFunction<GameLevel, Ghost, String> infoSupplier, byte personality) {
        return gameLevelInfo(game, level -> {
            if (!level.entities().ghosts().isEmpty()) {
                return infoSupplier.apply(level, level.ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostNameAndState(GameLevel level, Ghost ghost) {
        return String.format("%s (%s)", ghost.name(), ghostState(level, ghost));
    }

    private String ghostAnimationText(GameLevel level, Ghost ghost) {
        if (ghost.animations() instanceof SpriteAnimationMap<?> spriteAnimations) {
            return spriteAnimations.selectedAnimationID() != null
                ? "%s:%d".formatted(spriteAnimations.selectedAnimationID(), ghost.animations().currentFrame())
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