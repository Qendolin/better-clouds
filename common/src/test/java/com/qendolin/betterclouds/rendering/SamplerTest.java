package com.qendolin.betterclouds.rendering;

import com.qendolin.betterclouds.generator.Sampler;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SamplerTest {
    @Test
    void samplesAreFiniteAndDeterministicAcrossRegions() {
        Sampler first = new Sampler();
        Sampler second = new Sampler();
        boolean hasClouds = false;
        boolean hasClearSky = false;
        for (int x = -8192; x <= 8192; x += 127) {
            for (int z = -8192; z <= 8192; z += 131) {
                float value = first.sample(x, z, 0.8f, 0f, 0.5f);
                assertTrue(Float.isFinite(value), "Non-finite cloud density");
                assertEquals(value, second.sample(x, z, 0.8f, 0f, 0.5f));
                hasClouds |= value > 0;
                hasClearSky |= value == 0;
            }
        }
        assertTrue(hasClouds, "Noise must generate clouds");
        assertTrue(hasClearSky, "Noise must leave clear sky");
    }

    @Test
    void generatesSamplerImage() throws IOException {
        float scale = 0.5f;

        Sampler s = new Sampler();
        BufferedImage image = new BufferedImage(2400, 1600, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int value = (int) Math.max(0, 0xFF * s.sample(x, y, 0.8f, 0f, scale));
                image.setRGB(x, y, value << 16 | value << 8 | value);
            }
        }

        File output = new File("build/test-artifacts/sampler_options/multi_octave_sampler_test.png");
        Files.createDirectories(output.toPath().getParent());
        ImageIO.write(image, "png", output);
        System.out.printf("Sample map created; 1 pixel ≈ %s blocks", new DecimalFormat("#.#").format(1 / scale));
    }
}
