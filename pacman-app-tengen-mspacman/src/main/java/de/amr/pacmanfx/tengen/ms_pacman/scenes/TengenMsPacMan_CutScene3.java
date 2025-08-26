/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ActorRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationManager.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class TengenMsPacMan_CutScene3 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.3";

    private static final int GROUND_Y = TS * 24;
    private static final int RIGHT_BORDER = TS * 30;

    private TengenMsPacMan_HUDRenderer hudRenderer;
    private TengenMsPacMan_ActorRenderer actorRenderer;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag flyingBag;

    private boolean darkness;

    public TengenMsPacMan_CutScene3(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = (TengenMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas);
        actorRenderer = (TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas);
        debugInfoRenderer = new DefaultDebugInfoRenderer(ui, canvas);

        bindRendererProperties(hudRenderer, actorRenderer, debugInfoRenderer);

        context().game().hud().creditVisible(false).scoreVisible(false).levelCounterVisible(true).livesCounterVisible(false);

        actionBindings.bind(ACTION_LET_GAME_STATE_EXPIRE, ui.joypad().key(JoypadButton.START));

        var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 3, "JUNIOR");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(actorRenderer.arcadeFontTS());
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(msPacMan));

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(pacMan));

        stork = new Stork(spriteSheet);
        flyingBag = new Bag(spriteSheet);

        darkness = false;

        ui.soundManager().play(MUSIC_ID);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        final int t = (int) context().gameState().timer().tickCount();
        clapperboard.tick();

        switch (t) {
            case 130 -> {
                pacMan.setMoveDir(Direction.RIGHT);
                pacMan.setPosition(TS * 3, GROUND_Y - 4);
                pacMan.setSpeed(0);
                pacMan.selectAnimation(ANIM_PAC_MAN_MUNCHING);
                pacMan.show();

                msPacMan.setMoveDir(Direction.RIGHT);
                msPacMan.setPosition(TS * 5, GROUND_Y - 4);
                msPacMan.setSpeed(0);
                msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
                msPacMan.show();

                stork.setPosition(RIGHT_BORDER, TS * 7);
                stork.setVelocity(-0.8f, 0);
                stork.setBagReleasedFromBeak(false);
                stork.playAnimation(Stork.ANIM_FLYING);
                stork.show();
            }
            case 240 -> {
                // stork releases bag, bag starts falling
                stork.setVelocity(-1f, 0); // faster, no bag to carry!
                stork.setBagReleasedFromBeak(true);
                flyingBag.setPosition(stork.x() - 15, stork.y() + 8);
                flyingBag.setVelocity(-0.5f, 0);
                flyingBag.setAcceleration(0, 0.1f);
                flyingBag.show();
            }
            case 320 -> // reaches ground, starts bouncing
                flyingBag.setVelocity(-0.5f, flyingBag.velocity().y());
            case 380 -> {
                flyingBag.setOpen(true);
                flyingBag.setVelocity(Vector2f.ZERO);
                flyingBag.setAcceleration(Vector2f.ZERO);
            }
            case 640 -> darkness = true;
            case 660 -> context().gameController().letCurrentGameStateExpire();
        }

        stork.move();

        if (!flyingBag.isOpen()) {
            flyingBag.move();
            Vector2f velocity = flyingBag.velocity();
            if (flyingBag.y() > GROUND_Y) {
                flyingBag.setY(GROUND_Y);
                flyingBag.setVelocity(0.9f * velocity.x(), -0.3f * velocity.y());
            }
        }

    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            var game = context().<TengenMsPacMan_GameModel>game();
            if (game.mapCategory() != MapCategory.ARCADE) {
                hudRenderer.drawHUD(context().game(), game.hud(), sizeInPx().minus(0, 2 * TS));
            }
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorRenderer != null && !darkness) {
            Stream.of(clapperboard, stork, flyingBag, msPacMan, pacMan).forEach(actorRenderer::drawActor);
        }
    }
}