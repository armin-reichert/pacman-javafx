/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationBuilder;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GhostAnimations.AnimationID.BLINKY_DAMAGED;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene2D {

    // ordinal value corresponds to animation frame
    private enum NailDressAnimationFrame {
        NAIL, STRETCHED_S, STRETCHED_M, STRETCHED_L, RAPTURED
    }

    /** Tick when animation starts */
    public static final short ANIMATION_START = 120;

    public final int nailX = TS * 14;
    public final int nailY = TS * 19 + 3;
    public int tick;
    public Pac pacMan;
    public Ghost blinky;
    public SpriteAnimation nailDressAnimation;

    public ArcadePacMan_CutScene2() {}

    @Override
    public void doInit(Game game) {
        final UIConfig uiConfig = ui.currentConfig();

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationDriver()));

        blinky = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), RED_GHOST_SHADOW);

        nailDressAnimation = SpriteAnimationBuilder.builder(ui.spriteAnimationDriver())
            .sprites(ArcadePacMan_SpriteSheet.instance().sprites(SpriteID.RED_GHOST_STRETCHED))
            .initiallyStopped()
            .build();

        tick = -1;
    }

    @Override
    public void update(Game game) {
        ++tick;
        if (tick < ANIMATION_START) {
            return;
        }
        switch (tick) {
            case ANIMATION_START -> {
                ui.soundManager().play(SoundID.INTERMISSION_2);
                setNailDressAnimation(NailDressAnimationFrame.NAIL);
            }
            case ANIMATION_START + 25 -> {
                pacMan.placeAtTile(28, 20);
                pacMan.setMoveDir(Direction.LEFT);
                pacMan.setSpeed(1.15f);
                pacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
                pacMan.playAnimation();
                pacMan.show();
            }
            case ANIMATION_START + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
                blinky.playAnimation();
                blinky.show();
            }
            case ANIMATION_START + 194 -> {
                blinky.setSpeed(0.09f);
                blinkyAnimation(Ghost.AnimationID.GHOST_NORMAL).setFrameTicks(32);
            }
            case ANIMATION_START + 198 -> setNailDressAnimation(NailDressAnimationFrame.STRETCHED_S);
            case ANIMATION_START + 230 -> setNailDressAnimation(NailDressAnimationFrame.STRETCHED_M);
            case ANIMATION_START + 262 -> setNailDressAnimation(NailDressAnimationFrame.STRETCHED_L);
            case ANIMATION_START + 296 -> {
                blinky.setSpeed(0);
                blinky.stopAnimation();
            }
            case ANIMATION_START + 360 -> {
                setNailDressAnimation(NailDressAnimationFrame.RAPTURED);
                blinky.setX(blinky.x() - 4);
                blinky.selectAnimation(BLINKY_DAMAGED);
            }
            case ANIMATION_START + 420 -> blinkyAnimation(BLINKY_DAMAGED).advanceFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                game.flow().state().expire();
            }
            default -> {}
        }
        pacMan.move();
        blinky.move();
    }

    private void setNailDressAnimation(NailDressAnimationFrame animationFrame) {
        nailDressAnimation.setCurrentFrameIndex(animationFrame.ordinal());
    }

    private SpriteAnimation blinkyAnimation(Object animationID) {
        return (SpriteAnimation) blinky.animations().animation(animationID);
    }
}