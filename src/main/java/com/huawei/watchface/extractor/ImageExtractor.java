package com.huawei.watchface.extractor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extract images from *.hwt or com.huawei.watchface files
 */
public class ImageExtractor {

    private Logger log = LoggerFactory.getLogger(getClass());

    private final byte[] sign = new byte[]{85, 85, 85, 85};
    private Color currentColor;
    private int currentColorCount;

    public void read(File binFile, File outputDir) throws IOException {

        DataInputStream dataIn = null;
        ZipInputStream zis = null;

        if (binFile.getName().endsWith(".hwt")) {
            boolean foundEntry = false;
            zis = new ZipInputStream(new FileInputStream(binFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null && !foundEntry) {
                if (zipEntry.getName().equals("com.huawei.watchface")) {
                    dataIn = new DataInputStream(zis);
                    foundEntry = true;
                } else {
                    zipEntry = zis.getNextEntry();
                }
            }
            if (!foundEntry) {
                log.error("The archive {} doesn't contain com.huawei.watchface file", binFile);
                return;
            }
        } else {
            dataIn = new DataInputStream(new FileInputStream(binFile));
        }
        int signFlag = 0;

        for (int i = 0; (long) i < binFile.length(); ++i) {
            byte tmp = dataIn.readByte();
            if (sign[signFlag] == tmp) {
                ++signFlag;
            } else {
                signFlag = 0;
            }

            if (signFlag >= 4) {
                log.info("sign position->" + i);
                break;
            }
        }

        dataIn.readByte();
        dataIn.readByte();
        dataIn.readByte();
        dataIn.readByte();
        outputDir.mkdir();
        List<BufferedImage> imsList = new ArrayList<>();
        log.info("Start parsing pictures");
        boolean errflag = false;

        while (!errflag) {
            try {
                BufferedImage img = readImg(dataIn);
                imsList.add(img);
            } catch (Exception var10) {
                errflag = true;
            }
        }

        log.info("Start outputting pictures");

        for (int i = 0; i < imsList.size(); ++i) {
            log.info("Start outputting picture {}", i);
            ImageIO.write((RenderedImage) imsList.get(i), "png", new File(outputDir, i + ".png"));
        }

        log.info("End of output pictures");
        if (zis != null) {
            zis.closeEntry();
            zis.close();
        }
        dataIn.close();
    }

    private BufferedImage readImg(DataInputStream dataIn) throws IOException {
        log.info("Header->{}{}{}{}", Integer.toHexString(dataIn.readUnsignedByte()), Integer.toHexString(dataIn.readUnsignedByte()), Integer.toHexString(dataIn.readUnsignedByte()), Integer.toHexString(dataIn.readUnsignedByte()));
        int width1 = dataIn.readUnsignedByte();
        int width2 = dataIn.readUnsignedByte();
        int height1 = dataIn.readUnsignedByte();
        int height2 = dataIn.readUnsignedByte();
        int width = width1 + (width2 << 8);
        int height = height1 + (height2 << 8);
        log.info("Width->{}, Height->{}", width, height);
        BufferedImage img = new BufferedImage(width, height, 2);

        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                Color c = readColor(dataIn);
                img.setRGB(j, i, c.getRGB());
            }
        }

        return img;
    }

    private Color readColor(DataInputStream dataIn) throws IOException {
        if (currentColorCount > 0) {
            --currentColorCount;
            return currentColor;
        } else {
            int blue = dataIn.readUnsignedByte();
            int green = dataIn.readUnsignedByte();
            int red = dataIn.readUnsignedByte();
            int alpha = dataIn.readUnsignedByte();
            if (blue == 137 && green == 103 && red == 69 && alpha == 35) {
                blue = dataIn.readUnsignedByte();
                green = dataIn.readUnsignedByte();
                red = dataIn.readUnsignedByte();
                alpha = dataIn.readUnsignedByte();
                int l1 = dataIn.readUnsignedByte();
                int l2 = dataIn.readUnsignedByte();
                int l3 = dataIn.readUnsignedByte();
                int l4 = dataIn.readUnsignedByte();
                int count = (l4 << 24) + (l3 << 16) + (l2 << 8) + l1;
                currentColorCount = count - 1;
                currentColor = new Color(red, green, blue, alpha);
                return currentColor;
            } else {
                return new Color(red, green, blue, alpha);
            }
        }
    }
}
