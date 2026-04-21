package com.qendolin.betterclouds.clouds;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class SamplerTest {
    @Test
    void generatesSamplerImage() throws IOException {
        Sampler s = new Sampler(new Random().nextInt());
        BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int value = (int) (0xFF * s.sample(i, j, 1f, 0.8f, 0.5f));
                image.setRGB(i, j, value << 16 | value << 8 | value);
            }
        }

        File output = new File("sampler_test.png");
        if (!output.exists() && !output.createNewFile()) throw new RuntimeException("File creation failed");
        ImageIO.write(image, "png", output);
        System.out.println("Sample map created");
    }
}
