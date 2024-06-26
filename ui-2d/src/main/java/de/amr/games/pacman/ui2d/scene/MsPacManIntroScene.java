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
import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
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
        context.actionHandler().showSignature();
        context.setScoreVisible(true);
        intro = new MsPacManIntro();
        spriteRenderer.setSpriteSheet(context.getSpriteSheet(context.game().variant()));
        intro.msPacMan.setAnimations(new MsPacManGamePacAnimations(intro.msPacMan, (MsPacManGameSpriteSheet) spriteRenderer.getSpriteSheet()));
        intro.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
        for (var ghost : intro.ghosts) {
            ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, (MsPacManGameSpriteSheet) spriteRenderer.getSpriteSheet()));
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
        }
        intro.changeState(State.START);
        clearBlueMazeBug();
    }

    @Override
    public void end() {
        context.actionHandler().hideSignature();
        context.soundHandler().stopVoice();
    }

    @Override
    public void update() {
        intro.update();
    }

    @Override
    public void handleKeyboardInput() {
        if (GameKeys.ADD_CREDIT.pressed()) {
            triggerBlueMazeBug();
            context.actionHandler().addCredit();
        } else if (GameKeys.START_GAME.pressed()) {
            context.actionHandler().startGame();
        } else if (GameKeys.CUTSCENES.pressed()) {
            context.actionHandler().startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        var theme = context.theme();
        var font8 = sceneFont(8);
        var tx = intro.titlePosition.x();
        var ty = intro.titlePosition.y();
        var y0 = intro.stopY;
        drawMarquee();
        spriteRenderer.drawText(g, "\"MS PAC-MAN\"", context.theme().color("palette.orange"), font8, tx, ty);
        if (intro.state() == State.GHOSTS_MARCHING_IN) {
            var ghost = intro.ghosts[intro.ghostIndex];
            var color = switch (ghost.id()) {
                case GameModel.RED_GHOST -> theme.color("palette.red");
                case GameModel.PINK_GHOST -> theme.color("palette.pink");
                case GameModel.CYAN_GHOST -> theme.color("palette.cyan");
                case GameModel.ORANGE_GHOST -> theme.color("palette.orange");
                default -> throw new IllegalStateException("Unexpected value: " + ghost.id());
            };
            if (ghost.id() == GameModel.RED_GHOST) {
                spriteRenderer.drawText(g, "WITH", context.theme().color("palette.pale"), font8, tx, y0 + t(3));
            }
            var text = ghost.name().toUpperCase();
            var dx = text.length() < 4 ? t(1) : 0;
            spriteRenderer.drawText(g, text, color, font8, tx + t(3) + dx, y0 + t(6));
        } else if (intro.state() == State.MS_PACMAN_MARCHING_IN || intro.state() == State.READY_TO_PLAY) {
            spriteRenderer.drawText(g, "STARRING", context.theme().color("palette.pale"), font8, tx, y0 + t(3));
            spriteRenderer.drawText(g, "MS PAC-MAN", context.theme().color("palette.yellow"), font8, tx, y0 + t(6));
        }
        for (var ghost : intro.ghosts) {
            spriteRenderer.drawGhost(g, ghost);
        }
        spriteRenderer.drawPac(g, intro.msPacMan);
        drawMsPacManCopyright(t(6), t(28));
        drawLevelCounter(g);
    }

    private void drawMarquee() {
        var on = intro.marqueeState();
        for (int i = 0; i < intro.numBulbs; ++i) {
            g.setFill(on.get(i)
                ? context.theme().color("palette.pale")
                : context.theme().color("palette.red"));
            if (i <= 33) {
                g.fillRect(s(60 + 4 * i), s(148), s(2), s(2));
            } else if (i <= 48) {
                g.fillRect(s(192), s(280 - 4 * i), s(2), s(2));
            } else if (i <= 81) {
                g.fillRect(s(384 - 4 * i), s(88), s(2), s(2));
            } else {
                g.fillRect(s(60), s(4 * i - 236), s(2), s(2));
            }
        }
    }

    /**
     * @see  <a href="http://www.donhodges.com/ms_pacman_bugs.htm">Ms. Pac-Man blue maze bug</a>
     */
    private void triggerBlueMazeBug() {
        if (intro.state() == State.START) { // correct?
            MsPacManGame game = (MsPacManGame) context.game();
            game.blueMazeBug = true;
            Logger.info("Blue maze bug triggered");
        }
    }

    private void clearBlueMazeBug() {
        MsPacManGame game = (MsPacManGame) context.game();
        game.blueMazeBug = false;
        Logger.info("Blue maze bug cleared");
    }
}