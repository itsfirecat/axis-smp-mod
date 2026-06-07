package net.itsfirecat.arcbound.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.itsfirecat.arcbound.arcbound;
import net.itsfirecat.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup AXIS_SMP_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(arcbound.MOD_ID, "axis_smp_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.FREEZE_ARC))
                    .displayName(Text.translatable("itemgroup.arcbound.axis_smp_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.FREEZE_SHARD);
                        entries.add(ModItems.FREEZE_ARTIFACT);
                        entries.add(ModItems.FREEZE_ARC);
                        entries.add(ModItems.DASH_SHARD);
                        entries.add(ModItems.DASH_ARTIFACT);
                        entries.add(ModItems.DASH_ARC);
                        entries.add(ModItems.PULSE_ARTIFACT);
                        entries.add(ModItems.PULSE_ARC);
                        entries.add(ModItems.INFINITY_ARTIFACT);
                        entries.add(ModItems.INFINITY_ARC);
                        entries.add(ModItems.RESONANCE_SHARD);
                        entries.add(ModItems.RESONANCE_ARTIFACT);
                        entries.add(ModItems.RESONANCE_ARC);
                        entries.add(ModBlocks.RECALL_ANCHOR);
                    }).build());

    public static void registerItemGroups() {
        arcbound.LOGGER.info("Registering Item Groups for " + arcbound.MOD_ID);
    }
}
