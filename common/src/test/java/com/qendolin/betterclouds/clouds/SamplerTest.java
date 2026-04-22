package com.qendolin.betterclouds.clouds;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class SamplerTest {
    @Test
    void generatesSamplerImage() throws IOException {
        float scale = 0.5f;

        Sampler s = new Sampler(1337);
        BufferedImage image = new BufferedImage(2400, 1600, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int value = (int) (0xFF * s.sample(x, y, 1f, 1f, scale));
                assert value >= 0;
                image.setRGB(x, y, value << 16 | value << 8 | value);
            }
        }

        File output = new File("sampler_options/sampler_test.png");
        if (!output.exists() && !output.createNewFile()) throw new RuntimeException("File creation failed");
        ImageIO.write(image, "png", output);
        System.out.printf("Sample map created; 1 pixel ≈ %s blocks", new DecimalFormat("#.#").format(1 / scale));
    }
}
