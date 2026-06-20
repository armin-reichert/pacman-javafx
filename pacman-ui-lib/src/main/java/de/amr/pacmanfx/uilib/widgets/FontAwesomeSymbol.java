/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

//TODO incomplete
public enum FontAwesomeSymbol {
    CHEVRON_CIRCLE_LEFT('\uf137'),
    CHEVRON_CIRCLE_RIGHT('\uf138'),
    CUBES('\uf1b3'),
    DEAF('\uf2a4'),
    FLAG('\uf024'),
    PAUSE('\uf04c'),
    TAXI('\uf1ba'),
    USER_SECRET('\uf21b');

    FontAwesomeSymbol(char unicode) {
        this.unicode = unicode;
    }

    public char unicode() {
        return unicode;
    }

    private final char unicode;
}
