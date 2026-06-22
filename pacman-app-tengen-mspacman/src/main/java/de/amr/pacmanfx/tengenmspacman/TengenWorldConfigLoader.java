/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.ui.config.SettingsLoader;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds support for color specifications of the form <code>nes(0xhh)</code> which references
 * a color from the NES palette.
 */
public class TengenWorldConfigLoader extends SettingsLoader {

    protected Gson createParser() {
        return new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorAdapter())
            //.setStrictness(Strictness.LENIENT)
            .create();
    }

    private static final Pattern NES_COLOR_PATTERN = Pattern.compile("^nes\\(0x([0-9A-Fa-f]{2})\\)$");

    private static class ColorAdapter extends TypeAdapter<Color> {
        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            final String text = in.nextString();
            final Matcher m = NES_COLOR_PATTERN.matcher(text);
            if (m.matches()) {
                int index = Integer.parseInt(m.group(1), 16); // parse the two digits as hex digits
                return NES_Palette.color(index);
            }
            try {
                return Color.web(text);
            } catch (IllegalArgumentException x) {
                Logger.error(String.format("Invalid color: %s", text));
                return Color.WHITE;
            }
        }
    }
}