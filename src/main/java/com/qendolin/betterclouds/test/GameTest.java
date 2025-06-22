package com.qendolin.betterclouds.test;


import com.qendolin.betterclouds.BetterCloudsStatic;
import com.qendolin.betterclouds.platform.EventHooks;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.text.Text;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GameTest {

    private static final AtomicReference<Throwable> caught = new AtomicReference<>();
    private static final AtomicBoolean finished = new AtomicBoolean();

    public static void onClientExit() {
        if (!GameTestEnabled.ENABLED) return;
        if (caught.get() != null || !finished.get()) {
            BetterCloudsStatic.getLogger().error("\n========================\n    GameTest failed!    \n========================");
            if (caught.get() != null) throw new RuntimeException(caught.get());
            if (!finished.get()) throw new RuntimeException("Test did not finish!");
        } else {
            BetterCloudsStatic.getLogger().info("\n========================\n    GameTest passed!    \n========================");
        }
    }

    public static void run(MinecraftClient client) {
        if (!GameTestEnabled.ENABLED) return;

        // This is very scuffed, don't @ me

        AtomicBoolean stop = new AtomicBoolean(false);
        TestContext ctx = new TestContext(client);

        EventHooks.instance.onClientTick(c -> {
            if (stop.get()) {
                if (c.isRunning()) {
                    c.scheduleStop();
                }
                return;
            }

            ctx.lock.lock();
            try {
                ctx.tick.signalAll();
            } finally {
                ctx.lock.unlock();
            }
        });

        Thread testThread = new Thread(() -> {
            try {
                runTestAsync(ctx);
                finished.set(true);
            } catch (Throwable e) {
                caught.set(e);
            } finally {
                stop.set(true);
            }
        });
        testThread.setName("Test thread");
        testThread.setDaemon(true);
        testThread.start();
    }

    private static class TestContext {

        private final Lock lock = new ReentrantLock();
        private final Condition tick = lock.newCondition();

        private final MinecraftClient client;

        private TestContext(MinecraftClient client) {
            this.client = client;
        }

        public void runOnClient(Consumer<MinecraftClient> consumer) {
            Semaphore semaphore = new Semaphore(0);
            AtomicReference<Throwable> caught = new AtomicReference<>();
            client.execute(() -> {
                try {
                    consumer.accept(client);
                } catch (Throwable e) {
                    caught.set(e);
                } finally {
                    semaphore.release();
                }
            });
            semaphore.acquireUninterruptibly();
            if (caught.get() != null) {
                throw new RuntimeException(caught.get());
            }
        }

        public void waitFor(Predicate<MinecraftClient> consumer, long timeout) {
            while (!consumer.test(client)) {
                waitTick();
                timeout--;
                if (timeout <= 0) {
                    throw new AssertionError("Timed out waiting for predicate");
                }
            }
        }

        public void waitTicks(int ticks) {
            for (int i = 0; i < ticks; i++) {
                waitTick();
            }
        }

        public void waitTick() {
            lock.lock();
            try {
                tick.awaitUninterruptibly();
            } finally {
                lock.unlock();
            }
        }
    }

    private static void runTestAsync(TestContext test) {
        test.waitFor(client -> client.getOverlay() == null, SharedConstants.TICKS_PER_MINUTE);
        test.runOnClient(client -> {
            client.options.getCloudRenderMode().setValue(CloudRenderMode.FANCY);

            boolean exists = client.getLevelStorage().levelExists("Game Test");
            if (!exists) {
                throw new RuntimeException("No world 'Game Test', please create it");
            }
            //? if >1.20.1 {
            client.createIntegratedServerLoader().start("Game Test", () -> {
                throw new RuntimeException("Failed to open 'Game Test' world");
            });
            //?} else {
            /*client.createIntegratedServerLoader().start(null,"Game Test");
             *///?}
        });
        test.waitFor(client -> client.world != null && client.getServer() != null && client.getServer().isRunning(), SharedConstants.TICKS_PER_MINUTE);

        test.waitFor(client -> client.currentScreen == null, SharedConstants.TICKS_PER_MINUTE);
        test.waitTicks(20);
        test.runOnClient(client -> {
            assert client.world != null;
            //? >=1.21.6 {
            client.world.disconnect(Text.translatable("menu.returnToMenu"));
            client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")), false);
            //?} else {
            /*client.world.disconnect();
            client.disconnect(new MessageScreen(Text.translatable("menu.savingLevel")));
            *///?}
        });

        test.waitFor(client -> (client.getServer() == null || client.getServer().isRunning()) && client.world == null, SharedConstants.TICKS_PER_MINUTE);
    }
}