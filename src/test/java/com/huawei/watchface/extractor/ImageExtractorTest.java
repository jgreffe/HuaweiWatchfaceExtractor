package com.huawei.watchface.extractor;

import java.io.File;

import org.junit.Test;

public class ImageExtractorTest {

    @Test
    public void test() throws Exception {
        ImageExtractor ie = new ImageExtractor();
        ie.read(new File("src/test/resources/com.huawei.watchface.rgb"), new File("target/com.huawei.watchface.rgb_extra"));
        ie.read(new File("src/test/resources/Unnamed.hwt"), new File("target/Unnamed.hwt_extra"));
    }
}
