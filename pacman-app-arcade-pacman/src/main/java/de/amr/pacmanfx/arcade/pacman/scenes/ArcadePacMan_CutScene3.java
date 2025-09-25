/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_ActorRenderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ANIM_BLINKY_NAKED;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ANIM_BLINKY_PATCHED;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission";
    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    private ArcadePacMan_HUDRenderer hudRenderer;
    private ArcadePacMan_ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene3(GameUI ui) {
        super(ui);
    }

    @Override
    public void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        GameUI_Config uiConfig = ui.currentConfig();
        hudRenderer       = configureRenderer(new ArcadePacMan_HUDRenderer(canvas, uiConfig));
        actorRenderer     = configureRenderer(new ArcadePacMan_ActorRenderer(canvas, uiConfig));
        debugInfoRenderer = configureRenderer(new DefaultDebugInfoRenderer(ui, canvas) {
            @Override
            public void drawDebugInfo() {
                super.drawDebugInfo();
                String text = frame < ANIMATION_START
                        ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
                fillText(text, debugTextFill, debugTextFont, TS(1), TS(5));
            }
        });
    }

    @Override
    public ArcadePacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        context().game().hud().creditVisible(false).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        GameUI_Config uiConfig = ui.currentConfig();

        pac = createPac();
        pac.setAnimations(uiConfig.createPacAnimations());

        blinky = uiConfig.createGhost(RED_GHOST_SHADOW);

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
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
        switch (frame) {
            case ANIMATION_START -> {
                ui.soundManager().play(MUSIC_ID, 2);
                pac.placeAtTile(29, 20);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.show();
                pac.playAnimation(ANIM_PAC_MUNCHING);
                blinky.placeAtTile(35, 20);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.playAnimation(ANIM_BLINKY_PATCHED);
            }
            case ANIMATION_START + 400 -> {
                blinky.placeAtTile(-1, 20);
                blinky.setMoveDir(Direction.RIGHT);
                blinky.setWishDir(Direction.RIGHT);
                blinky.playAnimation(ANIM_BLINKY_NAKED);
            }
            case ANIMATION_START + 700 -> context().gameController().letCurrentGameStateExpire();
            default -> {}
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorRenderer != null) {
            actorsInZOrder.forEach(actor -> actorRenderer.drawActor(actor));
        }
    }
}