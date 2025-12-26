/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_HeadsUpDisplay;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    public static final short ANIMATION_START_TICK = 120;

    private int tick;
    private Pac pacMan;
    private Ghost blinky;

    public ArcadePacMan_CutScene3(GameUI ui) {
        super(ui);
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

        final var hud = (Arcade_HeadsUpDisplay) game.hud();
        hud.credit(false).score(true).levelCounter(true).livesCounter(false).show();

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
        if (tick < ANIMATION_START_TICK) {
            return;
        }
        switch (tick) {
            case ANIMATION_START_TICK -> {
                soundManager().play(SoundID.INTERMISSION_3, 2);
                pacMan.placeAtTile(29, 20);
                pacMan.setMoveDir(Direction.LEFT);
                pacMan.setSpeed(1.25f);
                pacMan.show();
                pacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                blinky.placeAtTile(35, 20);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.playAnimation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_PATCHED);
            }
            case ANIMATION_START_TICK + 400 -> {
                blinky.placeAtTile(-1, 20);
                blinky.setMoveDir(Direction.RIGHT);
                blinky.setWishDir(Direction.RIGHT);
                blinky.playAnimation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_NAKED);
            }
            case ANIMATION_START_TICK + 700 -> game.control().terminateCurrentGameState();
            default -> {}
        }
        pacMan.move();
        blinky.move();
    }
}