package eu.tghd.worldguard;

import net.fabricmc.api.ModInitializer;
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

import java.util.Objects;


public class Worldguard implements ModInitializer {

    @Override
    public void onInitialize() {
        LuckPerms lp = LuckPermsProvider.get();

        // Hitting Blocks (Lectern & Chiseled Bookshelf)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer playerServer) {
                if (world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.LECTERN
                        || world.getBlockState(hitResult.getBlockPos()).getBlock() == Blocks.CHISELED_BOOKSHELF) {
                    if (!hasPermission(playerServer, lp)) {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        // Attacking Blocks (Farmland)
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (player instanceof ServerPlayer playerServer) {
                if (world.getBlockState(pos).getBlock() == Blocks.FARMLAND) {
                    if (!hasPermission(playerServer, lp)) {
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
                        || entity.getType() == EntityType.GLOW_ITEM_FRAME) {
                    if (!hasPermission(playerServer, lp)) {
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

        return Objects.requireNonNull(lp.getUserManager().getUser(player.getUUID()))
                .getCachedData()
                .getPermissionData(qp)
                .checkPermission("group.admin")
                .asBoolean()
                || Objects.requireNonNull(lp.getUserManager().getUser(player.getUUID()))
                .getCachedData()
                .getPermissionData(qp)
                .checkPermission("group.mod")
                .asBoolean();

    }
}
