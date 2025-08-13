/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_ARCADE_INSERT_COIN;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_ARCADE_START_GAME;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_ORANGE;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    private RectShort livesCounterSprite;
    private MidwayCopyright midwayCopyright;

    public ArcadeMsPacMan_StartScene(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        gameContext().game().hudData().credit(true).score(true).levelCounter(true).livesCounter(false);

        midwayCopyright = new MidwayCopyright(ui.currentConfig().localAssetImage("logo.midway"));
        midwayCopyright.setPosition(TS * 6, TS * 28);
        midwayCopyright.show();

        @SuppressWarnings("unchecked") var spriteSheet = (SpriteSheet<SpriteID>) ui.currentConfig().spriteSheet();
        livesCounterSprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);

        actionBindings.assign(ACTION_ARCADE_INSERT_COIN, ui.actionBindings());
        actionBindings.assign(ACTION_ARCADE_START_GAME, ui.actionBindings());
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {}

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public void drawSceneContent() {
        ctx().setFill(ARCADE_ORANGE);
        ctx().setFont(scaledArcadeFont8());
        gameRenderer.fillTextAtScaledPosition("PUSH START BUTTON", TS*6, TS*16);
        gameRenderer.fillTextAtScaledPosition("1 PLAYER ONLY", TS*8, TS*18);
        gameRenderer.fillTextAtScaledPosition("ADDITIONAL    AT 10000", TS*2, TS*25);
        gameRenderer.drawSpriteScaled(livesCounterSprite, TS*13, TS*23 + 1);
        gameRenderer.fillTextAtScaledPosition("PTS", scaledArcadeFont6(), TS*25, TS*25);
        gameRenderer.drawActor(midwayCopyright);
    }
}