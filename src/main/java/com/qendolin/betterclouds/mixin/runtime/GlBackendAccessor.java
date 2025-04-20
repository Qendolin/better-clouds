package com.qendolin.betterclouds.mixin.runtime;

//? if >=1.21.5 {

/*import com.mojang.blaze3d.systems.CommandEncoder;
import net.minecraft.client.gl.GlBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("UnusedMixin")
@Mixin(GlBackend.class)
public interface GlBackendAccessor {
    @Accessor("commandEncoder")
    CommandEncoder getCommandEncoder();
}

*///?}