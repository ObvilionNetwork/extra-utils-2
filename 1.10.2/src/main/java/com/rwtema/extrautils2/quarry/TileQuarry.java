package com.rwtema.extrautils2.quarry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.InventoryHelper;
import com.rwtema.extrautils2.itemhandler.SingleStackHandler;
import com.rwtema.extrautils2.itemhandler.SingleStackHandlerFilter;
import com.rwtema.extrautils2.itemhandler.StackDump;
import com.rwtema.extrautils2.items.ItemBiomeMarker;
import com.rwtema.extrautils2.items.ItemIngredients;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.energy.XUEnergyStorage;
import com.rwtema.extrautils2.tile.RedstoneState;
import com.rwtema.extrautils2.tile.TilePower;
import com.rwtema.extrautils2.utils.CapGetter;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.datastructures.ListRandomOffset;
import com.rwtema.extrautils2.utils.datastructures.NBTSerializable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.*;

public class TileQuarry extends TilePower implements ITickable, IDynamicHandler {

	static final ArrayList<BlockPos> offset = new ArrayList<>();
	static final HashMap<BlockPos, ArrayList<EnumFacing>> offset_sides = new HashMap<>();
	private final static ItemStack genericDigger = new ItemStack(Items.DIAMOND_PICKAXE, 1);
	public static int ENERGY_PER_OPERATION = 16000;

	static {
		BlockPos origin = BlockPos.ORIGIN;
		for (EnumFacing facing1 : EnumFacing.values()) {
			BlockPos offset1 = origin.offset(facing1);
			for (EnumFacing facing2 : EnumFacing.values()) {
				if (facing1 != facing2.getOpposite()) {
					BlockPos offset2 = offset1.offset(facing2);
					if (!offset.contains(offset2))
						offset.add(offset2);
					offset_sides.computeIfAbsent(offset2, t -> new ArrayList<>()).add(facing2.getOpposite());
				}
			}
		}
	}

	private final StackDump extraStacks = registerNBT("extrastacks", new StackDump());
	public boolean redstoneDirty = true;
	public boolean redstoneActive;
	protected SingleStackHandlerFilter.ItemFilter filter = registerNBT("filter", new SingleStackHandlerFilter.ItemFilter() {
		@Override
		protected void onContentsChanged() {
			markDirty();
		}
	});
	XUEnergyStorage energy = registerNBT("energy", new XUEnergyStorage(ENERGY_PER_OPERATION * 100));
	NBTSerializable.Long blocksMined = registerNBT("mined", new NBTSerializable.Long());
	boolean needsToCheckNearbyBlocks = true;
	boolean hasNearbyBlocks = false;

	private final ItemBiomeMarker.ItemBiomeHandler biomeHandler = registerNBT("biome_marker", new ItemBiomeMarker.ItemBiomeHandler() {
		@Override
		protected void onContentsChanged() {
			Biome newBiome = getBiome();
			if (newBiome != null && newBiome != biome) {
				biome = newBiome;
				biomeData = QuarryItemsProvider.getQuarryBiome(biome);
			}
			markDirty();
		}
	});

	public Biome biome = null;
	public QuarryBiome biomeData = null;

	private final NBTSerializable.NBTEnum<RedstoneState> redstone_state = registerNBT("redstone_state", new NBTSerializable.NBTEnum<>(RedstoneState.OPERATE_ALWAYS));
	private final SingleStackHandler enchants = registerNBT("enchants", new SingleStackHandler() {
		@Override
		protected int getStackLimit(@Nonnull ItemStack stack) {
			if (stack.getItem() != Items.ENCHANTED_BOOK) {
				return 0;
			}

			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);
			if (map.isEmpty())
				return 0;
			for (Enchantment enchantment : map.keySet()) {
				if (enchantment.canApply(genericDigger))
					return 1;
			}

			return 0;
		}

		@Override
		protected void onContentsChanged() {
			markDirty();
		}
	});

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return Iterables.concat(
			InventoryHelper.getItemHandlerIterator(filter),
			InventoryHelper.getItemHandlerIterator(enchants),
			InventoryHelper.getItemHandlerIterator(biomeHandler)
		);
	}

	@Override
	public float getPower() {
		return Float.NaN;
	}

	@Override
	public void onPowerChanged() {

	}

	public boolean hasNearbyBlocks() {
		if (needsToCheckNearbyBlocks || world.isRemote) {
			needsToCheckNearbyBlocks = false;
			hasNearbyBlocks = true;
			for (EnumFacing facing : EnumFacing.values()) {
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));

				if (!(tileEntity instanceof TileQuarryProxy) || ((TileQuarryProxy) tileEntity).facing.value != facing.getOpposite()) {
					hasNearbyBlocks = false;
					break;
				}
			}
		}
		return hasNearbyBlocks;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return hasNearbyBlocks() && super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
	}

	@Override
	public void update() {
		if (world.isRemote) return;
		if (!hasNearbyBlocks()) return;

		if (redstoneDirty) {
			redstoneDirty = false;
			redstoneActive = false;

			for (EnumFacing facing : EnumFacing.values()) {
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));

				if ((tileEntity instanceof TileQuarryProxy) && ((TileQuarryProxy) tileEntity).facing.value == facing.getOpposite() && ((TileQuarryProxy) tileEntity).isPowered()) {
					redstoneActive = true;
					break;
				}
			}
		}

		if (!redstone_state.value.acceptableValue(redstoneActive)) return;

		float multiplier = 1;
		int numOps = 1;
		int fortune = 1;
		ItemStack enchantsStack = enchants.getStack();
		if (StackHelper.isNonNull(enchantsStack)) {
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(enchantsStack);
			Integer efficiency = enchantments.get(Enchantments.EFFICIENCY);
			Integer luck = enchantments.get(Enchantments.FORTUNE);

			for (int ui : enchantments.values()) {
				multiplier += ui * 0.5f;
			}

			if (luck != null) {
				fortune = luck;
			}

			if (efficiency != null) {
				multiplier -= efficiency * 0.5f;
				numOps += efficiency;
				multiplier = multiplier * numOps;
			}
		}

		final int need_energy = (int) (ENERGY_PER_OPERATION * multiplier);

		if (energy.extractEnergy(need_energy, true) != need_energy)
			return;

		if (biomeData == null) {
			biomeData = QuarryItemsProvider.getQuarryBiome(biome == null ? Biomes.FOREST : biome);
		}

		for (int rep_i = 0; rep_i < numOps; rep_i++) {
			if (extraStacks.stacks.isEmpty()) {
				ItemStack s = biomeData.getRandom(fortune);
				blocksMined.value += 1;

				if (!filter.matches(s)) continue;
				extraStacks.addStack(s);
			}

			if (!extraStacks.stacks.isEmpty()) {
				for (BlockPos offset_pos : new ListRandomOffset<>(offset)) {
					TileEntity tile = world.getTileEntity(pos.add(offset_pos));
					if (tile != null) {
						for (EnumFacing facing : new ListRandomOffset<>(offset_sides.get(offset_pos))) {
							IItemHandler handler = CapGetter.ItemHandler.getInterface(tile, facing);
							if (handler != null) {
								extraStacks.attemptDump(handler);
							}
						}
					}
				}
			}
		}

		energy.extractEnergy(need_energy, false);
	}

	@Override
	public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(worldIn, pos, state, neighborBlock);
		needsToCheckNearbyBlocks = true;
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerQuarry(player);
	}

	public class ContainerQuarry extends DynamicContainerTile {

		public ContainerQuarry(EntityPlayer player) {
			super(TileQuarry.this);
			addTitle("Quantum Quarry");
			addWidget(new SingleStackHandlerFilter.WidgetSlotFilter(filter, 4, 20) {
				@Override
				@SideOnly(Side.CLIENT)
				public List<String> getToolTip() {
					if (!getHasStack()) {
						return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(Lang.translate("If present, the quarry will auto-destroy any items that do NOT match the filter."), 120);
					}
					return null;
				}
			});
			addWidget(new WidgetSlotItemHandler(enchants, 0, 4, 20 + 18) {
				@Override
				@SideOnly(Side.CLIENT)
				public List<String> getToolTip() {
					if (!getHasStack()) {
						return ImmutableList.of(Lang.translate("Enchanted Book"));
					}
					return null;
				}

				@Override
				@SideOnly(Side.CLIENT)
				public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {
					super.renderBackground(manager, gui, guiLeft, guiTop);
					if (!getHasStack()) {
						ItemStack stack = ItemIngredients.Type.ENCHANTED_BOOK_SKELETON.newStack();
						gui.renderStack(stack, guiLeft + getX() + 1, guiTop + getY() + 1, "");
					}
				}
			});

			addWidget(biomeHandler.getSlot(4, 20 + 18 * 2));


			addWidget(new WidgetEnergyStorage(DynamicContainer.playerInvWidth - 18 - 4, 20, energy));

			addWidget(RedstoneState.getRSWidget(4, 20 + 37 + 18, redstone_state));

			crop();

			addWidget(new WidgetTextData(4 + 18 + 4, 20, (DynamicContainer.playerInvWidth - 18 * 2 - 16), 54, 1, 4210752) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					packet.writeLong(TileQuarry.this.blocksMined.value);
					packet.writeInt(biome != null ? Biome.REGISTRY.getIDForObject(biome) : -1);
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					long mined = packet.readLong();
					int biomeID = packet.readInt();
					Biome biome = biomeID != -1 ? Biome.REGISTRY.getObjectById(biomeID) : null;

					return Lang.translateArgs("Blocks Mined: %s", mined)
							+ ((biome != null) ? ("\n" + Lang.translateArgs("Biome: %s", biome.getBiomeName() + " (" + biomeID + ")")) : "");
				}
			});


			cropAndAddPlayerSlots(player.inventory);
			validate();
		}
	}
}
