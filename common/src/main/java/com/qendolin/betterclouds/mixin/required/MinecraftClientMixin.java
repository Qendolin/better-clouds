package com.qendolin.betterclouds.mixin.required;

import com.qendolin.betterclouds.telemetry.IssueReportManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen onSetScreen(Screen screen) {
        if(screen != null)
            return screen;

        return IssueReportManager.popQueuedScreen();
    }
}
