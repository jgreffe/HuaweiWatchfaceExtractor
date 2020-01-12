package com.huawei.watchface.extractor;

import java.io.File;

/**
 * Extract images from *.hwt or com.huawei.watchface file
 */
public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        File inputFile = null;
        File outputDir = null;

        try {
            String input = args[0];
            if ("-?".equals(input) || "/?".equals(input)) {
                showHelp();
                System.exit(0);
            } else {
                inputFile = new File(input);
                outputDir = new File(inputFile.getAbsolutePath() + "_extra");

                if (args.length > 1) {
                    outputDir = new File(args[1]);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            showHelp();
            System.exit(0);
        }
        try {
            new ImageExtractor().read(inputFile, outputDir);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }

    private static void showHelp() {
        String argFormat="%1$-10s";
        System.out.print("Extract images from HWT or com.huawei.watchface file\n" +
                "Simply drag & drop a file on executable\n\n" +
                "Usage: HuaweiWatchFaceExtractor input [outputDir]\n" +
                "\t"+String.format(argFormat,"input")+"*.hwt or com.huawei.watchface file\n" +
                "\t"+String.format(argFormat, "outputDir")+"optional output directory for extracted images. Defaults to <input>_extra\n");
    }


}
