/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_CutScene2_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene2D {

    public static final short ANIMATION_START = 120;

    private static final byte NAIL = 0, STRETCHED_S = 1, STRETCHED_M = 2, STRETCHED_L = 3, RAPTURED = 4;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyDamaged;
    private SpriteAnimation nailDressRaptureAnimation;

    private ArcadePacMan_CutScene2_Renderer sceneRenderer;
    private HUD_Renderer hudRenderer;

    public ArcadePacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = configureRenderer(
            uiConfig.createHUDRenderer(canvas));

        sceneRenderer = configureRenderer(
            new ArcadePacMan_CutScene2_Renderer(this, canvas, (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet()));
    }

    @Override
    public HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadePacMan_CutScene2_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    public SpriteAnimation nailDressRaptureAnimation() {
        return nailDressRaptureAnimation;
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
        game.hud().creditVisible(false).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        GameUI_Config uiConfig = ui.currentConfig();

        pac = ArcadePacMan_ActorFactory.createPacMan();
        pac.setAnimationManager(uiConfig.createPacAnimations());

        blinky = uiConfig.createAnimatedGhost(RED_GHOST_SHADOW);
        blinky.setSpeed(0);
        blinky.hide();

        blinky.optAnimationManager().ifPresent(animations -> {
            blinkyNormal     = (SpriteAnimation) animations.animation(CommonAnimationID.ANIM_GHOST_NORMAL);
            nailDressRaptureAnimation = (SpriteAnimation) animations.animation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_NAIL_DRESS_RAPTURE);
            blinkyDamaged    = (SpriteAnimation) animations.animation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_DAMAGED);
        });

        frame = -1;
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        ++frame;
        if (frame < ANIMATION_START) {
            return;
        }
        switch (frame) {
            case ANIMATION_START -> ui.soundManager().play(SoundID.INTERMISSION_2);
            case ANIMATION_START + 1 -> nailDressRaptureAnimation.setFrameIndex(NAIL);
            case ANIMATION_START + 25 -> {
                pac.placeAtTile(28, 20);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.15f);
                pac.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                pac.show();
            }
            case ANIMATION_START + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
                blinky.show();
            }
            case ANIMATION_START + 194 -> {
                blinky.setSpeed(0.09f);
                blinkyNormal.setFrameTicks(32);
            }
            case ANIMATION_START + 198 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_S);
            case ANIMATION_START + 230 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_M);
            case ANIMATION_START + 262 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_L);
            case ANIMATION_START + 296 -> {
                blinky.setSpeed(0);
                blinky.stopAnimation();
            }
            case ANIMATION_START + 360 -> {
                nailDressRaptureAnimation.setFrameIndex(RAPTURED);
                blinky.setX(blinky.x() - 4);
                blinky.selectAnimation(ArcadePacMan_UIConfig.AnimationID.ANIM_BLINKY_DAMAGED);
            }
            case ANIMATION_START + 420 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                game.control().terminateCurrentGameState();
            }
            default -> {}
        }
        pac.move();
        blinky.move();
    }
}