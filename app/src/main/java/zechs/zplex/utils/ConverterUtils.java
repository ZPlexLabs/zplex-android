package zechs.zplex.utils;

import android.annotation.SuppressLint;

public class ConverterUtils {

    @SuppressLint("DefaultLocale")
    public static String getSize(long size) {
        String s;
        double kb = Double.parseDouble(String.valueOf(size)) / 1024;
        double mb = kb / 1024;
        double gb = mb / 1024;
        double tb = gb / 1024;
        if (size < 1024L) {
            s = size + " Bytes";
        } else if (size < 1024L * 1024) {
            s = String.format("%.2f", kb) + " KB";
        } else if (size < 1024L * 1024 * 1024) {
            s = String.format("%.2f", mb) + " MB";
        } else if (size < 1024L * 1024 * 1024 * 1024) {
            s = String.format("%.2f", gb) + " GB";
        } else {
            s = String.format("%.2f", tb) + " TB";
        }
        return s;
    }
}