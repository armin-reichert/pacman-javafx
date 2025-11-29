/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_CutScene3_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    public static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    private ArcadePacMan_CutScene3_Renderer sceneRenderer;
    private HUD_Renderer hudRenderer;

    public ArcadePacMan_CutScene3(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = configureRenderer(
            uiConfig.createHUDRenderer(canvas));

        sceneRenderer = configureRenderer(
            new ArcadePacMan_CutScene3_Renderer(this, canvas, (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet()));
    }

    @Override
    public HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadePacMan_CutScene3_Renderer sceneRenderer() {
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
    public void doInit() {
        context().currentGame().hud().creditVisible(false).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        GameUI_Config uiConfig = ui.currentConfig();

        pac = ArcadePacMan_ActorFactory.createPacMan();
        pac.setAnimationManager(uiConfig.createPacAnimations());

        blinky = uiConfig.createAnimatedGhost(RED_GHOST_SHADOW);

        frame = -1;
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {
        ++frame;
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
        switch (frame) {
            case ANIMATION_START -> {
                ui.soundManager().play(SoundID.INTERMISSION_3, 2);
                pac.placeAtTile(29, 20);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.show();
                pac.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                blinky.placeAtTile(35, 20);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.playAnimation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_PATCHED);
            }
            case ANIMATION_START + 400 -> {
                blinky.placeAtTile(-1, 20);
                blinky.setMoveDir(Direction.RIGHT);
                blinky.setWishDir(Direction.RIGHT);
                blinky.playAnimation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_NAKED);
            }
            case ANIMATION_START + 700 -> context().currentGame().control().terminateCurrentGameState();
            default -> {}
        }
    }
}