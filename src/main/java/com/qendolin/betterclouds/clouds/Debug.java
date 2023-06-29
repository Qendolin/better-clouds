package com.qendolin.betterclouds.clouds;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.qendolin.betterclouds.Main;
import net.minecraft.client.gl.GlDebug;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Box;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.lwjgl.opengl.GL32.*;

public class Debug {
    public static int profileInterval = 0;
    public static boolean frustumCulling = false;
    public static boolean generatorPause = false;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NotNull
    public static Optional<DebugTrace> trace = Optional.empty();

    public static final List<Pair<Box, Boolean>> frustumCulledBoxes = new ArrayList<>();

    public static void clearFrustumCulledBoxed() {
        if (frustumCulling) {
            frustumCulledBoxes.clear();
        } else if (!frustumCulledBoxes.isEmpty()) {
            frustumCulledBoxes.clear();
        }
    }

    public static void addFrustumCulledBox(Box box, boolean visible) {
        if (!frustumCulling) return;
        frustumCulledBoxes.add(new Pair<>(box, visible));
    }

    public static void drawFrustumCulledBoxes(Vector3d cam) {
        if (!frustumCulling) return;
        BufferBuilder vertices = Tessellator.getInstance().getBuffer();
        vertices.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        ShaderProgram prevShader = RenderSystem.getShader();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        for (Pair<Box, Boolean> pair : frustumCulledBoxes) {
            Box box = pair.getLeft();
            if (pair.getRight()) {
                drawBox(cam, vertices, box, 0.6f, 1f, 0.5f, 1f);
            } else {
                drawBox(cam, vertices, box, 1f, 0.6f, 0.5f, 1f);
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.setShader(() -> prevShader);
    }

    public static void drawBox(Vector3d cam, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
        // I was having some issues with WorldRenderer#drawBox and sodium, so I've copied a modified version here
        float minX = (float) (box.minX - cam.x);
        float minY = (float) (box.minY - cam.y);
        float minZ = (float) (box.minZ - cam.z);
        float maxX = (float) (box.maxX - cam.x);
        float maxY = (float) (box.maxY - cam.y);
        float maxZ = (float) (box.maxZ - cam.z);
        vertexConsumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(minX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, minY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, minZ).color(red, green, blue, alpha).next();
        vertexConsumer.vertex(maxX, maxY, maxZ).color(red, green, blue, alpha).next();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File writeDebugTrace(DebugTrace snapshot) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        String name = String.format("debug-trace-%s.zip", dateFormat.format(new Date()));
        Path path = Path.of("./better-clouds/", name);
        File file = path.toFile();
        try {
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Main.LOGGER.error("Failed to write debug snapshot: ", e);
            return null;
        }
        try (
            FileOutputStream fos = new FileOutputStream(path.toFile());
            ZipOutputStream zip = new ZipOutputStream(fos)) {
            zip.setLevel(8);
            writeDebugTrace(snapshot, zip);
        } catch (Exception e) {
            file.delete();
            Main.LOGGER.error("Failed to write debug snapshot: ", e);
            return null;
        }
        return file;
    }

    public static void writeDebugTrace(DebugTrace snapshot, ZipOutputStream zip) throws IOException {
        zip.putNextEntry(new ZipEntry("log.txt"));
        StringBuilder logText = new StringBuilder();
        List<DebugTrace.Record> log = ImmutableList.copyOf(snapshot.log);
        for (DebugTrace.Record record : log) {
            logText.append(record.description());
            logText.append("\n");
        }
        zip.write(logText.toString().getBytes(StandardCharsets.UTF_8));

        int frame = 0;
        for (DebugTrace.Record record : log) {
            if (record instanceof DebugTrace.FrameRecord frameRecord) {
                frame = frameRecord.nr();
            }

            if (!record.hasAttachment()) continue;
            zip.putNextEntry(new ZipEntry(String.format("%03d-%s", frame, record.attachmentName())));
            record.writeAttachment(zip);
        }
        zip.close();
    }

    /**
     * Note: This method overrides the global OpenGL debug callback and there is no way to restore it
     */
    public static DebugTrace captureDebugTrace(Consumer<DebugTrace> onFinish) {
        DebugTrace snapshot = new DebugTrace(onFinish);
        // Enable all messages
        Main.glCompat.debugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, null, true);
        Main.glCompat.enableDebugOutputSynchronous();
        Main.glCompat.debugMessageCallback(snapshot::recordGlMessage);

        return snapshot;
    }

    public static class DebugTrace {
        public boolean captureEvents = true;
        public boolean captureFramebuffers = true;
        public boolean captureTextures = true;
        public boolean captureGlMessages = true;

        private final AtomicBoolean recording = new AtomicBoolean();
        private final AtomicBoolean finished = new AtomicBoolean();

        private int frame = 0;

        public final List<Record> log = new ArrayList<>();

        private final Consumer<DebugTrace> onFinish;

        public DebugTrace(Consumer<DebugTrace> onFinish) {
            this.onFinish = onFinish;
        }

        public void startRecording() {
            if (finished.get()) return;
            if (recording.getAndSet(true)) return;
            Debug.trace = Optional.of(this);
        }

        public void stopRecording() {
            if (!recording.getAndSet(false)) return;
            if (finished.getAndSet(true)) return;
            Debug.trace = Optional.empty();
            onFinish.accept(this);
        }

        public int getRecordedFrames() {
            return frame;
        }

        public boolean isRecording() {
            return recording.get();
        }

        public void recordFrame() {
            if (!recording.get()) return;
            frame++;
            log.add(new FrameRecord(frame));
        }

        public void recordEvent(String name) {
            if (!recording.get()) return;
            if (!captureEvents) return;
            log.add(new EventRecord(name));
        }

        public void recordFramebuffer(String name, int id) {
            if (!recording.get()) return;
            if (!captureFramebuffers) return;
            if (!captureTextures) return;
            glBindFramebuffer(GL_READ_FRAMEBUFFER, id);
            int type, attachmentId;
            for (int i = 0; i < glGetInteger(GL_MAX_COLOR_ATTACHMENTS); i++) {
                type = glGetFramebufferAttachmentParameteri(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
                if (type != GL_TEXTURE) continue;
                attachmentId = glGetFramebufferAttachmentParameteri(GL_READ_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
                if (attachmentId <= 0) continue;
                recordRGBATexture2D(String.format("%s-color%d", name, i), attachmentId);
            }

            type = glGetFramebufferAttachmentParameteri(GL_READ_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE);
            attachmentId = glGetFramebufferAttachmentParameteri(GL_READ_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME);
            if (type == GL_TEXTURE && attachmentId > 0) {
                RenderSystem.bindTexture(attachmentId);
                glBindTexture(GL_TEXTURE_2D, attachmentId);
                int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
                int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
                ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * Float.BYTES);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_FLOAT, buffer);
                log.add(new TextureRecord(String.format("%s-depth", name), width, height, 1, buffer));
            }
        }

        public void recordRGBATexture2D(String name, int id) {
            if (!recording.get()) return;
            if (!captureTextures) return;
            RenderSystem.bindTexture(id);
            glBindTexture(GL_TEXTURE_2D, id);
            int width = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
            int height = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4 * Float.BYTES);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_FLOAT, buffer);
            log.add(new TextureRecord(name, width, height, 4, buffer));
        }

        public void recordGlMessage(int source, int type, int id, int severity, int length, long messagePointer, long userParam) {
            if (!recording.get()) return;
            if (!captureGlMessages) return;
            synchronized (log) {
                String message = GLDebugMessageCallback.getMessage(length, messagePointer);
                log.add(new DebugMessageRecord(source, type, id, severity, message));
            }
        }

        public interface Record {
            @NotNull
            String description();

            default boolean hasAttachment() {
                return false;
            }

            default String attachmentName() {
                return null;
            }

            default void writeAttachment(OutputStream out) throws IOException {
                throw new NotImplementedException();
            }
        }

        public record TextureRecord(String name, int width, int height, int channels,
                                    ByteBuffer buffer) implements Record {
            @Override
            public @NotNull String description() {
                return String.format("texture: %s", name);
            }

            @Override
            public boolean hasAttachment() {
                return true;
            }

            @Override
            public String attachmentName() {
                return String.format("texture-%s.f32", name.replaceAll("\\s+", "-").replaceAll("[^A-Za-z0-9_-]", ""));
            }

            @Override
            public void writeAttachment(OutputStream out) throws IOException {
                WritableByteChannel channel = Channels.newChannel(out);

                ByteBuffer header = ByteBuffer.wrap(new byte[3 * Integer.BYTES]);
                header.order(ByteOrder.LITTLE_ENDIAN);
                IntBuffer intHeader = header.asIntBuffer();
                intHeader.put(width);
                intHeader.put(height);
                intHeader.put(channels);

                channel.write(header);
                channel.write(buffer);
            }
        }

        public record EventRecord(String name) implements Record {
            @Override
            public @NotNull String description() {
                return String.format("event: %s", name);
            }
        }

        public record FrameRecord(int nr) implements Record {
            @Override
            public @NotNull String description() {
                return String.format("frame: %d", nr);
            }
        }

        public record DebugMessageRecord(int source, int type, int id, int severity, String message) implements Record {
            @Override
            public @NotNull String description() {
                return String.format("debug_message: [%s] %s #%d from %s: %s", GlDebug.getSeverity(severity), GlDebug.getType(type), id, GlDebug.getSource(source), message);
            }
        }
    }
}
