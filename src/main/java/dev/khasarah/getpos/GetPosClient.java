package dev.khasarah.getpos;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GetPosClient implements ClientModInitializer {
    private static final Identifier GETPOS_LAYER_ID = Identifier.of("getpos:coordinates");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(this::registerClientCommand);
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(DrawContext drawContext, RenderTickCounter tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.currentScreen != null) return;

        var textRenderer = client.textRenderer;
        var player = client.player;

        String direction = getFacingDirection(player.getYaw());
        String coords = String.format("§e%.1f %.1f %.1f §f| §e%s", player.getX(), player.getY(), player.getZ(), direction);

        int x = (drawContext.getScaledWindowWidth() - textRenderer.getWidth(coords)) / 2;
        int y = drawContext.getScaledWindowHeight() - 60;

        drawContext.drawText(textRenderer, coords, x, y, 0xFFFFFF, true);
    }

    private String getFacingDirection(float yaw) {
        float normalizedYaw = (yaw % 360 + 360) % 360;

        if (normalizedYaw >= 45 && normalizedYaw < 135) { // Нужно сместить по часовой стрелки
            return "West";
        } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
            return "North";
        } else if (normalizedYaw >= 225 && normalizedYaw < 315) {
            return "East";
        } else {
            return "South";
        }
    }


    private void registerClientCommand(
            CommandDispatcher<FabricClientCommandSource> dispatcher,
            CommandRegistryAccess registryAccess
    ) {
        dispatcher.register(ClientCommandManager.literal("getpos")
                .executes(this::executeGetPos));
    }

    private int executeGetPos(CommandContext<FabricClientCommandSource> context) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player != null) {
            var pos = player.getPos();
            player.sendMessage(Text.literal(String.format(
                    "§a| §7Your position:§e %.1f %.1f %.1f",
                    pos.x, pos.y, pos.z
            )), false);
        }

        return 1;
    }
}
