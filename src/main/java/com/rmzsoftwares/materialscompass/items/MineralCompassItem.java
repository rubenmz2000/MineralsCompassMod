package com.rmzsoftwares.materialscompass.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public class MineralCompassItem extends CompassItem {

    // Tags vanilla típicos
    private static final ResourceLocation[] TARGETS = new ResourceLocation[] {
            new ResourceLocation("minecraft", "iron_ores"),
            new ResourceLocation("minecraft", "copper_ores"),
            new ResourceLocation("minecraft", "coal_ores"),
            new ResourceLocation("minecraft", "gold_ores"),
            new ResourceLocation("minecraft", "diamond_ores"),
            new ResourceLocation("minecraft", "redstone_ores"),
            new ResourceLocation("minecraft", "lapis_ores"),
            new ResourceLocation("minecraft", "emerald_ores")
    };

    private static final String NBT_TARGET = "TargetOreTag";
    private static final String NBT_TICK = "ScanCooldown";

    public MineralCompassItem(Item.Properties props) {
        super(props);
    }

    // Shift + click derecho para ciclar mineral
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isShiftKeyDown()) {
            int idx = getTargetIndex(stack);
            idx = (idx + 1) % TARGETS.length;
            setTargetIndex(stack, idx);
            player.displayClientMessage(Component.translatable("item.materialscompass.target",
                    friendlyName(TARGETS[idx].getPath())), true);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (!level.isClientSide) {
            TagKey<Block> targetTag = TagKey.create(Registries.BLOCK, TARGETS[getTargetIndex(stack)]);
            BlockPos here = player.blockPosition();
            BlockPos found = findNearestWithTag(level, here, targetTag, 96, 2);

            CompoundTag tag = stack.getOrCreateTag();
            if (found != null) {
                tag.put("LodestonePos", NbtUtils.writeBlockPos(found));
                tag.putString("LodestoneDimension", level.dimension().location().toString());
                tag.putBoolean("LodestoneTracked", false);
                player.displayClientMessage(
                        Component.literal("Mineral encontrado a " + here.distManhattan(found) + " bloques."),
                        true
                );
            } else {
                tag.remove("LodestonePos");
                tag.remove("LodestoneDimension");
                tag.remove("LodestoneTracked");
                player.displayClientMessage(Component.literal("Ningun mineral encontrado cerca."), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide || !(entity instanceof Player player)) return;
        // Solo si la llevas en mano
        boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        if (!inHand) return;

        CompoundTag tag = stack.getOrCreateTag();
        int cd = tag.getInt(NBT_TICK);
        if (cd > 0) { tag.putInt(NBT_TICK, cd - 1); return; }
        tag.putInt(NBT_TICK, 10); // busca cada ~10 ticks

        TagKey<Block> targetTag = TagKey.create(Registries.BLOCK, TARGETS[getTargetIndex(stack)]);
        BlockPos here = player.blockPosition();
        BlockPos found = findNearestWithTag(level, here, targetTag, 96, 2); // radio 96, salto 2 para rendimiento

        if (found != null) {
            // Escribe NBT de “lodestone” para que la brújula vanilla apunte ahí
            tag.put("LodestonePos", NbtUtils.writeBlockPos(found));
            tag.putString("LodestoneDimension", level.dimension().location().toString());
            tag.putBoolean("LodestoneTracked", false);
        } else {
            // Sin destino → aguja “baila”
            tag.remove("LodestonePos");
            tag.remove("LodestoneDimension");
            tag.remove("LodestoneTracked");
        }
    }

    @Nullable
    private BlockPos findNearestWithTag(Level level, BlockPos origin, TagKey<Block> tag, int radius, int step) {
        if (radius < 1) radius = 1;
        if (step < 1) step = 1;

        int minY = Math.max(level.getMinBuildHeight(), origin.getY() - radius);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + radius);

        int bestDist2 = Integer.MAX_VALUE;
        BlockPos best = null;

        int close = Math.min(8, radius);
        for (int y = Math.max(minY, origin.getY() - close); y <= Math.min(maxY, origin.getY() + close); y++) {
            for (int x = origin.getX() - close; x <= origin.getX() + close; x++) {
                for (int z = origin.getZ() - close; z <= origin.getZ() + close; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState bs = level.getBlockState(p);
                    if (bs.is(tag)) {
                        int dx = x - origin.getX(), dy = y - origin.getY(), dz = z - origin.getZ();
                        int d2 = dx * dx + dy * dy + dz * dz;

                        if (d2 < bestDist2) {
                            bestDist2 = d2;
                            best = p;
                            if (bestDist2 <= 1) return best;
                        }
                    }
                }
            }
        }

        for (int y = minY; y <= maxY; y += step) {
            for (int x = origin.getX() - radius; x <= origin.getX() + radius; x += step) {
                for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z += step) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState bs = level.getBlockState(p);
                    if (bs.is(tag)) {
                        int dx = x - origin.getX(), dy = y - origin.getY(), dz = z - origin.getZ();
                        int d2 = dx*dx + dy*dy + dz*dz;
                        if (d2 < bestDist2) {
                            bestDist2 = d2;
                            best = p;
                        }
                    }
                }
            }
        }

        return best;
    }

    private static int getTargetIndex(ItemStack stack) {
        CompoundTag t = stack.getOrCreateTag();
        int idx = t.getInt(NBT_TARGET);
        if (idx < 0 || idx >= TARGETS.length) idx = 0;
        return idx;
    }

    private static void setTargetIndex(ItemStack stack, int idx) {
        stack.getOrCreateTag().putInt(NBT_TARGET, idx);
    }

    private static String friendlyName(String path) {
        // path tipo "iron_ores" → "Iron"
        String base = path.replace("_ores","").replace('_',' ');
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int idx = getTargetIndex(stack);
        String name = friendlyName(TARGETS[idx].getPath());
        tooltip.add(Component.translatable("item.materialscompass.target", name, true));
        tooltip.add(Component.translatable("item.materialscompass.wipcretiveonly"));
    }
}