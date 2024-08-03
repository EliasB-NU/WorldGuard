package eu.tghd.worldguard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;



public class Worldguard implements ModInitializer {
    private final Logger logger = LoggerFactory.getLogger("WorldGuard");
    private LuckPerms lp;

    @Override
    public void onInitialize() {
        // Register a server starting event to safely access LuckPerms
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                lp = LuckPermsProvider.get();
                registerEvents();
                logger.info("Registered WorldGuard events ...");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> logger.info("Shutting Down WorldGuard Mod..."));
    }

    private void registerEvents() {
        // Hitting Blocks (Lectern & Chiseled Bookshelf & Farmland)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer playerServer) {
                if (world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.LECTERN
                        || world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.CHISELED_BOOKSHELF
                        || world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.FARMLAND
                        || world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.RESPAWN_ANCHOR
                        || world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.DRAGON_EGG) {
                    if (hasPermission(playerServer, lp)) {
                        return InteractionResult.FAIL;
                    }
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.PASS;
        });

        // Attacking Blocks (Farmland)
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player instanceof ServerPlayer playerServer) {
                if (world.getBlockState(pos).getBlock() == Blocks.FARMLAND) {
                    if (hasPermission(playerServer, lp)) {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        // Interacting with entities (Armor Stands & Frames)
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer playerServer) {
                if (entity.getType() == EntityType.ARMOR_STAND || entity.getType() == EntityType.ITEM_FRAME
                        || entity.getType() == EntityType.GLOW_ITEM_FRAME
                        || entity.getType() == EntityType.EGG
                        || entity.getType() == EntityType.END_CRYSTAL) {
                    if (hasPermission(playerServer, lp)) {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    private boolean hasPermission(ServerPlayer player, LuckPerms lp) {
        ContextManager cm = lp.getContextManager();
        QueryOptions qp = cm.getQueryOptions(player);

        return !Objects.requireNonNull(lp.getUserManager().getUser(player.getUUID()))
                .getCachedData()
                .getPermissionData(qp)
                .checkPermission("group.admin")
                .asBoolean()
                && !Objects.requireNonNull(lp.getUserManager().getUser(player.getUUID()))
                .getCachedData()
                .getPermissionData(qp)
                .checkPermission("group.mod")
                .asBoolean();
    }
}
