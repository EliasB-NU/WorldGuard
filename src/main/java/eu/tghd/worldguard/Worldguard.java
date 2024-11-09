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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;



public class Worldguard implements ModInitializer {
    private final Logger logger = LoggerFactory.getLogger("WorldGuard");
    private LuckPerms lp;

    private static final Block[] restrictedBlocks = {
            // Random stuff
            Blocks.LECTERN,
            Blocks.CHISELED_BOOKSHELF,
            Blocks.FARMLAND,
            Blocks.RESPAWN_ANCHOR,
            Blocks.DRAGON_EGG,
            Blocks.NETHER_WART,
            Blocks.END_PORTAL,
            Blocks.END_GATEWAY,
            Blocks.LEVER,
            Blocks.FLOWER_POT,
            Blocks.CAKE,
            Blocks.TNT,

            // Anvil
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL,

            // Furnace stuff
            Blocks.CAMPFIRE,
            Blocks.BLAST_FURNACE,
            Blocks.FURNACE,
            Blocks.SMOKER,

            // Storage
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.BARREL,
            Blocks.CAULDRON,
            Blocks.DISPENSER,
            Blocks.DROPPER,

            // Fence Gates
            Blocks.OAK_FENCE_GATE,
            Blocks.SPRUCE_FENCE_GATE,
            Blocks.BIRCH_FENCE_GATE,
            Blocks.JUNGLE_FENCE_GATE,
            Blocks.ACACIA_FENCE_GATE,
            Blocks.DARK_OAK_FENCE_GATE,
            Blocks.MANGROVE_FENCE_GATE,
            Blocks.CHERRY_FENCE_GATE,
            Blocks.BAMBOO_FENCE_GATE,
            Blocks.CRIMSON_FENCE_GATE,
            Blocks.WARPED_FENCE_GATE,

            // Trap Doors
            Blocks.OAK_TRAPDOOR,
            Blocks.SPRUCE_TRAPDOOR,
            Blocks.BIRCH_TRAPDOOR,
            Blocks.JUNGLE_TRAPDOOR,
            Blocks.ACACIA_TRAPDOOR,
            Blocks.DARK_OAK_TRAPDOOR,
            Blocks.MANGROVE_TRAPDOOR,
            Blocks.CHERRY_TRAPDOOR,
            Blocks.BAMBOO_TRAPDOOR,
            Blocks.CRIMSON_TRAPDOOR,
            Blocks.WARPED_TRAPDOOR,
            Blocks.IRON_TRAPDOOR,
            Blocks.COPPER_TRAPDOOR,
            Blocks.EXPOSED_COPPER_TRAPDOOR,
            Blocks.WEATHERED_COPPER_TRAPDOOR,
            Blocks.OXIDIZED_COPPER_TRAPDOOR,
            Blocks.WAXED_COPPER_TRAPDOOR,
            Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR,
            Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR,
            Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR,


            // Buttons
            Blocks.OAK_BUTTON,
            Blocks.SPRUCE_BUTTON,
            Blocks.BIRCH_BUTTON,
            Blocks.JUNGLE_BUTTON,
            Blocks.ACACIA_BUTTON,
            Blocks.DARK_OAK_BUTTON,
            Blocks.MANGROVE_BUTTON,
            Blocks.CHERRY_BUTTON,
            Blocks.BAMBOO_BUTTON,
            Blocks.CRIMSON_BUTTON,
            Blocks.WARPED_BUTTON,
            Blocks.STONE_BUTTON,
            Blocks.POLISHED_BLACKSTONE_BUTTON,

            // Bed
            Blocks.WHITE_BED,
            Blocks.LIGHT_GRAY_BED,
            Blocks.GRAY_BED,
            Blocks.BLACK_BED,
            Blocks.BROWN_BED,
            Blocks.RED_BED,
            Blocks.ORANGE_BED,
            Blocks.YELLOW_BED,
            Blocks.LIME_BED,
            Blocks.GREEN_BED,
            Blocks.CYAN_BED,
            Blocks.LIGHT_BLUE_BED,
            Blocks.BLUE_BED,
            Blocks.PURPLE_BED,
            Blocks.MAGENTA_BED,
            Blocks.PINK_BED,
    };


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
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer playerServer) {
                if (isRestrictedBlock(world.getBlockState(hitResult.getBlockPos()).getBlock())) {
                    if (hasPermission(playerServer, lp) && !playerServer.isCreative()) {
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
                if (isRestrictedBlock(world.getBlockState(pos).getBlock())) {
                    if (hasPermission(playerServer, lp) && !playerServer.isCreative()) {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });

        // Currently I don't know any entity interaction we want to allow ...
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayer playerServer) {
                if (hasPermission(playerServer, lp) && !playerServer.isCreative()) {
                    return InteractionResult.FAIL;
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

    private boolean isRestrictedBlock(Block block) {
        for (Block restrictedBlock : restrictedBlocks) {
            if (block == restrictedBlock) {
                return true;
            }
        }
        return false;
    }
}
