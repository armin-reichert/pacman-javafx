/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES_JoypadButtonID;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.ClapperboardAnimation;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_PacAnimations;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.uilib.SpriteAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_PAC_MUNCHING;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet.BLUE_BAG_SPRITE;
import static de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet.JUNIOR_PAC_SPRITE;
import static de.amr.games.pacman.ui.Globals.THE_UI;

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

    static final int CLAP_TILE_X = TS * 3;
    static final int CLAP_TILE_Y = TS * 10;

    static final int STORK_Y = TS * 7;
    static final int GROUND_Y = TS * 24;
    static final int RIGHT_BORDER = TS * (NES_TILES.x() - 2);

    private MediaPlayer music;
    private Pac mrPacMan;
    private Pac msPacMan;
    private Actor2D stork;
    private Actor2D bagWithJunior;

    private boolean bagReleased;
    private boolean bagOpen;
    private boolean darkness;

    private TengenMsPacMan_SpriteSheet spriteSheet;
    private ClapperboardAnimation clapAnimation;
    private SpriteAnimation storkAnimation;

    private int t;

    @Override
    public void bindGameActions() {
        bind(THE_GAME_CONTROLLER::terminateCurrentState, THE_UI.keyboard().currentJoypadKeyBinding().key(NES_JoypadButtonID.START));
    }

    @Override
    public void doInit() {
        t = -1;
        THE_UI.setScoreVisible(false);

        mrPacMan = new Pac();
        msPacMan = new Pac();
        stork = new Actor2D();
        bagWithJunior = new Actor2D();
        bagWithJunior.hide();

        spriteSheet = (TengenMsPacMan_SpriteSheet) THE_UI.configurations().current().spriteSheet();
        mrPacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        msPacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));

        music = THE_UI.sound().makeSound("intermission.3");
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            darkness = false;
            clapAnimation = new ClapperboardAnimation();
            clapAnimation.start();
            music.play();
        }
        else if (t == 130) {
            mrPacMan.setMoveDir(Direction.RIGHT);
            mrPacMan.setPosition(TS * 3, GROUND_Y - 4);
            mrPacMan.selectAnimation("pacman_munching");
            mrPacMan.show();

            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setPosition(TS * 5, GROUND_Y - 4);
            msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
            msPacMan.show();

            bagOpen = false;

            stork.setPosition(RIGHT_BORDER, STORK_Y);
            stork.setVelocity(-0.8f, 0);
            stork.show();
            storkAnimation = spriteSheet.createStorkFlyingAnimation();
            storkAnimation.play();
            bagReleased = false;
        }
        else if (t == 270) {
            // stork releases bag, bag starts falling
            stork.setVelocity(-1f, 0);
            bagReleased = true;
            bagWithJunior.setPosition(stork.posX(), stork.posY() + 8);
            bagWithJunior.setVelocity(-0.25f, 0);
            bagWithJunior.setAcceleration(0, 0.1f);
            bagWithJunior.show();
        }
        else if (t == 320) {
            // reaches ground, starts bouncing
            bagWithJunior.setVelocity(-1.0f, bagWithJunior.velocity().y());
        }
        else if (t == 380) {
            bagOpen = true;
            bagWithJunior.setVelocity(Vector2f.ZERO);
            bagWithJunior.setAcceleration(Vector2f.ZERO);
        }
        else if (t == 640) {
            darkness = true;
        }
        else if (t == 660) {
            THE_GAME_CONTROLLER.terminateCurrentState();
            return;
        }

        stork.move();
        if (!bagOpen) {
            bagWithJunior.move();
            Vector2f bv = bagWithJunior.velocity();
            if (bagWithJunior.position().y() > GROUND_Y) {
                bagWithJunior.setPosY(GROUND_Y);
                bagWithJunior.setVelocity(0.9f * bv.x(), -0.3f * bv.y());
            }
        }

        clapAnimation.tick();
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public void drawSceneContent() {
        if (darkness) {
            return;
        }
        var r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();
        r.drawClapperBoard(clapAnimation, "JUNIOR", 3, CLAP_TILE_X, CLAP_TILE_Y);
        r.drawStork(storkAnimation, stork, bagReleased);
        r.drawAnimatedActor(msPacMan);
        r.drawAnimatedActor(mrPacMan);
        if (bagWithJunior.isVisible()) {
            if (bagOpen) {
                r.drawActorSprite(bagWithJunior, JUNIOR_PAC_SPRITE);
            } else {
                r.drawActorSprite(bagWithJunior, BLUE_BAG_SPRITE);
            }
        }
        if (game().level().isPresent()) { // avoid exception in cut scene test mode
            r.setLevelNumberBoxesVisible(false);
            r.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 3 * TS);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y());
        gr.ctx().setFill(Color.WHITE);
        gr.ctx().setFont(GameRenderer.DEBUG_FONT);
        gr.ctx().fillText("Tick " + t, 20, 20);
    }
}