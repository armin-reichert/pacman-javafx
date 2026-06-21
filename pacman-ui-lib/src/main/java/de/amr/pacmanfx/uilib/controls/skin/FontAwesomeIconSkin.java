/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.controls.skin;

import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import javafx.scene.control.SkinBase;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import java.net.URL;

public class FontAwesomeIconSkin extends SkinBase<FontAwesomeIcon> {

    private static final String AWESOME_FONT_PATH = "fonts/Font Awesome 7 Free-Solid-900.otf";

    private static final Font AWESOME_FONT = loadAwesomeFont();

    private static Font loadAwesomeFont() {
        Font font;
        final int size = FontAwesomeIcon.DEFAULT_ICON_SIZE;
        final URL url = FontAwesomeIcon.class.getResource(AWESOME_FONT_PATH);
        if (url != null) {
            font = Font.loadFont(url.toExternalForm(), size);
            Logger.info("FontAwesome font loaded successfully: {}", font);
        } else {
            font = Font.font(size);
            Logger.error("Could not load Font Awesome fonts, using default: {}", font);
        }
        return font;
    }

    private static Font awesomeFontAtSize(Number size) {
        return Font.font(AWESOME_FONT.getFamily(), size.doubleValue());
    }

    public FontAwesomeIconSkin(FontAwesomeIcon control) {
        super(control);

        final Text text = new Text();
        text.setText(String.valueOf(control.symbol().unicode()));
        text.fillProperty().bind(control.fillProperty());
        text.fontProperty().bind(control.sizeProperty().map(FontAwesomeIconSkin::awesomeFontAtSize));
        text.opacityProperty().bind(control.opacityProperty());
        text.visibleProperty().bind(control.visibleProperty());
        text.managedProperty().bind(control.visibleProperty());

        getChildren().add(text);
    }
}
