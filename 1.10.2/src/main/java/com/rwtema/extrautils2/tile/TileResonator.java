package com.rwtema.extrautils2.tile;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.api.resonator.IResonatorRecipe;
import com.rwtema.extrautils2.compatibility.StackHelper;
import com.rwtema.extrautils2.crafting.ResonatorRecipe;
import com.rwtema.extrautils2.gui.backend.*;
import com.rwtema.extrautils2.itemhandler.*;
import com.rwtema.extrautils2.network.XUPacketBuffer;
import com.rwtema.extrautils2.power.IWorldPowerMultiplier;
import com.rwtema.extrautils2.power.PowerManager;
import com.rwtema.extrautils2.transfernodes.Upgrade;
import com.rwtema.extrautils2.utils.Lang;
import com.rwtema.extrautils2.utils.helpers.NBTHelper;
import com.rwtema.extrautils2.utils.helpers.StringHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class TileResonator extends TilePower implements ITickable, IWorldPowerMultiplier, IDynamicHandler {

	public static final ArrayList<IResonatorRecipe> resonatorRecipes = new ArrayList<>();
	public final float MULTIPLIER = 1 / 100.0F;
	public SingleStackHandlerUpgrades upgrades = registerNBT("upgrades", new SingleStackHandlerUpgrades(EnumSet.of(Upgrade.SPEED)) {
		@Override
		protected void onContentsChanged() {
			TileResonator.this.markDirty();
			PowerManager.instance.markDirty(TileResonator.this);
		}
	});
	IResonatorRecipe currentRecipe = null;
	int progress = 0;
	ItemStack displayStack;
	private SingleStackHandler OUTPUT = new SingleStackHandler() {
		protected void onContentsChanged() {
			TileResonator.this.onInputChanged();
		}
	};
	private ItemStackHandler INPUT = new XUTileItemStackHandler(1, this) {
		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			TileResonator.this.onInputChanged();
		}

		@Override
		protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
			if (!StackHelper.isNull(stack)) {
				for (IResonatorRecipe resonatorRecipe : resonatorRecipes) {
					if (resonatorRecipe.matches(stack) && StackHelper.getStacksize(stack) >= resonatorRecipe.getNumberOfInputsToConsume(stack)) {
						return super.getStackLimit(slot, stack);
					}
				}
			}
			return 0;
		}
	};
	private IItemHandler handler = ConcatItemHandler.concatNonNull(new PublicWrapper.Insert(INPUT), new PublicWrapper.Extract(OUTPUT));

	public static void register(ItemStack input, ItemStack output, int energy) {
		register(input, output, energy, false);
	}

	public static void register(ItemStack input, ItemStack output, int energy, boolean addOwnerTag) {
		ResonatorRecipe recipe = new ResonatorRecipe(input, output, energy, addOwnerTag);
		register(recipe);
	}

	public static boolean register(IResonatorRecipe recipe) {
		resonatorRecipes.add(recipe);
		return true;
	}

	@Override
	protected Iterable<ItemStack> getDropHandler() {
		return Iterables.concat(
				InventoryHelper.getItemHandlerIterator(handler),
				InventoryHelper.getItemHandlerIterator(upgrades)
		);
	}

	@Override
	public IItemHandler getItemHandler(EnumFacing facing) {
		return handler;
	}

	private void onInputChanged() {
		currentRecipe = getPotentialOutput();
		markDirty();

		if (currentRecipe == null) {
			progress = 0;
			return;
		}

		if (!OUTPUT.isEmpty() && StackHelper.isNonNull(OUTPUT.insertItem(0, getOutputStack(), true))) {
			currentRecipe = null;
			progress = 0;
		}
	}

	public IResonatorRecipe getPotentialOutput() {
		ItemStack input = INPUT.getStackInSlot(0);
		if (StackHelper.isNonNull(input))
			for (IResonatorRecipe resonatorRecipe : resonatorRecipes) {
				if (resonatorRecipe.matches(input)) {
					return resonatorRecipe;
				}
			}
		return null;
	}

	@Override
	public void update() {
		if (world.isRemote || !active || currentRecipe == null) return;
		ItemStack inputStack = INPUT.getStackInSlot(0);
		if (!currentRecipe.shouldProgress(this, frequency(), inputStack)) {
			return;
		}
		progress += 4 * (1 + upgrades.getLevel(Upgrade.SPEED));
		markDirty();

		if (progress >= currentRecipe.getEnergy(inputStack)) {
			ItemStack stack = getOutputStack();

			INPUT.extractItem(0, currentRecipe.getNumberOfInputsToConsume(inputStack), false);
			OUTPUT.insertItem(0, stack, false);
			progress = 0;
		}
	}

	private ItemStack getOutputStack() {
		ItemStack stack = currentRecipe.getOutput(INPUT.getStackInSlot(0)).copy();
		if (currentRecipe.shouldAddOwnerTag()) {
			NBTHelper.getOrInitTagCompound(stack).setInteger("Freq", frequency);
		}
		return stack;
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setTag("Input", INPUT.serializeNBT());
		compound.setTag("Output", OUTPUT.serializeNBT());
		compound.setInteger("Progress", progress);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		INPUT.deserializeNBT(compound.getCompoundTag("Input"));
		OUTPUT.deserializeNBT(compound.getCompoundTag("Output"));
		progress = compound.getInteger("Progress");
		onInputChanged();
	}

	@Override
	public void onPowerChanged() {

	}

	@Override
	public float getPower() {
		return 1;
	}

	@Override
	public IWorldPowerMultiplier getMultiplier() {
		return this;
	}

	@Override
	public float multiplier(World world) {
		int level = upgrades.getLevel(Upgrade.SPEED);
		if (level == 0)
			return progress * MULTIPLIER;
		else
			return progress * MULTIPLIER * (1 + level) + (Upgrade.SPEED.getPowerUse(level));
	}

	@Override
	public DynamicContainer getDynamicContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerResonator(player.inventory);
	}

	@Override
	public void addToDescriptionPacket(XUPacketBuffer packet) {
		super.addToDescriptionPacket(packet);
		IResonatorRecipe recipe = getPotentialOutput();
		ItemStack outStack = recipe != null ? recipe.getOutput(INPUT.getStackInSlot(0)) : null;
		packet.writeItemStack(outStack);

	}

	@Override
	public void handleDescriptionPacket(XUPacketBuffer packet) {
		super.handleDescriptionPacket(packet);
		displayStack = packet.readItemStack();
	}

	public static IResonatorRecipe removeRecipe(ItemStack stack) {
		for (Iterator<IResonatorRecipe> iterator = resonatorRecipes.iterator(); iterator.hasNext(); ) {
			IResonatorRecipe resonatorRecipe = iterator.next();
			if (resonatorRecipe.matches(stack)) {
				iterator.remove();
				return resonatorRecipe;
			}
		}
		return null;
	}

	public class ContainerResonator extends DynamicContainerTile {
		public ContainerResonator(InventoryPlayer inventory) {
			super(TileResonator.this, 30, 64);
			addTitle(Lang.getItemName(getXUBlock()), false);

			WidgetProgressArrowTimer arrowTimer = new WidgetProgressArrowTimer(centerX - 11, 52) {
				@Override
				protected float getTime() {
					return progress / (float) (1 + TileResonator.this.upgrades.getLevel(Upgrade.SPEED));
				}

				@Override
				protected float getMaxTime() {
					if (!active) return -1;
					IResonatorRecipe recipe = TileResonator.this.currentRecipe;
					if (recipe == null)
						return 0;
					return recipe.getEnergy(TileResonator.this.INPUT.getStackInSlot(0)) / (float) (1 + TileResonator.this.upgrades.getLevel(Upgrade.SPEED));
				}

				@Override
				public List<String> getToolTip() {
					if (currentRecipe != null && !StringHelper.isBlank(currentRecipe.getRequirementText())) {
						return ImmutableList.<String>builder().add(currentRecipe.getRequirementText()).addAll(super.getToolTip()).build();
					}

					return super.getToolTip();
				}

				@Override
				public List<String> getErrorMessage() {
					return ImmutableList.of(Lang.translate("Grid is overloaded"));
				}
			};
			addWidget(arrowTimer);
			addWidget(arrowTimer.getJEIWidget(ExtraUtils2.MODID + ".resonator"));

			addWidget(new WidgetSlotItemHandler(INPUT, 0, centerX - 11 - 6 - 18, 52));
			addWidget(new WidgetSlotItemHandler(OUTPUT, 0, centerX + 11 + 6, 52) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					return false;
				}
			});

			addWidget(upgrades.getSpeedUpgradeSlot(playerInvWidth + 4 - 18, 52));

			addWidget(new WidgetTextData(8, 20, 160) {
				@Override
				public void addToDescription(XUPacketBuffer packet) {
					IResonatorRecipe recipe = TileResonator.this.currentRecipe;
					if (recipe == null) {
						packet.writeBoolean(false);
					} else {
						packet.writeBoolean(true);
						packet.writeInt(progress);
						packet.writeInt(recipe.getEnergy(INPUT.getStackInSlot(0)));
					}
				}

				@Override
				protected String constructText(XUPacketBuffer packet) {
					this.align = 0;
					if (packet.readBoolean()) {
						int progress = packet.readInt();
						int level = upgrades.getLevel(Upgrade.SPEED);
						TileResonator.this.progress = progress;
						return Lang.translateArgs("Power: %s / %s GP", StringHelper.format(progress * MULTIPLIER * (1 + level)), StringHelper.format(packet.readInt() * MULTIPLIER * (1 + level)));
					} else
						return null;
				}
			});

			cropAndAddPlayerSlots(inventory);
			validate();
		}
	}


}
