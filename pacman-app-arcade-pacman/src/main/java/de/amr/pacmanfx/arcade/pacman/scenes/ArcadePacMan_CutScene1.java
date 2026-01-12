/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_PacAnimations;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * First cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over the screen,
 * then a frightened ghost is chased by a big Pac-Man from left to right.
 */
public class ArcadePacMan_CutScene1 extends GameScene2D {

    public static final short ANIMATION_START_TICK = 120;

    private int tick;
    private Pac pacMan;
    private Ghost blinky;

    public ArcadePacMan_CutScene1() {}
    
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
        game.hud().credit(false).score(true).levelCounter(true).livesCounter(false).show();
        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());
        blinky = uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW);
        tick = -1;
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        ++tick;
        if (tick == ANIMATION_START_TICK) {
            soundManager().play(SoundID.INTERMISSION_1, 2);

            pacMan.placeAtTile(29, 20);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(1.25f);
            pacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
            pacMan.show();

            blinky.placeAtTile(32, 20);
            blinky.setMoveDir(Direction.LEFT);
            blinky.setWishDir(Direction.LEFT);
            blinky.setSpeed(1.3f);
            blinky.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
            blinky.show();
        }
        else if (tick == ANIMATION_START_TICK + 260) {
            blinky.placeAtTile(-2, 20, 4, 0);
            blinky.setMoveDir(Direction.RIGHT);
            blinky.setWishDir(Direction.RIGHT);
            blinky.setSpeed(0.75f);
            blinky.playAnimation(Ghost.AnimationID.GHOST_FRIGHTENED);
        }
        else if (tick == ANIMATION_START_TICK + 400) {
            pacMan.placeAtTile(-3, 18, 0, 6.5f);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.playAnimation(ArcadePacMan_PacAnimations.AnimationID.ANIM_BIG_PAC_MAN);
        }
        else if (tick == ANIMATION_START_TICK + 632) {
            game.control().terminateCurrentGameState();
        }

        if (tick >= ANIMATION_START_TICK) {
            pacMan.move();
            blinky.move();
        }
    }
}