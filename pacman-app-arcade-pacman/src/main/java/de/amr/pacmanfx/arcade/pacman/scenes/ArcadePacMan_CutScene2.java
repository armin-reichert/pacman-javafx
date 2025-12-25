/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_HUD;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene2D {

    public static final short ANIMATION_START_TICK = 120;

    private static final byte NAIL = 0, STRETCHED_S = 1, STRETCHED_M = 2, STRETCHED_L = 3, RAPTURED = 4;

    private int tick;
    private Pac pacMan;
    private Ghost blinky;

    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyDamaged;
    private SpriteAnimation nailDressRaptureAnimation;

    public ArcadePacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    public SpriteAnimation nailDressRaptureAnimation() {
        return nailDressRaptureAnimation;
    }

    public Pac pac() {
        return pacMan;
    }

    public Ghost blinky() {
        return blinky;
    }

    public int tick() {
        return tick;
    }

    @Override
    public void doInit(Game game) {
        final GameUI_Config uiConfig = ui.currentConfig();

        final var hud = (Arcade_HUD) game.hud();
        hud.credit(false).score(true).levelCounter(true).livesCounter(false).show();

        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        blinky = uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW);
        blinky.setSpeed(0);
        blinky.hide();

        blinky.optAnimationManager().ifPresent(am -> {
            blinkyNormal = (SpriteAnimation) am.animation(CommonAnimationID.ANIM_GHOST_NORMAL);
            nailDressRaptureAnimation = (SpriteAnimation) am.animation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_NAIL_DRESS_RAPTURE);
            blinkyDamaged = (SpriteAnimation) am.animation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_DAMAGED);
        });

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
            case ANIMATION_START_TICK + 1 -> nailDressRaptureAnimation.setFrameIndex(NAIL);
            case ANIMATION_START_TICK + 25 -> {
                pacMan.placeAtTile(28, 20);
                pacMan.setMoveDir(Direction.LEFT);
                pacMan.setSpeed(1.15f);
                pacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                pacMan.show();
            }
            case ANIMATION_START_TICK + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
                blinky.show();
            }
            case ANIMATION_START_TICK + 194 -> {
                blinky.setSpeed(0.09f);
                blinkyNormal.setFrameTicks(32);
            }
            case ANIMATION_START_TICK + 198 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_S);
            case ANIMATION_START_TICK + 230 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_M);
            case ANIMATION_START_TICK + 262 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_L);
            case ANIMATION_START_TICK + 296 -> {
                blinky.setSpeed(0);
                blinky.stopAnimation();
            }
            case ANIMATION_START_TICK + 360 -> {
                nailDressRaptureAnimation.setFrameIndex(RAPTURED);
                blinky.setX(blinky.x() - 4);
                blinky.selectAnimation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_DAMAGED);
            }
            case ANIMATION_START_TICK + 420 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START_TICK + 508 -> {
                blinky.setVisible(false);
                game.control().terminateCurrentGameState();
            }
            default -> {}
        }
        pacMan.move();
        blinky.move();
    }
}