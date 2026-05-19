package de.amr.objparser;

public final class FastFaceRefTokenizer {
    public void split(String ref, int[] out) {
        int len = ref.length();
        int part = 0;
        int start = 0;

        for (int i = 0; i < len; i++) {
            if (ref.charAt(i) == '/') {
                out[part++] = parseInt(ref, start, i);
                start = i + 1;
            }
        }
        out[part] = parseInt(ref, start, len);
    }

    private int parseInt(String s, int start, int end) {
        if (start == end) return Integer.MIN_VALUE;
        int sign = 1;
        int i = start;
        if (s.charAt(i) == '-') {
            sign = -1;
            i++;
        }
        int val = 0;
        while (i < end) {
            val = val * 10 + (s.charAt(i) - '0');
            i++;
        }
        return val * sign;
    }
}
