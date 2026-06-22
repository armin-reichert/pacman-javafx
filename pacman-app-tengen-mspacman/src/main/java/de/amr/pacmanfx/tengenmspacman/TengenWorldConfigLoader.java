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
import de.amr.pacmanfx.ui.config.UISettings;
import de.amr.pacmanfx.ui.config.WorldConfigLoader;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TengenWorldConfigLoader extends WorldConfigLoader {

    protected Gson createParser() {
        return new GsonBuilder()
            .registerTypeAdapter(Color.class, new ColorAdapter())
            //.setStrictness(Strictness.LENIENT)
            .create();
    }

    private static final Pattern NES_COLOR_PATTERN = Pattern.compile("nes(0x)?([0-9a-f]{2})");

    public static class ColorAdapter extends TypeAdapter<Color> {
        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            final String s = in.nextString();
            final Matcher m = NES_COLOR_PATTERN.matcher(s);
            if (m.matches()) {
                int index = Integer.parseInt(m.group(1));
                return NES_Palette.color(index);
            }
            return Color.web(s);
        }
    }
}