/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.startscene;

import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.pacmanfx.uilib.ArcadeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

public class ArcadeMsPacMan_StartScene extends AbstractGameScene {

    private final List<Renderable> texts = List.of(
        createText("PUSH START BUTTON",      ArcadeColor.ORANGE.color(), 8,  6, 16),
        createText("1 PLAYER ONLY",          ArcadeColor.ORANGE.color(), 8,  8, 18),
        createText("ADDITIONAL    AT 10000", ArcadeColor.ORANGE.color(), 8,  2, 25),
        createText("PTS",                    ArcadeColor.ORANGE.color(), 6, 25, 25)
    );

    private final ImageView msPacManImageView;
    private final ImageView copyrightImageView;
    private final List<TextView> copyrightTexts = new ArrayList<>();

    public ArcadeMsPacMan_StartScene() {
        // Add 2D rendering support
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());


        msPacManImageView = new ImageView();
        msPacManImageView.image().setImage(ArcadeMsPacMan_SpriteSheet.instance().createImage(SpriteID.LIVES_COUNTER_SYMBOL));
        msPacManImageView.pos().set(13 * TS, 23.5 * TS);
        msPacManImageView.show();

        copyrightImageView = new ImageView();
        copyrightImageView.show();
        copyrightImageView.pos().set(tilesPx(6), tilesPx(28));

        copyrightTexts.add(createText("©",             ArcadeColor.RED.color(), 8, 11, 30.125f));
        copyrightTexts.add(createText("MIDWAY MFG CO", ArcadeColor.RED.color(), 8, 13, 30));
        copyrightTexts.add(createText("1980/1981",     ArcadeColor.RED.color(), 8, 14, 32));
        copyrightTexts.forEach(TextView::show);
    }

    @Override
    protected void onAppConnected() {
        final AssetMap assets = app().variantManager().currentRuntime().uiConfig().assets();
        copyrightImageView.image().setImage(assets.image("logo.midway"));
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            texts,
            msPacManImageView,
            copyrightImageView,
            copyrightTexts
        );
    }

    @Override
    public void onActivate() {
        // Bind "insert coin" + "start game" actions
        final Arcade_Actions actions = app().variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registry().registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
        actionBindings().registry().dispose();
    }

    @Override
    public void onTick(GameContext game) {}
}