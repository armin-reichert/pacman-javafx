/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.InfoBox;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class InfoBoxJoypad extends InfoBox {

    private static final ResourceManager LOCAL_RESOURCES = () -> GameUI_Implementation.class;
    private static final Font CONTENT_FONT = Font.font("Monospace", 16);

    public InfoBoxJoypad(GameUI ui) {
        super(ui);
    }

    public void init(GameUI ui) {
        final var joypad = TengenMsPacMan_UIConfig.JOYPAD.currentKeyBinding();
        final var imageNesController = new ImageView(LOCAL_RESOURCES.loadImage("graphics/nes-controller.jpg"));

        setContentTextFont(CONTENT_FONT);

        addRow(imageNesController);
        addDynamicLabeledValue("[SELECT]", () -> "%s".formatted(
            joypad.key(JoypadButton.SELECT).getDisplayText())
        );
        addDynamicLabeledValue("[START]", () -> "%s".formatted(
                joypad.key(JoypadButton.START).getDisplayText())
        );
        addDynamicLabeledValue("[B]  [A]", () -> "%s   %s".formatted(
            joypad.key(JoypadButton.B).getDisplayText(),
            joypad.key(JoypadButton.A).getDisplayText())
        );
        addDynamicLabeledValue("UP/DOWN/LEFT/RIGHT", () -> "%s  %s  %s  %s".formatted(
            joypad.key(JoypadButton.UP).getDisplayText(),
            joypad.key(JoypadButton.DOWN).getDisplayText(),
            joypad.key(JoypadButton.LEFT).getDisplayText(),
            joypad.key(JoypadButton.RIGHT).getDisplayText())
        );
    }
}