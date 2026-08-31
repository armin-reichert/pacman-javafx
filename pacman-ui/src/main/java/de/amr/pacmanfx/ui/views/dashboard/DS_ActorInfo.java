/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.Named;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.basics.timer.TickTimer.ticksToString;

public class DS_ActorInfo extends GameDashboardSection {

    public DS_ActorInfo() {
        super(DashboardID.ACTOR_INFO);
    }

    @Override
    public void setGameApp(GameAppContext app) {
        addDynamicInfo("Pac Name",  supplyPacStateAndName(app));
        addDynamicInfo("Lives",     supplyLivesCount(app));
        addDynamicInfo("Movement",  supplyPacText(app, this::actorMovementText));
        addDynamicInfo("Tile",      supplyPacText(app, this::actorLocationText));
        addDynamicInfo("Power",     supplyPacPowerText(app));
        addDynamicInfo("Animation", supplyPacAnimationText(app));
        emptyRow();
        addGhostInfo(app, GhostPersonality.RED_GHOST_SHADOW);
        emptyRow();
        addGhostInfo(app, GhostPersonality.PINK_GHOST_SPEEDY);
        emptyRow();
        addGhostInfo(app, GhostPersonality.CYAN_GHOST_BASHFUL);
        emptyRow();
        addGhostInfo(app, GhostPersonality.ORANGE_GHOST_POKEY);
    }

    private Supplier<String> supplyPacStateAndName(GameAppContext app) {
        return () -> app.game().session().optLevel()
            .map(level -> level.entities().pac())
            .map(pac -> "%s (%s)".formatted(pac.name(), pac.state().enumValue()))
            .orElse(NO_INFO);
    }

    private Supplier<?> supplyLivesCount(GameAppContext appContext) {
        return fnLevelInfo(appContext, _ -> {
            final GameSession session = appContext.game().session();
            return session.numLives();
        });
    }

    private void addGhostInfo(GameAppContext appContext, GhostPersonality personality) {
        addDynamicInfo(ghostName(personality), supplyGhostText(appContext, this::ghostNameAndStateText, personality));
        addDynamicInfo("Movement",  supplyGhostText(appContext, this::actorMovementText,  personality));
        addDynamicInfo("Tile",      supplyGhostText(appContext, this::actorLocationText,  personality));
        addDynamicInfo("Animation", supplyGhostText(appContext,
            (_, ghost) -> ghostAnimationText(appContext.game().variant().systems().actorSpriteAnimController(), ghost),
            personality));
    }

    private static String ghostName(GhostPersonality personality) {
        return switch (personality) {
            case RED_GHOST_SHADOW   -> "Red Ghost";
            case PINK_GHOST_SPEEDY  -> "Pink Ghost";
            case CYAN_GHOST_BASHFUL -> "Cyan Ghost";
            case ORANGE_GHOST_POKEY -> "Orange Ghost";
        };
    }

    private String actorLocationText(GameLevel level, GameEntity actor) {
        if (actor == null) return NO_INFO;

        final WorldNavigationComp worldNavigation = actor.reqComp(WorldNavigationComp.class);

        final Vector2i tile = actor.pos().tile();
        final Vector2i tileOffset = actor.pos().offset();

        return "(%2d,%2d) offset=(%2d,%2d)%s".formatted(
            tile.x(), tile.y(),
            tileOffset.x(), tileOffset.y(),
            worldNavigation.isNewTileEntered() ? " NEW" : "");
    }

    private String actorMovementText(GameLevel level, GameEntity actor) {
        if (actor == null) return NO_INFO;

        return actor.optComp(MovementComp.class).map(movement -> {
            final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
            final float speed = movement.speed() * GameConstants.SIMULATION_FPS;
            final boolean moved = !navigation.info().moved;
            final String turnbackText = navigation.isTurnBackRequested() ? "REV!" : "";
            return moved
                ? "STANDING STILL"
                : "%.2fpx/s %s (%s)%s".formatted(speed, navigation.moveDir(), navigation.wishDir(), turnbackText);
        }).orElse(NO_INFO);
    }

    private Supplier<String> supplyPacPowerText(GameAppContext appContext) {
        return () -> appContext.game().session().optLevel()
            .map(level -> level.entities().pac())
            .map(this::pacPowerText)
            .orElse(NO_INFO);
    }

    private String pacPowerText(Pac pac) {
        return pac.power().isActive()
            ? "Remaining: %s".formatted(ticksToString(pac.power().ticksRemaining()))
            : "No Power";
    }

    private Supplier<?> supplyPacText(GameAppContext appContext, BiFunction<GameLevel, Pac, String> infoSupplier) {
        return fnLevelInfo(appContext, level -> infoSupplier.apply(level, level.entities().pac()));
    }

    private Supplier<?> supplyGhostText(
        GameAppContext appContext,
        BiFunction<GameLevel, Ghost, String> infoSupplier, GhostPersonality personality) {

        return fnLevelInfo(appContext, level -> {
            if (!level.entities().ghosts().isEmpty()) {
                return infoSupplier.apply(level, level.entities().ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostNameAndStateText(GameLevel level, Ghost ghost) {
        return "%s (%s)".formatted(ghost.name(), ghostStateText(level, ghost));
    }

    private Supplier<String> supplyPacAnimationText(GameAppContext app) {
        return () -> app.game().session().optLevel().map(level -> {
            final ActorSpriteAnimController animSystem = app.game().variant().systems().actorSpriteAnimController();
            final Pac pac = level.entities().pac();
            final boolean stopped = pac.animation().isStopped();
            final boolean locked = pac.animation().isLocked();
            String statusText = "";
            if (locked) statusText += " locked";
            if (stopped) statusText += " stopped";
            if (animSystem.selectedAnimationID(pac) != null) {
                return "%s:%d%s".formatted(
                    animSystem.selectedAnimationID(pac),
                    animSystem.currentFrame(pac),
                    statusText);
            }
            return NO_INFO;
        }).orElse(NO_INFO);
    }

    private String ghostAnimationText(ActorSpriteAnimController animSystem, Ghost ghost) {
        final Named id = animSystem.selectedAnimationID(ghost);
        if (id == null) {
            return NO_INFO;
        }
        if (animSystem.animation(ghost, id) instanceof SpriteAnimation spriteAnimation) {
            final boolean stopped = ghost.animation().isStopped();
            final boolean locked = ghost.animation().isLocked();
            String statusText = "";
            if (locked) statusText += " locked";
            if (stopped) statusText += " stopped";
            return "%s:%d%s".formatted(id, animSystem.currentFrame(ghost), statusText);
        }
        return NO_INFO;
    }

    private String ghostStateText(GameLevel level, Ghost ghost) {
        var stateText = ghost.state().enumValue() != null ? ghost.state().enumValue().name() : "undefined";
        if (ghost.state().enumValue() == GhostState.HUNTING_PAC) {
            stateText = level.huntingTimerStrategy().currentHuntingPhase().name();
        }
        return stateText;
    }
}