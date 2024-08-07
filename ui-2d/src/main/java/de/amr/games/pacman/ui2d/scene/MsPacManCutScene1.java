/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.ui2d.rendering.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 *
 * @author Armin Reichert
 */
public class MsPacManCutScene1 extends GameScene2D {

    private MsPacManIntermission1 intermission;
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
        intermission = new MsPacManIntermission1();
        intermission.msPac.setAnimations(new MsPacManGamePacAnimations(intermission.msPac, ss));
        intermission.pacMan.setAnimations(new MsPacManGamePacAnimations(intermission.pacMan, ss));
        intermission.inky.setAnimations(new MsPacManGameGhostAnimations(intermission.inky, ss));
        intermission.pinky.setAnimations(new MsPacManGameGhostAnimations(intermission.pinky, ss));
        clapAnimation = new ClapperboardAnimation("1", "THEY MEET");
        clapAnimation.start();
        intermission.changeState(MsPacManIntermission1.STATE_FLAP, 120);
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
        spriteRenderer.drawGhost(g, intermission.inky);
        spriteRenderer.drawGhost(g, intermission.pinky);
        spriteRenderer.drawEntitySprite(g, intermission.heart, ss.heartSprite());
        drawLevelCounter(g);
    }
}