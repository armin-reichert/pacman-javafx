/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacAnimations;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * First cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over the screen,
 * then a frightened ghost is chased by a big Pac-Man from left to right.
 */
public class ArcadePacMan_CutScene1 extends GameScene2D {

    public static final short ANIMATION_START_TICK = 120;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene1(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void onSceneStart() {
        final UIConfig uiConfig = ui.currentConfig();
        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationDriver()));
        blinky = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(long tick) {
        if (++sceneTick < ANIMATION_START_TICK) {
            return;
        }
        if (sceneTick == ANIMATION_START_TICK) {
            ui.soundManager().play(SoundID.INTERMISSION_1, 2);
            startBlinkyChasingPacMan();
        }
        else if (sceneTick == ANIMATION_START_TICK + 260) {
            startBlinkyEscapingPacMan();
        }
        else if (sceneTick == ANIMATION_START_TICK + 400) {
            startBigPacManChasingBlinky();
        }
        else if (sceneTick == ANIMATION_START_TICK + 632) {
            gameContext().game().flow().state().expire();
        }
        if (sceneTick >= ANIMATION_START_TICK) {
            pacMan.move();
            blinky.move();
        }
    }

    private void startBigPacManChasingBlinky() {
        pacMan.placeAtTile(-3, 18, 0, 6.5f);
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.selectAnimation(ArcadePacMan_PacAnimations.AnimationID.ANIM_BIG_PAC_MAN);
        pacMan.playAnimation();
    }

    private void startBlinkyEscapingPacMan() {
        blinky.placeAtTile(-2, 20, 4, 0);
        blinky.setMoveDir(Direction.RIGHT);
        blinky.setWishDir(Direction.RIGHT);
        blinky.setSpeed(0.75f);
        blinky.selectAnimation(Ghost.AnimationID.GHOST_FRIGHTENED);
        blinky.playAnimation();
    }

    private void startBlinkyChasingPacMan() {
        pacMan.placeAtTile(29, 20);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.setSpeed(1.25f);
        pacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
        pacMan.playAnimation();
        pacMan.show();

        blinky.placeAtTile(32, 20);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        blinky.setSpeed(1.3f);
        blinky.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
        blinky.playAnimation();
        blinky.show();
    }
}