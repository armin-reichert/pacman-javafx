/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.model.GameModel.RED_GHOST_SHADOW;

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

    public ArcadePacMan_CutScene3(Game game) {
        super(game);
    }

    @Override
    public void onActivate() {
        final SpriteAnimationContainer spriteAnimationContainer = game().ui().sprites().animations();
        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        final GameVariantConfig variantConfig = game().variantManager().selectedVariant().config();
        pacMan.setAnimations(variantConfig.createPacAnimations(spriteAnimationContainer));
        blinky = variantConfig.createAnimatedGhost(spriteAnimationContainer, RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(long tick) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }
        switch (sceneTick) {
            case TICK_ANIMATION_START      -> startAnimation();
            case TICK_BLINKY_RUNNING_NAKED -> startBlinkyRunningNaked();
            case TICK_ANIMATION_ENDS       -> gameState().triggerTimeout();
        }
        pacMan.move();
        blinky.move();
    }

    private void startAnimation() {
        game().ui().sounds().play(PacManGameSoundID.INTERMISSION_3, 2);
        startBlinkyChasingPacMan();
    }

    private void startBlinkyRunningNaked() {
        blinky.placeAtTile(-1, 20);
        blinky.setMoveDir(Direction.RIGHT);
        blinky.setWishDir(Direction.RIGHT);
        blinky.animations().select(ArcadePacMan_AnimationID.BLINKY_NAKED);
        blinky.animations().playSelected();
    }

    private void startBlinkyChasingPacMan() {
        pacMan.placeAtTile(29, 20);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.setSpeed(1.25f);
        pacMan.show();
        pacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pacMan.animations().playSelected();

        blinky.placeAtTile(35, 20);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        blinky.setSpeed(1.25f);
        blinky.show();
        blinky.animations().select(ArcadePacMan_AnimationID.BLINKY_DRESS_PATCHED);
        blinky.animations().playSelected();
    }
}