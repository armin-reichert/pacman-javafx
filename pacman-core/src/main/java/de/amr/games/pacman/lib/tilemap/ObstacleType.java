package de.amr.games.pacman.lib.tilemap;

import java.util.Set;
import java.util.stream.Stream;

/**
 * The encoding depends on the byte values in interface {@link Tiles}!
 */
public enum ObstacleType {
    CROSS_SHAPE,
    F_SHAPE,
    H_SHAPE,
    L_SHAPE(
            "dgegfceb", "dcgfdfeb", "dcgbfege", "dfdgbfce", // 8 segments
            "dcgbfegce", "dcfdgbfce", "dcgfcdfeb", "dgecgfceb", // 9 segments
            "dgbegfceb", "dcgfdbfeb", "dcgbfebge", "dfbdgbfce", // 9 segments
            "dcgbfebgce", "dcfbdgbfce", "dgbecgfceb", "dcgfcdbfeb" // 10 segments
    ),
    O_SHAPE,
    S_SHAPE,
    T_SHAPE("dgbecgfcdbfeb", "dcfbdgbfebgce", "dcgfcdbfebgce", "dcfbdgbecgfce"),
    U_SHAPE,
    ANY;

    ObstacleType(String... encodingStrings) {
        encodings = Set.of(encodingStrings);
    }

    private final Set<String> encodings;

    public Stream<String> encodings() {
        return encodings.stream();
    }

    public boolean matches(String encoding) {
        return encodings.contains(encoding);
    }
}
