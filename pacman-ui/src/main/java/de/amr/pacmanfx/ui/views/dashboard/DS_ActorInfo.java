/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views.dashboard;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.comp.ghost.GhostState;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.ui.action.core.GameAppContext;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.basics.timer.TickTimer.ticksToString;

public class DS_ActorInfo extends GameDashboardSection {

    public DS_ActorInfo() {
        super(DashboardID.ACTOR_INFO);
    }

    @Override
    public void setGameAppContext(GameAppContext appContext) {
        addDynamicInfo("Pac Name",  supplyPacText(appContext, (_, pac) -> pac.name()));
        addDynamicInfo("Lives",     supplyLivesCount(appContext));
        addDynamicInfo("Movement",  supplyPacText(appContext, this::actorMovementText));
        addDynamicInfo("Tile",      supplyPacText(appContext, this::actorLocationText));
        addDynamicInfo("Power",     supplyPacPowerText(appContext));
        addDynamicInfo("Animation", supplyPacAnimationText(appContext));
        emptyRow();
        addGhostInfo(appContext, GhostPersonality.RED_GHOST_SHADOW);
        emptyRow();
        addGhostInfo(appContext, GhostPersonality.PINK_GHOST_SPEEDY);
        emptyRow();
        addGhostInfo(appContext, GhostPersonality.CYAN_GHOST_BASHFUL);
        emptyRow();
        addGhostInfo(appContext, GhostPersonality.ORANGE_GHOST_POKEY);
    }

    private Supplier<String> supplyLivesCount(GameAppContext appContext) {
        return fnGameLevelInfo(appContext, level -> "%d".formatted(level.gameModel().lifeCount()));
    }

    private void addGhostInfo(GameAppContext appContext, GhostPersonality personality) {
        addDynamicInfo(ghostName(personality), supplyGhostText(appContext, this::ghostNameAndStateText, personality));
        addDynamicInfo("Movement",  supplyGhostText(appContext, this::actorMovementText,  personality));
        addDynamicInfo("Tile",      supplyGhostText(appContext, this::actorLocationText,  personality));
        addDynamicInfo("Animation", supplyGhostText(appContext,
            (_, ghost) -> ghostAnimationText(appContext.currentGameContext().systems().spriteAnim(), ghost),
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

        final WorldNavigationComp worldNavigation = actor.requireComponent(WorldNavigationComp.class);

        final Vector2i tile = WorldNavigationSystem.computeTile(actor);
        final Vector2f tileOffset = WorldNavigationSystem.computeTileOffset(actor);

        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            tileOffset.x(), tileOffset.y(),
            worldNavigation.isNewTileEntered() ? " NEW" : "");
    }

    private String actorMovementText(GameLevel level, GameEntity actor) {
        if (actor == null) return NO_INFO;

        return actor.optComponent(MovementComp.class).map(movement -> {
            final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);
            final float speed = movement.speed() * GameConstants.SIMULATION_FPS;
            final boolean blocked = !navigation.info.moved;
            final String turnbackHint = navigation.isTurnBackRequested() ? "REV!" : "";
            return blocked
                ? "BLOCKED!"
                : "%.2fpx/s %s (%s)%s".formatted(speed, navigation.moveDir(), navigation.wishDir(), turnbackHint);
        }).orElse(NO_INFO);
    }

    private Supplier<String> supplyPacPowerText(GameAppContext appContext) {
        return () -> appContext.currentGameContext().model().optLevel()
            .map(level -> level.entities().pac())
            .map(pac -> pacPowerText(
                appContext.currentGameContext().systems().pacPower(), pac
            ))
            .orElse(NO_INFO);
    }

    private String pacPowerText(PacPowerSystem pacPowerSystem, Pac pac) {
        return pacPowerSystem.isPowerActive(pac)
            ? "Remaining: %s".formatted(ticksToString(pacPowerSystem.powerTicksRemaining(pac)))
            : "No Power";
    }

    private Supplier<String> supplyPacText(GameAppContext appContext, BiFunction<GameLevel, Pac, String> infoSupplier) {
        return fnGameLevelInfo(appContext, level -> infoSupplier.apply(level, level.entities().pac()));
    }

    private Supplier<String> supplyPacAnimationText(GameAppContext appContext) {
        return () -> appContext.currentGameContext().model().optLevel().map(level -> {
            final SpriteAnimSystem animSystem = appContext.currentGameContext().systems().spriteAnim();
            final Pac pac = level.entities().pac();
            if (animSystem.selectedAnimationID(pac) != null) {
                return "%s:%d".formatted(animSystem.selectedAnimationID(pac), animSystem.currentFrame(pac));
            }
            return NO_INFO;
        }).orElse(NO_INFO);
    }

    private Supplier<String> supplyGhostText(
        GameAppContext appContext,
        BiFunction<GameLevel, Ghost, String> infoSupplier, GhostPersonality personality) {

        return fnGameLevelInfo(appContext, level -> {
            if (!level.entities().ghosts().isEmpty()) {
                return infoSupplier.apply(level, level.ghost(personality));
            }
            return NO_INFO;
        });
    }

    private String ghostNameAndStateText(GameLevel level, Ghost ghost) {
        return "%s (%s)".formatted(ghost.name(), ghostStateText(level, ghost));
    }

    private String ghostAnimationText(SpriteAnimSystem animSystem, Ghost ghost) {
        return animSystem.selectedAnimationID(ghost) != null
            ? "%s:%d".formatted(animSystem.selectedAnimationID(ghost), animSystem.currentFrame(ghost))
            : NO_INFO;
    }

    private String ghostStateText(GameLevel level, Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = level.huntingRules().currentHuntingPhase().name();
        }
        return stateText;
    }
}