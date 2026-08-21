/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene {

    public static final int TICK_ANIMATION_START      = 120;
    public static final int TICK_BLINKY_RUNNING_NAKED = TICK_ANIMATION_START + 400;
    public static final int TICK_ANIMATION_ENDS       = TICK_ANIMATION_START + 700;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene3(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = app().gameVariants().currentGameVariant().uiConfig().renderConfig();
        final SpriteAnimationContainer animationContainer = app().ui().sprites().animations();
        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animationContainer));

        blinky = renderConfig.createAnimatedGhost(game(), animationContainer, GhostPersonality.RED_GHOST_SHADOW);

        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext game) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }

        final GameSystems sys = game.variant().systems();

        switch (sceneTick) {
            case TICK_ANIMATION_START      -> startAnimation(sys);
            case TICK_BLINKY_RUNNING_NAKED -> startBlinkyRunningNaked(sys);
            case TICK_ANIMATION_ENDS       -> game().state().triggerTimeout();
        }

        sys.motor().move(pacMan);
        sys.motor().move(blinky);
    }

    private void startAnimation(GameSystems sys) {
        app().ui().sounds().play(PacManGameSoundID.INTERMISSION_3, 2);
        startBlinkyChasingPacMan(sys);
    }

    private void startBlinkyRunningNaked(GameSystems sys) {
        sys.worldNavigator().placeAtTile(blinky, -1, 20);
        sys.worldNavigator().setMoveDir(blinky, Direction.RIGHT);
        sys.worldNavigator().setWishDir(blinky, Direction.RIGHT);

        sys.spriteAnimController().select(blinky, CommonSpriteAnimationID.BLINKY_NAKED);
        sys.spriteAnimController().playSelected(blinky);
    }

    private void startBlinkyChasingPacMan(GameSystems sys) {
        pacMan.show();

        sys.worldNavigator().placeAtTile(pacMan, 29, 20);
        sys.worldNavigator().setMoveDir(pacMan, Direction.LEFT);
        sys.worldNavigator().setMoveDirSpeed(pacMan, 1.25f);

        sys.spriteAnimController().select(pacMan, CommonSpriteAnimationID.PAC_MUNCHING);
        sys.spriteAnimController().playSelected(pacMan);

        blinky.show();

        sys.worldNavigator().placeAtTile(blinky, 35, 20);
        sys.worldNavigator().setMoveDir(blinky, Direction.LEFT);
        sys.worldNavigator().setWishDir(blinky, Direction.LEFT);
        sys.worldNavigator().setMoveDirSpeed(blinky, 1.25f);

        sys.spriteAnimController().select(blinky, CommonSpriteAnimationID.BLINKY_PATCHED);
        sys.spriteAnimController().playSelected(blinky);
    }
}