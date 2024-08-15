/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.MsPacManIntro;
import de.amr.games.pacman.controller.MsPacManIntro.State;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameKey;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 *
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

    private MsPacManIntro intro;

    @Override
    public boolean isCreditVisible() {
        return true;
    }

    @Override
    public void init() {
        super.init();

        var sheet = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
        spriteRenderer.setSpriteSheet(sheet);

        context.actionHandler().showSignature();
        context.setScoreVisible(true);
        clearBlueMazeBug();

        intro = new MsPacManIntro();
        intro.msPacMan.setAnimations(new MsPacManGamePacAnimations(intro.msPacMan, sheet));
        intro.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
        for (var ghost : intro.ghosts) {
            ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, sheet));
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
        }
        intro.changeState(State.START);
    }

    @Override
    public void end() {
        context.actionHandler().hideSignature();
        GameSounds.stopVoice();
    }

    @Override
    public void update() {
        intro.update();
    }

    @Override
    public void handleKeyboardInput(ActionHandler handler) {
        if (GameKey.ADD_CREDIT.pressed()) {
            if (intro.state() == State.START) {
                triggerBlueMazeBug();
            }
            handler.addCredit();
        } else if (GameKey.START_GAME.pressed()) {
            handler.startGame();
        } else if (GameKey.CUTSCENES.pressed()) {
            handler.startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        var assets = context.assets();
        var font8 = sceneFont(8);
        var tx = intro.titlePosition.x();
        var ty = intro.titlePosition.y();
        var y0 = intro.stopY;
        drawMarquee();
        spriteRenderer.drawText(g, "\"MS PAC-MAN\"", assets.color("palette.orange"), font8, tx, ty);
        if (intro.state() == State.GHOSTS_MARCHING_IN) {
            var ghost = intro.ghosts[intro.ghostIndex];
            var color = switch (ghost.id()) {
                case GameModel.RED_GHOST -> assets.color("palette.red");
                case GameModel.PINK_GHOST -> assets.color("palette.pink");
                case GameModel.CYAN_GHOST -> assets.color("palette.cyan");
                case GameModel.ORANGE_GHOST -> assets.color("palette.orange");
                default -> throw new IllegalStateException("Unexpected value: " + ghost.id());
            };
            if (ghost.id() == GameModel.RED_GHOST) {
                spriteRenderer.drawText(g, "WITH", assets.color("palette.pale"), font8, tx, y0 + t(3));
            }
            var text = ghost.name().toUpperCase();
            var dx = text.length() < 4 ? t(1) : 0;
            spriteRenderer.drawText(g, text, color, font8, tx + t(3) + dx, y0 + t(6));
        } else if (intro.state() == State.MS_PACMAN_MARCHING_IN || intro.state() == State.READY_TO_PLAY) {
            spriteRenderer.drawText(g, "STARRING", assets.color("palette.pale"), font8, tx, y0 + t(3));
            spriteRenderer.drawText(g, "MS PAC-MAN", assets.color("palette.yellow"), font8, tx, y0 + t(6));
        }
        for (var ghost : intro.ghosts) {
            spriteRenderer.drawGhost(g, ghost);
        }
        spriteRenderer.drawPac(g, intro.msPacMan);
        drawMsPacManCopyright(t(6), t(28));
        drawLevelCounter(g);
    }

    // TODO This is too cryptic
    private void drawMarquee() {
        double xMin = 60, xMax = 192, yMin = 88, yMax = 148;
        for (int i = 0; i < intro.numBulbs; ++i) {
            boolean on = intro.marqueeState().get(i);
            if (i <= 33) { // lower edge left-to-right
                drawLight(xMin + 4 * i, yMax, on);
            } else if (i <= 48) { // right edge bottom-to-top
                drawLight(xMax, 4 * (70 - i), on);
            } else if (i <= 81) { // upper edge right-to-left
                drawLight(4 * (96 - i), yMin, on);
            } else { // left edge top-to-bottom
                drawLight(xMin, 4 * (i - 59), on);
            }
        }
    }

    private void drawLight(double x, double y, boolean on) {
        Color onColor = context.assets().color("palette.pale"), offColor = context.assets().color("palette.red");
        double bulbSize = s(2);
        g.setFill(on ? onColor : offColor);
        g.fillRect(s(x), s(y), bulbSize, bulbSize);
    }

    /**
     * <p>"It is well known that if a credit is inserted at the very beginning of the attract mode,
     * before the red ghost appears under the marquee, the first maze of the game will be colored
     * blue instead of the normal maze color."</p>
     * @see  <a href="http://www.donhodges.com/ms_pacman_bugs.htm">Ms. Pac-Man blue maze bug</a>
     */
    private void triggerBlueMazeBug() {
        MsPacManGame game = (MsPacManGame) context.game();
        game.blueMazeBug = true;
        Logger.info("Blue maze bug triggered");
    }

    private void clearBlueMazeBug() {
        MsPacManGame game = (MsPacManGame) context.game();
        game.blueMazeBug = false;
        Logger.info("Blue maze bug cleared");
    }
}