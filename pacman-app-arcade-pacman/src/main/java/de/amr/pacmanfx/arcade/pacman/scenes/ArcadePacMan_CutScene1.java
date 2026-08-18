/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacSAM;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

/**
 * First cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over the screen,
 * then a frightened ghost is chased by a big Pac-Man from left to right.
 */
public class ArcadePacMan_CutScene1 extends GameScene {

    public static final short ANIMATION_START_TICK = 120;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene1(GameAppContext appContext) {
        super(appContext);
    }
    
    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        final SpriteAnimationContainer container = app().ui().sprites().animations();
        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(container));

        blinky = renderConfig.createAnimatedGhost(game(), container, GhostPersonality.RED_GHOST_SHADOW);

        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext game) {
        if (++sceneTick < ANIMATION_START_TICK) {
            return;
        }

        final GameSystems sys = game.variant().systems();

        if (sceneTick == ANIMATION_START_TICK) {
            app().ui().sounds().play(PacManGameSoundID.INTERMISSION_1, 2);
            startBlinkyChasingPacMan(sys);
        }
        else if (sceneTick == ANIMATION_START_TICK + 260) {
            startBlinkyEscapingPacMan(sys);
        }
        else if (sceneTick == ANIMATION_START_TICK + 400) {
            startBigPacManChasingBlinky(sys);
        }
        else if (sceneTick == ANIMATION_START_TICK + 632) {
            game().state().triggerTimeout();
        }
        if (sceneTick >= ANIMATION_START_TICK) {
            sys.motor().move(pacMan);
            sys.motor().move(blinky);
        }
    }

    private void startBigPacManChasingBlinky(GameSystems sys) {
        sys.worldNavigator().placeAtTile(pacMan, -3, 18, 0, 6.5f);
        sys.worldNavigator().setMoveDir(pacMan, Direction.RIGHT);

        sys.spriteAnimController().select(pacMan, ArcadePacMan_PacSAM.AnimationID.ANIM_BIG_PAC_MAN);
        sys.spriteAnimController().playSelected(pacMan);
    }

    private void startBlinkyEscapingPacMan(GameSystems sys) {
        sys.worldNavigator().placeAtTile(blinky, -2, 20, 4, 0);
        sys.worldNavigator().setMoveDir(blinky, Direction.RIGHT);
        sys.worldNavigator().setWishDir(blinky, Direction.RIGHT);
        sys.worldNavigator().setSpeed(blinky, 0.75f);

        sys.spriteAnimController().select(blinky, CommonSpriteAnimationID.GHOST_FRIGHTENED);
        sys.spriteAnimController().playSelected(blinky);
    }

    private void startBlinkyChasingPacMan(GameSystems sys) {
        pacMan.show();

        sys.worldNavigator().placeAtTile(pacMan, 29, 20);
        sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);
        sys.worldNavigator().setSpeed(pacMan, 1.25f);

        sys.spriteAnimController().select(pacMan, CommonSpriteAnimationID.PAC_MUNCHING);
        sys.spriteAnimController().playSelected(pacMan);

        blinky.show();

        sys.worldNavigator().placeAtTile(blinky, 32, 20);
        sys.worldNavigator().setMoveDir(blinky, Direction.LEFT);
        sys.worldNavigator().setWishDir(blinky, Direction.LEFT);
        sys.worldNavigator().setSpeed(blinky, 1.3f);

        sys.spriteAnimController().select(blinky, CommonSpriteAnimationID.GHOST_NORMAL);
        sys.spriteAnimController().playSelected(blinky);
    }
}