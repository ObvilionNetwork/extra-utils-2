package com.rwtema.extrautils2.compatibility;

import com.google.common.base.Optional;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.XUBlock;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.PotionHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class CompatHelper112 {
	public static List<String> getTooltip(ItemStack itemStack, EntityPlayer player, boolean advancedItemTooltips) {
		return itemStack.getTooltip(player, advancedItemTooltips);
	}

	public static Collection<? extends String> getTooltip(ItemStack itemStack, ItemTooltipEvent event) {
		return itemStack.getTooltip(event.getEntityPlayer(), event.isShowAdvancedItemTooltips());
	}

	public static <T extends XUBlock> boolean isChunkProviderEnd(Object provider) {
		return provider instanceof ChunkProviderEnd;
	}

	public static <T extends IForgeRegistryEntry<T>> void register(T value) {
		GameRegistry.register(value);
	}

	@SuppressWarnings("Guava")
	public static <T> Optional<T> optionalOf(T identity) {
		return Optional.of(identity);
	}

	public static VillagerRegistry.VillagerProfession getVillagerProfession(String name1, String texture1) {
		return new VillagerRegistry.VillagerProfession(ExtraUtils2.MODID + ":" + name1, ExtraUtils2.MODID + ":textures/villagers/" + texture1 + ".png");
	}

	public static void damage(ItemStack itemStackIn, int amount, Random rand) {
		itemStackIn.attemptDamageItem(amount, rand);
	}

	public static List<String> getTooltip(ItemTooltipEvent event) {
		return event.getItemStack().getTooltip(event.getEntityPlayer(), event.isShowAdvancedItemTooltips());
	}

	public static void addRecipe(IRecipe recipe) {
		GameRegistry.addRecipe(recipe);
	}

	public static void drainExperience(EntityPlayer player, int amount, ItemStack result) {
		player.func_71013_b(amount);
	}


	public static void loadVersionSpecificEntries() {

	}

	public static EntityLiving getMobForSpawning(WorldServer world, Biome.SpawnListEntry entry) {
		EntityLiving mob;


		if (entry == null || entry.entityClass == null) {
			mob = null;
		} else {


			try {
				mob = entry.entityClass.getConstructor(World.class).newInstance(world);
			} catch (Exception exception) {
				exception.printStackTrace();
				mob = null;
			}
		}
		return mob;
	}

	public static <T> T getPotionInput(PotionHelper.MixPredicate<T> predicate) {
		Object input = predicate.input;
		return (T) input;
	}

	public static <T > T getPotionOutput(PotionHelper.MixPredicate<T> predicate) {
		Object output = predicate.output;
		return (T) output;
	}

	public static <T> Pair<T, T> createLink(PotionHelper.MixPredicate<T> t) {
		return Pair.of(getPotionInput(t), getPotionOutput(t));
	}
}
