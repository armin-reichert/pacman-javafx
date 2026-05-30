/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.core.Globals.RED_GHOST_SHADOW;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    public static final int TICK_ANIMATION_START      = 120;
    public static final int TICK_BLINKY_RUNNING_NAKED = TICK_ANIMATION_START + 400;
    public static final int TICK_ANIMATION_ENDS       = TICK_ANIMATION_START + 700;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene3(GameUI ui) {
        super(ui);
    }

    @Override
    public void onActivate(UIConfig uiConfig) {
        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations(ui.spriteAnimationSet()));
        blinky = uiConfig.createGhostWithAnimations(ui.spriteAnimationSet(), RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(GameClock clock) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }
        if (sceneTick == TICK_ANIMATION_START) {
            ui.services().sounds().play(PacManGameSoundID.INTERMISSION_3, 2);
            startBlinkyChasingPacMan();
        }
        else if (sceneTick == TICK_BLINKY_RUNNING_NAKED){
            startBlinkyRunningNaked();
        }
        else if (sceneTick == TICK_ANIMATION_ENDS) {
            gameContext().game().flow().state().expire();
            return;
        }
        pacMan.move();
        blinky.move();
    }

    private void startBlinkyRunningNaked() {
        blinky.placeAtTile(-1, 20);
        blinky.setMoveDir(Direction.RIGHT);
        blinky.setWishDir(Direction.RIGHT);
        blinky.animationManager().select(ArcadePacMan_AnimationID.BLINKY_NAKED);
        blinky.animationManager().playSelected();
    }

    private void startBlinkyChasingPacMan() {
        pacMan.placeAtTile(29, 20);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.setSpeed(1.25f);
        pacMan.show();
        pacMan.animationManager().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pacMan.animationManager().playSelected();
        blinky.placeAtTile(35, 20);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        blinky.setSpeed(1.25f);
        blinky.show();
        blinky.animationManager().select(ArcadePacMan_AnimationID.BLINKY_DRESS_PATCHED);
        blinky.animationManager().playSelected();
    }
}