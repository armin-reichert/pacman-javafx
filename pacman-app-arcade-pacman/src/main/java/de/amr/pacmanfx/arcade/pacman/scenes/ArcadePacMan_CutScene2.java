/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ANIM_BLINKY_DAMAGED;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ANIM_BLINKY_NAIL_DRESS_RAPTURE;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene2D {

    private static final short ANIMATION_START = 120;

    private static final byte NAIL = 0, STRETCHED_S = 1, STRETCHED_M = 2, STRETCHED_L = 3, RAPTURED = 4;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyDamaged;
    private SpriteAnimation nailDressRapture;

    private HUDRenderer hudRenderer;
    private ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer       = configureRenderer(uiConfig.createHUDRenderer(canvas));
        actorRenderer     = configureRenderer(uiConfig.createActorRenderer(canvas));
        debugInfoRenderer = configureRenderer(new DefaultDebugInfoRenderer(ui, canvas) {
            @Override
            public void drawDebugInfo() {
                super.drawDebugInfo();
                String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
                fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
            }
        });
    }

    @Override
    public HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        context().game().hud().creditVisible(false).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        GameUI_Config uiConfig = ui.currentConfig();

        pac = ArcadePacMan_ActorFactory.createPacMan();
        pac.setAnimationManager(uiConfig.createPacAnimations());

        blinky = uiConfig.createAnimatedGhost(RED_GHOST_SHADOW);
        blinky.setSpeed(0);
        blinky.hide();

        blinky.optAnimationManager().ifPresent(animations -> {
            blinkyNormal     = (SpriteAnimation) animations.animation(CommonAnimationID.ANIM_GHOST_NORMAL);
            nailDressRapture = (SpriteAnimation) animations.animation(ANIM_BLINKY_NAIL_DRESS_RAPTURE);
            blinkyDamaged    = (SpriteAnimation) animations.animation(ANIM_BLINKY_DAMAGED);
        });
        actorsInZOrder.add(pac);
        actorsInZOrder.add(blinky);

        frame = -1;
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {
        ++frame;
        if (frame < ANIMATION_START) {
            return;
        }
        switch (frame) {
            case ANIMATION_START -> ui.soundManager().play(SoundID.INTERMISSION_2);
            case ANIMATION_START + 1 -> nailDressRapture.setFrameIndex(NAIL);
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
            case ANIMATION_START + 198 -> nailDressRapture.setFrameIndex(STRETCHED_S);
            case ANIMATION_START + 230 -> nailDressRapture.setFrameIndex(STRETCHED_M);
            case ANIMATION_START + 262 -> nailDressRapture.setFrameIndex(STRETCHED_L);
            case ANIMATION_START + 296 -> {
                blinky.setSpeed(0);
                blinky.stopAnimation();
            }
            case ANIMATION_START + 360 -> {
                nailDressRapture.setFrameIndex(RAPTURED);
                blinky.setX(blinky.x() - 4);
                blinky.selectAnimation(ANIM_BLINKY_DAMAGED);
            }
            case ANIMATION_START + 420 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                context().gameController().letCurrentGameStateExpire();
            }
            default -> {}
        }
        pac.move();
        blinky.move();
    }

    @Override
    public void drawSceneContent() {
        sceneRenderer.drawSprite(nailDressRapture.currentSprite(), TS(14), TS(19) + 3, true);
        actorsInZOrder.forEach(actor -> actorRenderer.drawActor(actor));
    }
}