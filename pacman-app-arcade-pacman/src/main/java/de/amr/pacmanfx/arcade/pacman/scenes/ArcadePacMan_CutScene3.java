/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_GhostAnimations;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    public static final short ANIMATION_START_TICK = 120;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene3() {}

    @Override
    public void onStart() {
        final UIConfig uiConfig = ui.currentConfig();

        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationDriver()));
        blinky = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), RED_GHOST_SHADOW);

        sceneTick = -1;
    }

    @Override
    public void onTick(long tick) {
        ++sceneTick;
        if (sceneTick < ANIMATION_START_TICK) {
            return;
        }
        if (sceneTick == ANIMATION_START_TICK) {
            ui.soundManager().play(SoundID.INTERMISSION_3, 2);
            pacMan.placeAtTile(29, 20);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(1.25f);
            pacMan.show();
            pacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
            pacMan.playAnimation();
            blinky.placeAtTile(35, 20);
            blinky.setMoveDir(Direction.LEFT);
            blinky.setWishDir(Direction.LEFT);
            blinky.setSpeed(1.25f);
            blinky.show();
            blinky.selectAnimation(ArcadePacMan_GhostAnimations.AnimationID.BLINKY_DRESS_PATCHED);
            blinky.playAnimation();
        }
        else if (sceneTick == ANIMATION_START_TICK + 400){
            blinky.placeAtTile(-1, 20);
            blinky.setMoveDir(Direction.RIGHT);
            blinky.setWishDir(Direction.RIGHT);
            blinky.selectAnimation(ArcadePacMan_GhostAnimations.AnimationID.BLINKY_NAKED);
            blinky.playAnimation();
        }
        else if (sceneTick == ANIMATION_START_TICK + 700) {
            gameContext().game().flow().state().expire();
            return;
        }
        pacMan.move();
        blinky.move();
    }
}