package com.qendolin.betterclouds.clouds;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class JenkinsHashTest {
    @Test
    void generatesJenkinsHash() throws IOException {
        int seed = new Random().nextInt();
        BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int value = (int) (0xFF * Sampler.hashToFloat(seed, x, y));
                image.setRGB(x, y, value << 16 | value << 8 | value);
            }
        }

        File output = new File("hash_test.png");
        if (!output.exists() && !output.createNewFile()) throw new RuntimeException("File creation failed");
        ImageIO.write(image, "png", output);

        System.out.println("Hash visualizer created; noise should be pretty uniform");
    }
}
