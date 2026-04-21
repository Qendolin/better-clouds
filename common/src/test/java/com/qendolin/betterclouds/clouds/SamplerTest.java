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
        for (int i = 0; i < Sampler.OCTAVE_OPTIONS.length; i++) {
            Sampler s = new Sampler(new Random().nextInt(), i);
            BufferedImage image = new BufferedImage(1200, 800, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int value = (int) (0xFF * s.sample(x, y, 1f, 0.8f, 0.5f));
                    image.setRGB(x, y, value << 16 | value << 8 | value);
                }
            }

            File output = new File("sampler_options/sampler_test_" + (i + 1) + ".png");
            if (!output.exists() && !output.createNewFile()) throw new RuntimeException("File creation failed");
            ImageIO.write(image, "png", output);
        }
        System.out.println("Sample map created; octaves should match their descriptions in the Javadoc");
    }
}
