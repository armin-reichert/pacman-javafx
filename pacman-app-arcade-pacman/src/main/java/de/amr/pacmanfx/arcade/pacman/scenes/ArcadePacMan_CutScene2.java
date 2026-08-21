/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_CutScene2.NailDressState.*;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene {

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
        final GameVariantRenderConfig renderConfig = app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        final SpriteAnimContainer animContainer = app().ui().spriteAnimManager().animContainer();
        final SpriteAnimController animController = app().game().variant().systems().spriteAnimController();
        final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();
        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        blinky = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW);

        nailDressAnimation = new SpriteAnimationBuilder()
            .sprites(spriteSheet.findSprites(SpriteID.RED_GHOST_STRETCHED))
            .initiallyStopped()
            .build(animContainer);

        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext game) {
        if (++sceneTick < TICK_ANIMATION_START) {
            return;
        }

        final GameSystems sys = game.variant().systems();

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
            case TICK_BLINK_INSPECTS_DAMAGE  -> blinkyInspectsDamagedDress(sys.spriteAnimController());
            case TICK_ANIMATION_ENDS         -> endTheShow();
        }

        sys.motor().move(pacMan);
        sys.motor().move(blinky);
    }

    private void blinkyInspectsDamagedDress(SpriteAnimController animSystem) {
        animSystem.advanceFrame(blinky);
    }

    private void startTheShow() {
        app().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
        setDressState(NailDressState.NAIL);
    }

    private void endTheShow() {
        blinky.hide();
        game().state().triggerTimeout();
    }

    private void dressRaptures(GameSystems sys) {
        blinky.pos().sub(4, 0);
        sys.spriteAnimController().select(blinky, CommonSpriteAnimationID.BLINKY_DAMAGED);
        setDressState(NailDressState.RAPTURED);
    }

    private void blinkyStopsMoving(GameSystems sys) {
        sys.worldNavigator().setMoveDirSpeed(blinky, 0);
        sys.spriteAnimController().stopSelected(blinky);
    }

    private void blinkyGetsCaughtOnNail(GameSystems sys) {
        sys.worldNavigator().setMoveDirSpeed(blinky, 0.09f);
        //TODO
        //blinkyAnimation(CommonAnimationID.GHOST_NORMAL).setFrameDurationTicks(32);
    }

    private void blinkyStartsRunning(GameSystems sys) {
        blinky.show();

        sys.worldNavigator().placeAtTile(blinky, 28, 20, -3, 0);
        sys.worldNavigator().setMoveDir(blinky, Direction.LEFT);
        sys.worldNavigator().setWishDir(blinky, Direction.LEFT);
        sys.worldNavigator().setMoveDirSpeed(blinky, 1.25f);

        sys.spriteAnimController().select(blinky, CommonSpriteAnimationID.GHOST_NORMAL);
        sys.spriteAnimController().playSelected(blinky);
    }

    private void pacManStartsRunning(GameSystems sys) {
        pacMan.show();

        sys.worldNavigator().placeAtTile(pacMan, 28, 20);
        sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);
        sys.worldNavigator().setMoveDirSpeed(pacMan, 1.15f);

        sys.spriteAnimController().select(pacMan, CommonSpriteAnimationID.PAC_MUNCHING);
        sys.spriteAnimController().playSelected(pacMan);
    }

    private void setDressState(NailDressState state) {
        nailDressAnimation.setFrame(state.ordinal());
    }
}