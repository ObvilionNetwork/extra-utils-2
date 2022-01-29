package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraft.init.Biomes;
import net.minecraft.item.Item;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

public class QuarryItemsProvider {
    public static QuarryBiome[] all;
    public static Biome[] biomes = new Biome[] {
            Biomes.FOREST, Biomes.DESERT, Biomes.MESA, Biomes.HELL, /* The End */ Biome.REGISTRY.getObjectById(9)
    };

    public static QuarryBiome getQuarryBiome(Biome biome) {
        int i = ArrayUtils.indexOf(biomes, biome);
        return i == -1 ? all[0] : all[i];
    }

    public static void loadConfig() {
        ArrayList<QuarryBiome> b = new ArrayList<>();
        for (Biome biome : biomes) {
            String[] values = ExtraUtils2.config.get("Quarry items", biome.getBiomeName(),
                    new String[]{ "minecraft:cobblestone 0 1000 1 minecraft:stone 0" }).getStringList();

            int len = values.length;

            ArrayList<Item> items = new ArrayList<>();
            ArrayList<Short> items_meta = new ArrayList<>();
            ArrayList<Short> chances = new ArrayList<>();
            ArrayList<Short> luck_chances = new ArrayList<>();
            ArrayList<Item> silk_items = new ArrayList<>();
            ArrayList<Short> silk_items_meta = new ArrayList<>();

            for (String s : values) {
                String[] v = s.split(" ");

                items.add(Item.getByNameOrId(v[0]));
                items_meta.add(Short.parseShort(v[1]));

                chances.add(Short.parseShort(v[2]));
                luck_chances.add(Short.parseShort(v[3]));

                if (v.length > 4) {
                    silk_items.add(Item.getByNameOrId(v[4]));
                    silk_items_meta.add(Short.parseShort(v[5]));
                } else {
                    silk_items.add(null);
                    silk_items_meta.add(null);
                }
            }

            b.add(new QuarryBiome(
                items.toArray(new Item[len]), items_meta.toArray(new Short[len]),
                silk_items.toArray(new Item[len]), silk_items_meta.toArray(new Short[len]),
                chances.toArray(new Short[len]), luck_chances.toArray(new Short[len])
            ));
        }

        all = b.toArray(new QuarryBiome[0]);
    }
}
