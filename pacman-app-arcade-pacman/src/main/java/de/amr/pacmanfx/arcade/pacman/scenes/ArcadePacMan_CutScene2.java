/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene2.NailDressState.*;
import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends AbstractGameScene2D {

    public enum NailDressState {
        NAIL, STRETCHED_SMALL, STRETCHED_MEDIUM, STRETCHED_LARGE, RAPTURED
    }

    public static final int TICK_ANIMATION_START = 120;
    public static final int TICK_PAC_MAN_STARTS_RUNNING = TICK_ANIMATION_START + 25;
    public static final int TICK_BLINKY_STARTS_RUNNING  = TICK_ANIMATION_START + 111;
    public static final int TICK_BLINKY_GETS_CAUGHT     = TICK_ANIMATION_START + 194;
    public static final int TICK_DRESS_STRETCHED_SMALL  = TICK_ANIMATION_START + 198;
    public static final int TICK_DRESS_STRETCHED_MEDIUM = TICK_ANIMATION_START + 230;
    public static final int TICK_DRESS_STRETCHED_LARGE  = TICK_ANIMATION_START + 262;
    public static final int TICK_BLINKY_STOPS_MOVING    = TICK_ANIMATION_START + 296;
    public static final int TICK_DRESS_RAPTURES         = TICK_ANIMATION_START + 360;
    public static final int TICK_BLINK_INSPECTS_DAMAGE  = TICK_ANIMATION_START + 420;
    public static final int TICK_ANIMATION_ENDS         = TICK_ANIMATION_START + 508;

    public final int nailX = WorldMap.TS * 14;
    public final int nailY = WorldMap.TS * 19 + 3;
    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;
    public SpriteAnimation nailDressAnimation;

    public ArcadePacMan_CutScene2(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimationContainer = appContext().ui().sprites().animations();
        final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(spriteAnimationContainer);

        blinky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimationContainer, RED_GHOST_SHADOW);

        nailDressAnimation = new SpriteAnimationBuilder()
            .sprites(spriteSheet.findSprites(SpriteID.RED_GHOST_STRETCHED))
            .initiallyStopped()
            .build(spriteAnimationContainer);

        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext gameContext) {
        if (++sceneTick < TICK_ANIMATION_START) {
            return;
        }
        final MovementSystem motor = gameContext.systems().movementSystem;
        final WorldMovementSystem navigator = gameContext.systems().worldMovementSystem;

        switch (sceneTick) {
            case TICK_ANIMATION_START        -> startTheShow();
            case TICK_PAC_MAN_STARTS_RUNNING -> pacManStartsRunning(navigator);
            case TICK_BLINKY_STARTS_RUNNING  -> blinkyStartsRunning(navigator);
            case TICK_BLINKY_GETS_CAUGHT     -> blinkyGetsCaughtOnNail(navigator);
            case TICK_DRESS_STRETCHED_SMALL  -> setDressState(STRETCHED_SMALL);
            case TICK_DRESS_STRETCHED_MEDIUM -> setDressState(STRETCHED_MEDIUM);
            case TICK_DRESS_STRETCHED_LARGE  -> setDressState(STRETCHED_LARGE);
            case TICK_BLINKY_STOPS_MOVING    -> blinkyStopsMoving(navigator);
            case TICK_DRESS_RAPTURES         -> dressRaptures();
            case TICK_BLINK_INSPECTS_DAMAGE  -> blinkyInspectsDamagedDress();
            case TICK_ANIMATION_ENDS         -> endTheShow();
        }
        motor.moveAccelerated(pacMan);
        motor.moveAccelerated(blinky);
    }

    private void blinkyInspectsDamagedDress() {
        blinkyAnimation(CommonAnimationID.BLINKY_DAMAGED).advanceFrame();
    }

    private void startTheShow() {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
        setDressState(NailDressState.NAIL);
    }

    private void endTheShow() {
        blinky.visibility().hide();
        gameState().triggerTimeout();
    }

    private void dressRaptures() {
        setDressState(NailDressState.RAPTURED);
        blinky.position().x -= 4;
        blinky.animations.select(CommonAnimationID.BLINKY_DAMAGED);
    }

    private void blinkyStopsMoving(WorldMovementSystem worldMovementSystem) {
        worldMovementSystem.setSpeed(blinky, 0);
        blinky.animations.stopSelected();
    }

    private void blinkyGetsCaughtOnNail(WorldMovementSystem worldMovementSystem) {
        worldMovementSystem.setSpeed(blinky, 0.09f);
        blinkyAnimation(CommonAnimationID.GHOST_NORMAL).setFrameDurationTicks(32);
    }

    private void blinkyStartsRunning(WorldMovementSystem worldMovementSystem) {
        worldMovementSystem.placeAtTile(blinky, 28, 20, -3, 0);
        worldMovementSystem.setMoveDir(blinky, Direction.LEFT);
        worldMovementSystem.setWishDir(blinky, Direction.LEFT);
        worldMovementSystem.setSpeed(blinky, 1.25f);
        blinky.animations.select(CommonAnimationID.GHOST_NORMAL);
        blinky.animations.playSelected();
        blinky.visibility().show();
    }

    private void pacManStartsRunning(WorldMovementSystem worldMovementSystem) {
        worldMovementSystem.placeAtTile(pacMan, 28, 20);
        worldMovementSystem.setMoveDir(pacMan, Direction.LEFT);
        worldMovementSystem.setSpeed(pacMan, 1.15f);

        pacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
        pacMan.animations.playSelected();

        pacMan.visibility().show();
    }

    private void setDressState(NailDressState state) {
        nailDressAnimation.setFrame(state.ordinal());
    }

    private SpriteAnimation blinkyAnimation(Identifier animationID) {
        return (SpriteAnimation) blinky.animations.animation(animationID);
    }
}