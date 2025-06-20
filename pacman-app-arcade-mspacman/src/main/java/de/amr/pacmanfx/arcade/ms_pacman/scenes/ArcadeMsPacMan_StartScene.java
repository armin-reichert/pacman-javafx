/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_ORANGE;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;

public class ArcadeMsPacMan_StartScene extends GameScene2D implements ActionBindingSupport {

    private Sprite livesCounterSprite;
    private MidwayCopyright copyright;

    @Override
    public SpriteGameRenderer gr() {
        return (SpriteGameRenderer) super.gr();
    }

    @Override
    public void doInit() {
        theGame().hud().showCredit(true);
        theGame().hud().showScore(true);
        theGame().hud().showLevelCounter(true);
        theGame().hud().showLivesCounter(false);

        copyright = new MidwayCopyright();
        copyright.setPosition(TS * 6, TS * 28);
        copyright.setColor(ARCADE_RED);
        copyright.setFont(scaledArcadeFont8());
        copyright.show();

        @SuppressWarnings("unchecked") var spriteSheet = (SpriteSheet<SpriteID>) theUI().configuration().spriteSheet();
        livesCounterSprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);

        bindAction(ACTION_ARCADE_INSERT_COIN, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_ARCADE_START_GAME, COMMON_ACTION_BINDINGS);
    }

    @Override
    public void update() {}

    @Override
    public void onCreditAdded(GameEvent e) {
        theSound().playInsertCoinSound();
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public void drawSceneContent() {
        ctx().setFill(ARCADE_ORANGE);
        ctx().setFont(scaledArcadeFont8());
        gr().fillTextAtScaledPosition("PUSH START BUTTON", TS*6, TS*16);
        gr().fillTextAtScaledPosition("1 PLAYER ONLY", TS*8, TS*18);
        gr().fillTextAtScaledPosition("ADDITIONAL    AT 10000", TS*2, TS*25);
        gr().drawSpriteScaled(livesCounterSprite, TS*13, TS*23 + 1);
        gr().fillTextAtScaledPosition("PTS", scaledArcadeFont6(), TS*25, TS*25);
        gr().drawActor(copyright);
    }
}