package com.rwtema.extrautils2.machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.compatibility.XUShapedRecipe;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.recipes.GenericMachineRecipe;
import com.rwtema.extrautils2.recipes.SingleInputStackToStackRecipeCached;
import com.rwtema.extrautils2.utils.Lang;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

public class MachineInit {

	static HashSet<String> registeredDusts = Sets.newHashSet(
			"dustIron",
			"dustGold",
			"dustCopper",
			"dustTin",
			"dustLead",
			"dustSilver",
			"dustNickel",
			"dustPlatinum"
	);
	private static MachineSlotItem SLOT_SLIME_SECONDARY;


	@SubscribeEvent
	public static void onOreRegister(OreDictionary.OreRegisterEvent event) {
		String name = event.getName();
		if (name.startsWith("ingot") || name.startsWith("ore") || name.startsWith("dust")) {
			Set<String> oreNames = Sets.newHashSet(OreDictionary.getOreNames());
			for (String dust : oreNames) {
				if (!dust.startsWith("dust") || registeredDusts.contains(dust))
					continue;

				String ore = Pattern.compile("dust", Pattern.LITERAL).matcher(dust).replaceFirst("ore");
				String ingot = Pattern.compile("dust", Pattern.LITERAL).matcher(dust).replaceFirst("ingot");

				if (oreNames.contains(ore) && oreNames.contains(ingot)) {
					addCrusherOreRecipe(ore, dust, 2);
					addCrusherOreRecipe(ingot, dust, 1);
					registeredDusts.add(dust);
				}
			}
		}
	}


	public static void init() {
		XUMachineEnchanter.INSTANCE = new Machine("extrautils2:enchanter", 100000, 1000, ImmutableList.of(XUMachineEnchanter.INPUT, XUMachineEnchanter.INPUT_LAPIS), ImmutableList.of(), ImmutableList.of(XUMachineEnchanter.OUTPUT), ImmutableList.of(), "extrautils2:machine/enchanter_off", "extrautils2:machine/enchanter_on", Machine.EnergyMode.USES_ENERGY, 0xffffff, null, null, null,
//				"extrautils2:machine/enchanter_top"
				null
		) {
			@Override
			public float getSpeed(World world, BlockPos pos, TileMachine tileMachine) {
				float power = 0;

				for (int dx = -1; dx <= 1; dx++) {
					for (int dz = -1; dz <= 1; dz++) {
						if (dx == 0 && dz == 0) continue;
						if (isPassable(world, pos.add(dz, 0, dx)) && isPassable(world, pos.add(dz, 1, dx))) {
							power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 0, dx * 2));
							power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 1, dx * 2));
							if (dz != 0 && dx != 0) {
								power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 0, dx));
								power += ForgeHooks.getEnchantPower(world, pos.add(dz * 2, 1, dx));
								power += ForgeHooks.getEnchantPower(world, pos.add(dz, 0, dx * 2));
								power += ForgeHooks.getEnchantPower(world, pos.add(dz, 1, dx * 2));
							}
							if (power >= 15)
								return 1;
						}
					}
				}
				if (power < 15)
					return 0;

				return 1;
			}

			public boolean isPassable(World world, BlockPos pos) {
				return !world.isBlockFullCube(pos);
			}

			@Nullable
			@Override
			public String getRunError(World world, BlockPos pos, TileMachine tileMachine, float speed) {
				if (speed != 0) return null;
				return Lang.translate("Enchanter requires a full set of nearby bookshelves, or other enchantment boosting blocks");
			}

			@Override
			public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand, TileMachine tileMachine) {
				for (int i = 0; i < 3; i++) {
					Blocks.ENCHANTING_TABLE.randomDisplayTick(
							Blocks.ENCHANTING_TABLE.getDefaultState(),
							worldIn,
							pos,
							rand);
				}
			}
		};

		XUMachineEnchanter.INSTANCE.recipes_registry.addRecipe(new MechEnchantmentRecipe(5 * 60 * 20, OreDictionary.getOres("gemLapis"), MechEnchantmentRecipe.EnchantType.LOWEST));
		XUMachineEnchanter.INSTANCE.recipes_registry.addRecipe(new MechEnchantmentRecipe(30 * 60 * 20, OreDictionary.getOres("netherStar"), MechEnchantmentRecipe.EnchantType.HIGHEST));

		MinecraftForge.EVENT_BUS.register(MachineInit.class);
		RecipeBuilder.Builder.builder = GenericMachineRecipe.Builder::new;

		SLOT_SLIME_SECONDARY = new MachineSlotItem("input2");

		MachineRegistry.register(XUMachineFurnace.INSTANCE);
		MachineRegistry.register(XUMachineCrusher.INSTANCE);
		MachineRegistry.register(XUMachineEnchanter.INSTANCE);
	}

	private static void spawnParticlesNearby(World world, AxisAlignedBB radius, float r, float g, float b) {
		world.spawnParticle(EnumParticleTypes.SPELL_MOB,
				radius.minX + (radius.maxX - radius.minX) * world.rand.nextFloat(),
				radius.minY + (radius.maxY - radius.minY) * world.rand.nextFloat(),
				radius.minZ + (radius.maxZ - radius.minZ) * world.rand.nextFloat(),
				r, g, b
		);
	}

	public static void addMachineRecipes() {

		XUMachineFurnace.INSTANCE.recipes_registry.addRecipe(
				new SingleInputStackToStackRecipeCached(XUMachineFurnace.INPUT, XUMachineFurnace.OUTPUT) {
					@Override
					@Nonnull
					public Collection<ItemStack> getInputValues() {
						return FurnaceRecipes.instance().getSmeltingList().keySet();
					}

					@Override
					public ItemStack getResult(@Nonnull ItemStack stack) {
						return FurnaceRecipes.instance().getSmeltingResult(stack);
					}

					@Nullable
					@Override
					public ItemStack getContainer(ItemStack stack) {
						return StackHelper.empty();
					}

					@Override
					public int getEnergyOutput(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
						return 2000;
					}

					@Override
					public int getProcessingTime(Map<MachineSlotItem, ItemStack> inputItems, Map<MachineSlotFluid, FluidStack> inputFluids) {
						return 100;
					}
				}
		);

		XUMachineCrusher.addRecipe(new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.BLAZE_POWDER, 2), new ItemStack(Items.BLAZE_POWDER, 3), 0.4F);
		XUMachineCrusher.addRecipe(new ItemStack(Items.BONE), new ItemStack(Items.DYE, 3, EnumDyeColor.WHITE.getDyeDamage()), new ItemStack(Items.DYE, 3, EnumDyeColor.WHITE.getDyeDamage()), 0.5F);

		for (int i = 0; i < 16; ++i) {
			ItemStack outputSecondary;
			if ((15 - i) != 4) {
				outputSecondary = new ItemStack(Items.DYE, 1, 15 - i);
			} else {
				outputSecondary = ItemIngredients.Type.DYE_POWDER_BLUE.newStack();
			}

			XUMachineCrusher.addRecipe(new ItemStack(Blocks.WOOL, 1, i), new ItemStack(Items.STRING, 3), outputSecondary, 0.05F);
			XUMachineCrusher.addRecipe(new ItemStack(Blocks.CARPET, 1, i), new ItemStack(Items.STRING, 2), outputSecondary, 0.05F);
		}

		XUMachineCrusher.addRecipe(new ItemStack(Blocks.YELLOW_FLOWER, 1, BlockFlower.EnumFlowerType.DANDELION.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.YELLOW.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.POPPY.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.RED.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.BLUE_ORCHID.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.LIGHT_BLUE.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ALLIUM.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.MAGENTA.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.HOUSTONIA.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.SILVER.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.RED_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.RED.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.ORANGE_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.ORANGE.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.WHITE_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.SILVER.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.PINK_TULIP.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.PINK.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.RED_FLOWER, 1, BlockFlower.EnumFlowerType.OXEYE_DAISY.getMeta()), new ItemStack(Items.DYE, 2, EnumDyeColor.SILVER.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.SUNFLOWER.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.YELLOW.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.SYRINGA.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.MAGENTA.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.ROSE.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.RED.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Blocks.DOUBLE_PLANT, 1, BlockDoublePlant.EnumPlantType.PAEONIA.getMeta()), new ItemStack(Items.DYE, 4, EnumDyeColor.PINK.getDyeDamage()));
		XUMachineCrusher.addRecipe(new ItemStack(Items.BEETROOT, 1), new ItemStack(Items.DYE, 2, EnumDyeColor.RED.getDyeDamage()));


		List<Pair<String, String>> pairedOres = ImmutableList.of(
				Pair.of("Iron", "Gold"),
				Pair.of("Gold", "Iron"),
				Pair.of("Copper", "Tin"),
				Pair.of("Tin", "Copper"),
				Pair.of("Lead", "Silver"),
				Pair.of("Silver", "Lead"),
				Pair.of("Nickel", "Platinum"),
				Pair.of("Platinum", "Nickel")
		);
		for (Pair<String, String> pair : pairedOres) {
			String s = pair.getLeft();
			addCrusherOreRecipe("ore" + s, "dust" + s, 2, "dust" + pair.getRight(), 1, 0.1F);
			addCrusherOreRecipe("ingot" + s, "dust" + s, 1);
		}
		addCrusherOreRecipe("oreDiamond", "gemDiamond", 1, "gemDiamond", 3, 0.2F);
		addCrusherOreRecipe("oreEmerald", "gemEmerald", 1, "gemEmerald", 3, 0.2F);
		addCrusherOreRecipe("cobblestone", "gravel", 1, "sand", 1, 0.1F);
		addCrusherOreRecipe("gravel", "sand", 1);
		addCrusherOreRecipe("oreLapis", "gemLapis", 8);
		addCrusherOreRecipe("oreRedstone", "dustRedstone", 8);
		addCrusherOreRecipe("oreQuartz", "gemQuartz", 1, "gemQuartz", 3, 0.2F);
		addCrusherOreRecipe("glowstone", "dustGlowstone", 4);
		addCrusherOreRecipe("oreCoal", Items.COAL, 4);
	}

	static void addCrusherOreRecipe(@Nonnull Object oreInput, @Nonnull Object dustOutput, int amount) {
		addCrusherOreRecipe(oreInput, dustOutput, amount, null, 0, 0);
	}

	static void addCrusherOreRecipe(@Nonnull Object oreInput, @Nonnull Object dustOutput, int amount, @Nullable Object outputSecondary, int outputSecondaryAmount, float outputSecondaryProbability) {
		RecipeBuilder recipeBuilder = RecipeBuilder.newbuilder(XUMachineCrusher.INSTANCE);
		recipeBuilder.setItemInput(XUMachineCrusher.INPUT, XUShapedRecipe.getRecipeStackList(oreInput), 1);
		recipeBuilder.setItemOutput(XUMachineCrusher.OUTPUT, XUShapedRecipe.getRecipeStackList(dustOutput), amount);
		if (outputSecondary != null && outputSecondaryAmount > 0) {
			recipeBuilder.setItemOutput(XUMachineCrusher.OUTPUT_SECONDARY, XUShapedRecipe.getRecipeStackList(outputSecondary), outputSecondaryAmount);
			recipeBuilder.setProbability(XUMachineCrusher.OUTPUT_SECONDARY, outputSecondaryProbability);
		}
		recipeBuilder.setEnergy(4000);
		recipeBuilder.setProcessingTime(200);
		XUMachineCrusher.INSTANCE.recipes_registry.addRecipe(recipeBuilder.build());
	}


	public static void register() {
		MachineRegistry.register(XUMachineFurnace.INSTANCE);
		MachineRegistry.register(XUMachineCrusher.INSTANCE);
		MachineRegistry.register(XUMachineEnchanter.INSTANCE);
	}
}
