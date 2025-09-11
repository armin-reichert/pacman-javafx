/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ANIM_BIG_PAC_MAN;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;

/**
 * First cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over the screen,
 * then a frightened ghost is chased by a big Pac-Man from left to right.
 */
public class ArcadePacMan_CutScene1 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission";
    private static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;

    private ArcadePacMan_HUDRenderer hudRenderer;
    private ActorRenderer actorRenderer;

    public ArcadePacMan_CutScene1(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        GameUI_Config uiConfig = ui.currentConfig();
        hudRenderer       = configureRenderer(new ArcadePacMan_HUDRenderer(canvas, uiConfig));
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
    public ArcadePacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();

        context().game().hud().creditVisible(false).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        pac = createPac();
        pac.setAnimations(uiConfig.createPacAnimations(pac));

        blinky = createGhost(RED_GHOST_SHADOW);
        blinky.setAnimations(uiConfig.createGhostAnimations(blinky));

        actorsInZOrder.add(pac);
        actorsInZOrder.add(blinky);

        frame = -1;
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
        Logger.info("{} ends", getClass().getSimpleName());
    }

    @Override
    public void update() {
        ++frame;
        if (frame == ANIMATION_START) {
            ui.soundManager().play(MUSIC_ID, 2);

            pac.placeAtTile(29, 20);
            pac.setMoveDir(Direction.LEFT);
            pac.setSpeed(1.25f);
            pac.playAnimation(ANIM_PAC_MUNCHING);
            pac.show();

            blinky.placeAtTile(32, 20);
            blinky.setMoveDir(Direction.LEFT);
            blinky.setWishDir(Direction.LEFT);
            blinky.setSpeed(1.3f);
            blinky.playAnimation(ANIM_GHOST_NORMAL);
            blinky.show();
        }
        else if (frame == ANIMATION_START + 260) {
            blinky.placeAtTile(-2, 20, 4, 0);
            blinky.setMoveDir(Direction.RIGHT);
            blinky.setWishDir(Direction.RIGHT);
            blinky.setSpeed(0.75f);
            blinky.playAnimation(ANIM_GHOST_FRIGHTENED);
        }
        else if (frame == ANIMATION_START + 400) {
            pac.placeAtTile(-3, 18, 0, 6.5f);
            pac.setMoveDir(Direction.RIGHT);
            pac.playAnimation(ANIM_BIG_PAC_MAN);
        }
        else if (frame == ANIMATION_START + 632) {
            context().gameController().letCurrentGameStateExpire();
        }
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
    }

    @Override
    public void drawSceneContent() {
        actorsInZOrder.forEach(actor -> actorRenderer.drawActor(actor));
    }
}