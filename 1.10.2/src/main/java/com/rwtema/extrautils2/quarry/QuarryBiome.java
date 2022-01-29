package com.rwtema.extrautils2.quarry;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class QuarryBiome {
    public Item[] items, silk_items;
    public Short[] items_meta, silk_items_meta, luck_chances;

    public int[] chances_range;
    public int max_chance;

    public QuarryBiome(Item[] items, Short[] items_meta, Item[] silk_items, Short[] silk_items_meta, Short[] chances,
                       Short[] luck_chances) {
        this.luck_chances = luck_chances;

        this.silk_items_meta = silk_items_meta;
        this.silk_items = silk_items;

        this.items_meta = items_meta;
        this.items = items;

        int old = 0;
        chances_range = new int[chances.length];
        for (int i = 0; i < chances.length; i++) {
            old = old + chances[i];
            chances_range[i] = old;
        }

        max_chance = chances_range[chances_range.length - 1];
    }

    public ItemStack getRandom(int luck_level) {
        int num = ExtraUtils2.RANDOM.nextInt(max_chance);

        int result = 0;
        for (int i = 0; i < chances_range.length; i++) {
            int v = chances_range[i];
            if (num <= v) {
                result = i;
                break;
            }
        }

        int count = luck_level == 1 || luck_chances[result] == 1
                ? 1
                : ExtraUtils2.RANDOM.nextInt(luck_chances[result] * luck_level);

        return new ItemStack(items[result], count, items_meta[result]);
    }
}
