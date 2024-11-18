/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.NES_SIZE;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.NES_TILES_X;

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
public class CutScene3 extends GameScene2D {

    static final int STORK_Y = TS * 7;
    static final int LANE_Y = TS * 24;
    static final int RIGHT_BORDER = TS * (NES_TILES_X - 2);

    private MediaPlayer music;
    private Pac mrPacMan;
    private Pac msPacMan;
    private Entity stork;
    private Entity bagOrJunior;

    private boolean bagReleased;
    private boolean juniorVisible;
    private boolean darkness;

    private MsPacManTengenGameSpriteSheet spriteSheet;
    private ClapperboardAnimation clapAnimation;
    private SpriteAnimation storkAnimation;

    private int t;

    @Override
    public void bindGameActions() {
        bind(context -> context.gameController().terminateCurrentState(), context.joypad().keyCombination(NES.Joypad.START));
    }

    @Override
    public void doInit() {
        t = 0;
        context.setScoreVisible(false);

        mrPacMan = new Pac();
        msPacMan = new Pac();
        stork = new Entity();
        bagOrJunior = new Entity();

        music = context.sound().makeSound("intermission.3",1.0, false);

        spriteSheet = (MsPacManTengenGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        mrPacMan.setAnimations(new PacAnimations(spriteSheet));
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        if (t == 0) {
            darkness = false;
            clapAnimation = new ClapperboardAnimation();
            clapAnimation.start();
            music.play();
        }
        else if (t == 130) {
            mrPacMan.setMoveDir(Direction.RIGHT);
            mrPacMan.setPosition(TS * 3, LANE_Y - 4);
            mrPacMan.selectAnimation("pacman_munching");
            mrPacMan.show();

            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setPosition(TS * 5, LANE_Y - 4);
            msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
            msPacMan.show();

            juniorVisible = false;

            stork.setPosition(RIGHT_BORDER, STORK_Y);
            stork.setVelocity(-0.8f, 0);
            stork.show();
            storkAnimation = spriteSheet.createStorkFlyingAnimation();
            storkAnimation.start();
            bagReleased = false;
        }
        else if (t == 267) {
            // start falling
            bagReleased = true;
            bagOrJunior.setPosition(stork.posX(), stork.posY() + 4);
            bagOrJunior.setVelocity(-0.2f, 2.0f);
            bagOrJunior.show();
        }
        else if (t == 327) {
            // reaches ground
            bagOrJunior.setVelocity(-0.1f, 0); // TODO bounce
        }
        else if (t == 360) {
            juniorVisible = true;
            bagOrJunior.setVelocity(Vector2f.ZERO);
        }
        else if (t == 642) {
            darkness = true;
        }
        else if (t == 654) {
            context.gameController().terminateCurrentState();
            return;
        }

        stork.move();
        bagOrJunior.move();
        clapAnimation.tick();
        ++t;
    }

    @Override
    public Vector2f size() {
        return NES_SIZE;
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        if (darkness) {
            return;
        }
        String assetPrefix = assetPrefix(context.gameVariant());
        Color color = context.assets().color(assetPrefix + ".color.clapperboard");
        var r = (MsPacManTengenGameRenderer) renderer;
        r.drawClapperBoard(clapAnimation, "JUNIOR", 3, r.scaledArcadeFont(TS), color, t(3), t(10));
        r.drawAnimatedEntity(msPacMan);
        r.drawAnimatedEntity(mrPacMan);
        r.drawStork(storkAnimation, stork, bagReleased);
        r.drawSprite(bagOrJunior, juniorVisible
            ? MsPacManTengenGameSpriteSheet.JUNIOR_PAC_SPRITE
            : MsPacManTengenGameSpriteSheet.BLUE_BAG_SPRITE);
        r.setLevelNumberBoxesVisible(false);
        if (context.game().level().isPresent()) { // avoid exception in cut scene test mode
            r.drawLevelCounter(context, size());
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        renderer.ctx().setFill(Color.WHITE);
        renderer.ctx().setFont(Font.font(20));
        renderer.ctx().fillText("Tick " + t, 20, 20);
    }
}