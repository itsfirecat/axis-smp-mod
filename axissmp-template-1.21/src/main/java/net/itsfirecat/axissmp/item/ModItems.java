package net.itsfirecat.axissmp.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.itsfirecat.axissmp.AxisSMP;
import net.itsfirecat.axissmp.item.custom.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item FREEZE_SHARD = registerItem("freeze_shard", new Item(new Item.Settings()));
    public static final Item FREEZE_ARTIFACT = registerItem("freeze_artifact", new Item(new Item.Settings()));
    public static final Item FREEZE_ARC = registerItem("freeze_arc", new FreezeArcItem(new Item.Settings()));
    public static final Item INFINITY_ARTIFACT = registerItem("infinity_artifact", new Item(new Item.Settings()));
    public static final Item INFINITY_ARC = registerItem("infinity_arc", new InfinityArcItem(new Item.Settings()));
    public static final Item PULSE_ARTIFACT = registerItem("pulse_artifact", new Item(new Item.Settings()));
    public static final Item PULSE_ARC = registerItem("pulse_arc", new PulseArcItem(new Item.Settings()));
    public static final Item DASH_SHARD = registerItem("dash_shard", new Item(new Item.Settings()));
    public static final Item DASH_ARTIFACT = registerItem("dash_artifact", new Item(new Item.Settings()));
    public static final Item DASH_ARC = registerItem("dash_arc", new DashArcItem(new Item.Settings()));
    public static final Item RESONANCE_SHARD = registerItem("resonance_shard", new Item(new Item.Settings()));
    public static final Item RESONANCE_ARTIFACT = registerItem("resonance_artifact", new Item(new Item.Settings()));
    public static final Item RESONANCE_ARC = registerItem("resonance_arc", new ResonanceArcItem(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(AxisSMP.MOD_ID, name), item);
    }

    public static void registerModItems() {
        AxisSMP.LOGGER.info("Registering Mod Items for " + AxisSMP.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(FREEZE_SHARD);
            entries.add(FREEZE_ARTIFACT);
            entries.add(INFINITY_ARTIFACT);
            entries.add(PULSE_ARTIFACT);
            entries.add(DASH_SHARD);
            entries.add(DASH_ARTIFACT);
            entries.add(RESONANCE_SHARD);
            entries.add(RESONANCE_ARTIFACT);
        });
    }
}
