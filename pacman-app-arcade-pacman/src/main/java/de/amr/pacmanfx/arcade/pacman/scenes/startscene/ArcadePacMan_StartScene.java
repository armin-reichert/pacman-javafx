/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes.startscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.entities.TextDisplay;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.GlobalFonts;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene {

    private final List<TextDisplay> texts = new ArrayList<>();

    public ArcadePacMan_StartScene(GameAppContext app) {
        super(app);
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());

        final Font arcade6 = GlobalFonts.ARCADE.font(6);
        final Font arcade8 = GlobalFonts.ARCADE.font(8);

        addText("PUSH START BUTTON",       ARCADE_ORANGE, arcade8, tilesPx(6),  tilesPx(17));
        addText("1 PLAYER ONLY",           ARCADE_CYAN,   arcade8, tilesPx(8),  tilesPx(21));
        addText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   arcade8, tilesPx(1),  tilesPx(25));
        addText("PTS",                     ARCADE_ROSE,   arcade6, tilesPx(25), tilesPx(25));
        addText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   arcade8, tilesPx(4),  tilesPx(29));
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(texts);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().currentGameVariantUIConfig()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindingsSupport().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
    }

    private void addText(String text, Color color, Font font, float x, float y) {
        final var textDisplay = new TextDisplay();
        textDisplay.data().setFillColor(color);
        textDisplay.data().setFont(font);
        textDisplay.data().setText(text);
        textDisplay.pos().set(x, y);
        textDisplay.show();
        texts.add(textDisplay);
    }
}