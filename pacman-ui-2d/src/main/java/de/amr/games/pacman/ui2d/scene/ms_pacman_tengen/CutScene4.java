package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.*;

public class CutScene4 extends GameScene2D {

    static final int LEFT_BORDER = TS;
    static final int RIGHT_BORDER = TS * (NES_TILES_X - 2);

    static final int CLAP_TILE_X = TS * 3;
    static final int CLAP_TILE_Y = TS * 10;

    static final int LOWER_LANE = TS * 26;

    private Pac mrPacMan;
    private Pac msPacMan;

    private MediaPlayer music;
    private ClapperboardAnimation clapAnimation;
    private Color clapTextColor;

    private int t;

    @Override
    protected void doInit() {
        t = 0;
        context.setScoreVisible(false);

        mrPacMan = new Pac();
        msPacMan = new Pac();

        var spriteSheet = (MsPacManTengenGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        mrPacMan.setAnimations(new PacAnimations(spriteSheet));
        msPacMan.setAnimations(new PacAnimations(spriteSheet));

        music = context.sound().makeSound("intermission.4",1.0, false);
        clapTextColor = context.assets().color(assetPrefix(context.gameVariant()) + ".color.clapperboard");
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        if (t == 0) {
            clapAnimation = new ClapperboardAnimation();
            clapAnimation.start();
            music.play();
        }
        else if (t == 130) {
            mrPacMan.setMoveDir(Direction.RIGHT);
            mrPacMan.setPosition(LEFT_BORDER, LOWER_LANE);
            mrPacMan.setSpeed(1f);
            mrPacMan.selectAnimation("pacman_munching");
            mrPacMan.animations().ifPresent(Animations::startCurrentAnimation);
            mrPacMan.show();

            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setSpeed(1f);
            msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
            msPacMan.animations().ifPresent(Animations::startCurrentAnimation);
            msPacMan.show();
        }
        else if (t == 230) {
            mrPacMan.setSpeed(0);
            msPacMan.setSpeed(0);
            mrPacMan.animations().ifPresent(Animations::stopCurrentAnimation);
            msPacMan.animations().ifPresent(Animations::stopCurrentAnimation);
        }
        else if (t == 400) {
            mrPacMan.animations().ifPresent(Animations::startCurrentAnimation);
            msPacMan.animations().ifPresent(Animations::startCurrentAnimation);
        }
        else if (t == 520) {
            // change sprite animation to wink etc
        }
        else if (t == 11*60) {
            context.gameController().changeState(GameState.BOOT);
        }

        mrPacMan.move();
        msPacMan.move();
        clapAnimation.tick();
        ++t;
    }

    @Override
    public Vector2f size() {
        return NES_SIZE;
    }

    @Override
    public void bindGameActions() {}

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        var r = (MsPacManTengenGameRenderer) renderer;
        r.drawClapperBoard(clapAnimation, "THE END", 4,
                r.scaledArcadeFont(TS), clapTextColor, CLAP_TILE_X, CLAP_TILE_Y);
        r.drawAnimatedEntity(msPacMan);
        r.drawAnimatedEntity(mrPacMan);

        if (context.game().level().isPresent()) { // avoid exception in cut scene test mode
            r.setLevelNumberBoxesVisible(false);
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
