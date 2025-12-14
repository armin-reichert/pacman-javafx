/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_CutScene1_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * First cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over the screen,
 * then a frightened ghost is chased by a big Pac-Man from left to right.
 */
public class ArcadePacMan_CutScene1 extends GameScene2D {

    public static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    private ArcadePacMan_CutScene1_Renderer sceneRenderer;

    public ArcadePacMan_CutScene1(GameUI ui) {
        super(ui);
    }
    
    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();
        sceneRenderer = adaptRenderer(
            new ArcadePacMan_CutScene1_Renderer(this, canvas, (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet()));
    }

    @Override
    public ArcadePacMan_CutScene1_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    public Pac pac() {
        return pac;
    }

    public Ghost blinky() {
        return blinky;
    }

    public int frame() {
        return frame;
    }

    @Override
    public void doInit(Game game) {
        GameUI_Config uiConfig = ui.currentConfig();
        game.hud().credit(false).score(true).levelCounter(true).livesCounter(false).show();
        pac = ArcadePacMan_ActorFactory.createPacMan();
        pac.setAnimationManager(uiConfig.createPacAnimations());
        blinky = uiConfig.createAnimatedGhost(RED_GHOST_SHADOW);
        frame = -1;
    }

    @Override
    protected void doEnd(Game game) {
    }

    @Override
    public void update(Game game) {
        ++frame;
        if (frame == ANIMATION_START) {
            ui.soundManager().play(SoundID.INTERMISSION_1, 2);

            pac.placeAtTile(29, 20);
            pac.setMoveDir(Direction.LEFT);
            pac.setSpeed(1.25f);
            pac.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
            pac.show();

            blinky.placeAtTile(32, 20);
            blinky.setMoveDir(Direction.LEFT);
            blinky.setWishDir(Direction.LEFT);
            blinky.setSpeed(1.3f);
            blinky.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
            blinky.show();
        }
        else if (frame == ANIMATION_START + 260) {
            blinky.placeAtTile(-2, 20, 4, 0);
            blinky.setMoveDir(Direction.RIGHT);
            blinky.setWishDir(Direction.RIGHT);
            blinky.setSpeed(0.75f);
            blinky.playAnimation(CommonAnimationID.ANIM_GHOST_FRIGHTENED);
        }
        else if (frame == ANIMATION_START + 400) {
            pac.placeAtTile(-3, 18, 0, 6.5f);
            pac.setMoveDir(Direction.RIGHT);
            pac.playAnimation(ArcadePacMan_UIConfig.AnimationID.ANIM_BIG_PAC_MAN);
        }
        else if (frame == ANIMATION_START + 632) {
            game.control().terminateCurrentGameState();
        }
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
    }
}