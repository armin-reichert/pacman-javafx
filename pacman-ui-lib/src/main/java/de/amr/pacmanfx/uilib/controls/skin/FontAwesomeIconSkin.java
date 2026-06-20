/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.controls.skin;

import de.amr.pacmanfx.uilib.controls.FontAwesomeIcon;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.tinylog.Logger;

import java.net.URL;

/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
public class FontAwesomeIconSkin extends SkinBase<FontAwesomeIcon> {

    private static final String AWESOME_FONT_PATH = "fonts/Font Awesome 7 Free-Solid-900.otf";

    private static final Font AWESOME_FONT = loadAwesomeFont();

    private static Font loadAwesomeFont() {
        Font font;
        final int size = FontAwesomeIcon.DEFAULT_SIZE;
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

    public FontAwesomeIconSkin(FontAwesomeIcon control) {
        super(control);

        final StackPane container = new StackPane();
        container.getStyleClass().add(FontAwesomeIcon.DEFAULT_STYLE_CLASS + "-container");

        final Text text = new Text();
        text.setText(String.valueOf(control.symbol().unicode()));
        text.fillProperty().bind(control.fillProperty());
        text.fontProperty().bind(container.prefHeightProperty()
            .map(height -> Font.font(AWESOME_FONT.getFamily(), height.doubleValue())));
        text.opacityProperty().bind(control.opacityProperty());
        text.visibleProperty().bind(control.visibleProperty());
        text.managedProperty().bind(control.visibleProperty());

        container.getChildren().add(text);
        getChildren().add(container);
    }
}
