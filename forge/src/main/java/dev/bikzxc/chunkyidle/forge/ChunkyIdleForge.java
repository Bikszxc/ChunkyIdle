package dev.bikzxc.chunkyidle.forge;

import dev.bikzxc.chunkyidle.ChunkyIdle;
import net.minecraftforge.fml.common.Mod;

@Mod(ChunkyIdle.MOD_ID)
public class ChunkyIdleForge {
    public ChunkyIdleForge() {
        ChunkyIdle.init();
    }
}
