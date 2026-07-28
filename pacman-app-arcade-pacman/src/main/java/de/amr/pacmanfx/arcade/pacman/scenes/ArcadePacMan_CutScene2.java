/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameSystems;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
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
        final var factory = new ArcadePacMan_ActorFactory();

        pacMan = factory.createPacMan();
        pacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimationContainer));

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

        final GameSystems sys = gameContext.systems();

        switch (sceneTick) {
            case TICK_ANIMATION_START        -> startTheShow();
            case TICK_PAC_MAN_STARTS_RUNNING -> pacManStartsRunning(sys);
            case TICK_BLINKY_STARTS_RUNNING  -> blinkyStartsRunning(sys);
            case TICK_BLINKY_GETS_CAUGHT     -> blinkyGetsCaughtOnNail(sys);
            case TICK_DRESS_STRETCHED_SMALL  -> setDressState(STRETCHED_SMALL);
            case TICK_DRESS_STRETCHED_MEDIUM -> setDressState(STRETCHED_MEDIUM);
            case TICK_DRESS_STRETCHED_LARGE  -> setDressState(STRETCHED_LARGE);
            case TICK_BLINKY_STOPS_MOVING    -> blinkyStopsMoving(sys);
            case TICK_DRESS_RAPTURES         -> dressRaptures(sys);
            case TICK_BLINK_INSPECTS_DAMAGE  -> blinkyInspectsDamagedDress(sys.spriteAnim);
            case TICK_ANIMATION_ENDS         -> endTheShow();
        }

        sys.motor.moveAccelerated(pacMan);
        sys.motor.moveAccelerated(blinky);
    }

    private void blinkyInspectsDamagedDress(SpriteAnimSystem animSystem) {
        animSystem.advanceFrame(blinky);
    }

    private void startTheShow() {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
        setDressState(NailDressState.NAIL);
    }

    private void endTheShow() {
        blinky.visibility().hide();
        gameState().triggerTimeout();
    }

    private void dressRaptures(GameSystems sys) {
        blinky.position().x -= 4;
        sys.spriteAnim.select(blinky, CommonAnimationID.BLINKY_DAMAGED);
        setDressState(NailDressState.RAPTURED);
    }

    private void blinkyStopsMoving(GameSystems sys) {
        sys.navigator.setSpeed(blinky, 0);
        sys.spriteAnim.stopSelected(blinky);
    }

    private void blinkyGetsCaughtOnNail(GameSystems sys) {
        sys.navigator.setSpeed(blinky, 0.09f);

        //TODO
        //blinkyAnimation(CommonAnimationID.GHOST_NORMAL).setFrameDurationTicks(32);
    }

    private void blinkyStartsRunning(GameSystems sys) {
        blinky.visibility().show();

        sys.navigator.placeAtTile(blinky, 28, 20, -3, 0);
        sys.navigator.setMoveDir(blinky, Direction.LEFT);
        sys.navigator.setWishDir(blinky, Direction.LEFT);
        sys.navigator.setSpeed(blinky, 1.25f);

        sys.spriteAnim.select(blinky, CommonAnimationID.GHOST_NORMAL);
        sys.spriteAnim.playSelected(blinky);
    }

    private void pacManStartsRunning(GameSystems sys) {
        pacMan.visibility().show();

        sys.navigator.placeAtTile(pacMan, 28, 20);
        sys.navigator.setMoveDir(pacMan, Direction.LEFT);
        sys.navigator.setSpeed(pacMan, 1.15f);

        sys.spriteAnim.select(pacMan, CommonAnimationID.PAC_MUNCHING);
        sys.spriteAnim.playSelected(pacMan);
    }

    private void setDressState(NailDressState state) {
        nailDressAnimation.setFrame(state.ordinal());
    }
}