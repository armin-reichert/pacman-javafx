/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GhostAnimations.AnimationID.BLINKY_DAMAGED;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene2D {

    public static final short ANIMATION_START_TICK = 120;

    public enum NailDressState {
        NAIL, STRETCHED_S, STRETCHED_M, STRETCHED_L, RAPTURED
    }

    private int tick;
    private Pac pacMan;
    private Ghost blinky;

    private SpriteAnimation nailDressStretchingAnimation;

    public ArcadePacMan_CutScene2() {}

    public Pac pac() {
        return pacMan;
    }

    public Ghost blinky() {
        return blinky;
    }

    public RectShort currentNailOrStretchedDressSprite() {
        return nailDressStretchingAnimation.currentSprite();
    }

    public int tick() {
        return tick;
    }

    @Override
    public void doInit(Game game) {
        final GameUI_Config uiConfig = ui.currentConfig();

        game.hud().credit(false).score(true).levelCounter(true).livesCounter(false).show();

        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        blinky = uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW);
        blinky.setSpeed(0);
        blinky.hide();

        nailDressStretchingAnimation = SpriteAnimation.buildAnimation()
            .sprites(ArcadePacMan_SpriteSheet.INSTANCE.sprites(SpriteID.RED_GHOST_STRETCHED))
            .once();

        tick = -1;
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        ++tick;
        if (tick < ANIMATION_START_TICK) {
            return;
        }
        switch (tick) {
            case ANIMATION_START_TICK -> soundManager().play(SoundID.INTERMISSION_2);
            case ANIMATION_START_TICK + 1 -> setNailDressState(NailDressState.NAIL);
            case ANIMATION_START_TICK + 25 -> {
                pacMan.placeAtTile(28, 20);
                pacMan.setMoveDir(Direction.LEFT);
                pacMan.setSpeed(1.15f);
                pacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
                pacMan.show();
            }
            case ANIMATION_START_TICK + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
                blinky.show();
            }
            case ANIMATION_START_TICK + 194 -> {
                blinky.setSpeed(0.09f);
                blinkyAnimation(Ghost.AnimationID.GHOST_NORMAL).setFrameTicks(32);
            }
            case ANIMATION_START_TICK + 198 -> setNailDressState(NailDressState.STRETCHED_S);
            case ANIMATION_START_TICK + 230 -> setNailDressState(NailDressState.STRETCHED_M);
            case ANIMATION_START_TICK + 262 -> setNailDressState(NailDressState.STRETCHED_L);
            case ANIMATION_START_TICK + 296 -> {
                blinky.setSpeed(0);
                blinky.stopAnimation();
            }
            case ANIMATION_START_TICK + 360 -> {
                setNailDressState(NailDressState.RAPTURED);
                blinky.setX(blinky.x() - 4);
                blinky.selectAnimation(BLINKY_DAMAGED);
            }
            case ANIMATION_START_TICK + 420 -> blinkyAnimation(BLINKY_DAMAGED).nextFrame(); // Eyes right-down
            case ANIMATION_START_TICK + 508 -> {
                blinky.setVisible(false);
                game.control().terminateCurrentGameState();
            }
            default -> {}
        }
        pacMan.move();
        blinky.move();
    }

    private void setNailDressState(NailDressState state) {
        switch (state) {
            case NAIL -> nailDressStretchingAnimation.setFrameIndex(0);
            case STRETCHED_S -> nailDressStretchingAnimation.setFrameIndex(1);
            case STRETCHED_M -> nailDressStretchingAnimation.setFrameIndex(2);
            case STRETCHED_L -> nailDressStretchingAnimation.setFrameIndex(3);
            case RAPTURED -> nailDressStretchingAnimation.setFrameIndex(4);
        }
    }

    private SpriteAnimation blinkyAnimation(Object animationID) {
        return (SpriteAnimation) blinky.optAnimationManager()
            .map(animations -> animations.animation(animationID))
            .orElse(null);
    }
}