/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends AbstractGameScene2D {

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
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer container = appContext().ui().sprites().animations();
        final var factory = ArcadePacMan_ActorFactory.instance();

        pacMan = factory.createPacMan();
        pacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(container));

        blinky = renderConfig.createAnimatedGhost(gameContext(), container, GhostPersonality.RED_GHOST_SHADOW);

        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext gameContext) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }

        final GameSystems sys = gameContext.systems();

        switch (sceneTick) {
            case TICK_ANIMATION_START      -> startAnimation(sys);
            case TICK_BLINKY_RUNNING_NAKED -> startBlinkyRunningNaked(sys);
            case TICK_ANIMATION_ENDS       -> gameState().triggerTimeout();
        }

        sys.motor().moveAccelerated(pacMan);
        sys.motor().moveAccelerated(blinky);
    }

    private void startAnimation(GameSystems sys) {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3, 2);
        startBlinkyChasingPacMan(sys);
    }

    private void startBlinkyRunningNaked(GameSystems sys) {
        sys.navigator().placeAtTile(blinky, -1, 20);
        sys.navigator().setMoveDir(blinky, Direction.RIGHT);
        sys.navigator().setWishDir(blinky, Direction.RIGHT);

        sys.spriteAnim().select(blinky, CommonAnimationID.BLINKY_NAKED);
        sys.spriteAnim().playSelected(blinky);
    }

    private void startBlinkyChasingPacMan(GameSystems sys) {
        pacMan.show();

        sys.navigator().placeAtTile(pacMan, 29, 20);
        sys.navigator().setMoveDir(pacMan, Direction.LEFT);
        sys.navigator().setSpeed(pacMan, 1.25f);

        sys.spriteAnim().select(pacMan, CommonAnimationID.PAC_MUNCHING);
        sys.spriteAnim().playSelected(pacMan);

        blinky.show();

        sys.navigator().placeAtTile(blinky, 35, 20);
        sys.navigator().setMoveDir(blinky, Direction.LEFT);
        sys.navigator().setWishDir(blinky, Direction.LEFT);
        sys.navigator().setSpeed(blinky, 1.25f);

        sys.spriteAnim().select(blinky, CommonAnimationID.BLINKY_PATCHED);
        sys.spriteAnim().playSelected(blinky);
    }
}