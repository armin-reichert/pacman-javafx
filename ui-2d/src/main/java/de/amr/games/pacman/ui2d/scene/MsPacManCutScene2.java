/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.ui2d.rendering.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class MsPacManCutScene2 extends GameScene2D {

    private MsPacManIntermission2 intermission;
    private ClapperboardAnimation clapAnimation;
    private MsPacManGameSpriteSheet ss;

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit();
    }

    @Override
    public void init() {
        super.init();
        ss = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
        context.setScoreVisible(true);
        intermission = new MsPacManIntermission2();
        intermission.msPac.setAnimations(new MsPacManGamePacAnimations(intermission.msPac, ss));
        intermission.pacMan.setAnimations(new MsPacManGamePacAnimations(intermission.pacMan, ss));
        clapAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapAnimation.start();
        intermission.changeState(MsPacManIntermission2.STATE_FLAP, 120);
    }

    @Override
    public void update() {
        intermission.tick();
        clapAnimation.tick();
    }

    @Override
    public void drawSceneContent() {
        spriteRenderer.drawClapperBoard(g,
            context.assets().font("font.arcade", s(8)),
            context.assets().color("palette.pale"),
            clapAnimation, t(3), t(10));
        spriteRenderer.drawPac(g, intermission.msPac);
        spriteRenderer.drawPac(g, intermission.pacMan);
        drawLevelCounter(g);
    }
}