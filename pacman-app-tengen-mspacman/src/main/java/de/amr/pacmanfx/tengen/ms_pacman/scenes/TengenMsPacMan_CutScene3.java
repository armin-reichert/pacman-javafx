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
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_CutScene3 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.3";

    private static final int GROUND_Y = TS * 24;
    private static final int RIGHT_BORDER = TS * 30;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag flyingBag;

    private boolean darkness;
    private int t;

    public TengenMsPacMan_CutScene3(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        t = -1;
        darkness = false;

        gameContext().theGame().hud().showScore(false);
        gameContext().theGame().hud().showLevelCounter(true);
        gameContext().theGame().hud().showLivesCounter(false);

        actionBindings.bind(ACTION_LET_GAME_STATE_EXPIRE, theUI().theJoypad().key(JoypadButton.START));

        PacManGames_UIConfig config = theUI().theUIConfiguration();
        var spriteSheet = (TengenMsPacMan_SpriteSheet) config.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 3, "JUNIOR");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(scaledArcadeFont8());
        msPacMan = createMsPacMan(null);
        msPacMan.setAnimations(config.createPacAnimations(msPacMan));
        pacMan = createPacMan(null);
        pacMan.setAnimations(config.createPacAnimations(pacMan));
        stork = new Stork(spriteSheet);
        flyingBag = new Bag(spriteSheet);
    }

    @Override
    protected void doEnd() {
        theUI().theSound().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapperboard.show();
            clapperboard.startAnimation();
            theUI().theSound().play(MUSIC_ID);
        }
        else if (t == 130) {
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
            stork.playAnimation("flying");
            stork.show();
        }
        else if (t == 240) {
            // stork releases bag, bag starts falling
            stork.setVelocity(-1f, 0); // faster, no bag to carry!
            stork.setBagReleasedFromBeak(true);
            flyingBag.setPosition(stork.x() - 15, stork.y() + 8);
            flyingBag.setVelocity(-0.5f, 0);
            flyingBag.setAcceleration(0, 0.1f);
            flyingBag.show();
        }
        else if (t == 320) {
            // reaches ground, starts bouncing
            flyingBag.setVelocity(-0.5f, flyingBag.velocity().y());
        }
        else if (t == 380) {
            flyingBag.setOpen(true);
            flyingBag.setVelocity(Vector2f.ZERO);
            flyingBag.setAcceleration(Vector2f.ZERO);
        }
        else if (t == 640) {
            darkness = true;
        }
        else if (t == 660) {
            gameContext().theGameController().letCurrentGameStateExpire();
            return;
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

        clapperboard.tick();
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @Override
    public TengenMsPacMan_GameRenderer gr() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void draw() {
        gameRenderer.setScaling(scaling());
        clear();
        drawSceneContent();
        if (debugInfoVisibleProperty.get()) {
            drawDebugInfo();
        }
        // draw HUD only for non-Arcade map mode
        var game = gameContext().<TengenMsPacMan_GameModel>theGame();
        if (game.mapCategory() != MapCategory.ARCADE) {
            gameRenderer.drawHUD(gameContext(), game.hud(), sizeInPx().minus(0, 2 * TS));
        }
    }

    @Override
    public void drawSceneContent() {
        if (!darkness) {
            gr().drawVerticalSceneBorders();
            gr().drawActor(clapperboard);
            gr().drawActor(stork);
            gr().drawActor(flyingBag);
            gr().drawActor(msPacMan);
            gr().drawActor(pacMan);
        }
    }
}