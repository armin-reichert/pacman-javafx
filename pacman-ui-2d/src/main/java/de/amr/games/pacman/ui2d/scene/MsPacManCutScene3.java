/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.ui2d.rendering.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;

import static de.amr.games.pacman.lib.Globals.t;

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
public class MsPacManCutScene3 extends GameScene2D {

    private MsPacManCutScene3Controller intermission;
    private ClapperboardAnimation clapAnimation;
    private SpriteAnimation storkAnimation;
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
        intermission = new MsPacManCutScene3Controller();
        intermission.msPacMan.setAnimations(new MsPacManGamePacAnimations(intermission.msPacMan, ss));
        intermission.pacMan.setAnimations(new MsPacManGamePacAnimations(intermission.pacMan, ss));
        storkAnimation = ss.createStorkFlyingAnimation();
        storkAnimation.start();
        clapAnimation = new ClapperboardAnimation("3", "JUNIOR");
        clapAnimation.start();
        intermission.changeState(MsPacManCutScene3Controller.STATE_FLAP, TickTimer.INDEFINITE);
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
        spriteRenderer.drawPac(g, intermission.msPacMan);
        spriteRenderer.drawPac(g, intermission.pacMan);
        spriteRenderer.drawEntitySprite(g, intermission.stork, storkAnimation.currentSprite());
        spriteRenderer.drawEntitySprite(g, intermission.bag,
            intermission.bagOpen ? ss.juniorPacSprite() : ss.blueBagSprite());
        drawLevelCounter(g);
    }
}