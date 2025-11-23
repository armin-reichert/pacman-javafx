/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUD;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.MsPacMan;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.PacMan;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_CutScene3_Renderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUD_Renderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class TengenMsPacMan_CutScene3 extends GameScene2D {

    private static final int GROUND_Y = TS * 24;
    private static final int RIGHT_BORDER = TS * 30;

    private TengenMsPacMan_HUD_Renderer hudRenderer;
    private TengenMsPacMan_CutScene3_Renderer sceneRenderer;

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
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = GameScene2D_Renderer.configureRendererForGameScene(
            (TengenMsPacMan_HUD_Renderer) uiConfig.createHUDRenderer(canvas), this);

        sceneRenderer = GameScene2D_Renderer.configureRendererForGameScene(
            new TengenMsPacMan_CutScene3_Renderer(this, canvas, uiConfig.spriteSheet()), this);
    }

    @Override
    public TengenMsPacMan_HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public TengenMsPacMan_CutScene3_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public Stork stork() {
        return stork;
    }

    public Bag flyingBag() {
        return flyingBag;
    }

    public boolean darkness() {
        return darkness;
    }

    @Override
    public void doInit() {
        TengenMsPacMan_HUD hud = (TengenMsPacMan_HUD) context().currentGame().hud();
        hud.creditVisible(false).scoreVisible(false).levelCounterVisible(true).livesCounterVisible(false);
        hud.showGameOptions(false);

        actionBindings.addKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, ui.joypad().key(JoypadButton.START));

        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 3, "JUNIOR");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = new MsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());

        pacMan = new PacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        stork = new Stork(spriteSheet);
        flyingBag = new Bag(spriteSheet);

        darkness = false;

        ui.soundManager().play(SoundID.INTERMISSION_3);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(SoundID.INTERMISSION_3);
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
                pacMan.selectAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_PAC_MAN_MUNCHING);
                pacMan.show();

                msPacMan.setMoveDir(Direction.RIGHT);
                msPacMan.setPosition(TS * 5, GROUND_Y - 4);
                msPacMan.setSpeed(0);
                msPacMan.selectAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                msPacMan.show();

                stork.setPosition(RIGHT_BORDER, TS * 7);
                stork.setVelocity(-0.8f, 0);
                stork.setBagReleasedFromBeak(false);
                stork.playAnimation(Stork.ANIM_ID_FLYING);
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
            case 660 -> context().currentGame().stateMachine().letCurrentStateExpire();
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
    public Vector2i sizeInPx() { return NES_SIZE_PX; }

    @Override
    public void draw() {
        sceneRenderer.draw(this);
        if (hudRenderer != null) {
            var game = context().<TengenMsPacMan_GameModel>currentGame();
            if (game.mapCategory() != MapCategory.ARCADE) {
                hudRenderer.drawHUD(context().currentGame(), game.hud(), sizeInPx().minus(0, 2 * TS));
            }
        }
    }
}