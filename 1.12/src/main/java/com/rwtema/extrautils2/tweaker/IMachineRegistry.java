package com.rwtema.extrautils2.tweaker;

import com.rwtema.extrautils2.RunnableClient;
import com.rwtema.extrautils2.api.machine.*;
import com.rwtema.extrautils2.backend.model.Textures;
import crafttweaker.annotations.ZenRegister;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@ZenRegister
@ZenClass(XUTweaker.PACKAGE_NAME_BASE + "IMachineRegistry")
public class IMachineRegistry {
	@ZenMethod
	public static IMachine createNewMachine(@Nonnull String name,
											int energyBufferSize,
											int energyTransferLimit,
											List<IMachineSlot> inputSlots,
											List<IMachineSlot> outputSlots,
											@Nonnull String frontTexture,
											@Nonnull String frontTextureActive,
											int color) {
		return createNewMachine(name, energyBufferSize, energyTransferLimit, inputSlots, outputSlots, frontTexture, frontTextureActive, color, null, null, null, null, Machine.EnergyMode.USES_ENERGY);
	}

	public static IMachine createNewMachine(@Nonnull String name,
											int energyBufferSize,
											int energyTransferLimit,
											List<IMachineSlot> inputSlots,
											List<IMachineSlot> outputSlots,
											@Nonnull String frontTexture,
											@Nonnull String frontTextureActive,
											int color,
											@Nullable String textureTop,
											@Nullable String textureBase,
											@Nullable String textureBottom,
											@Nullable String textureTopOverlay, Machine.EnergyMode energyMode) {
		if (name.indexOf(':') == -1) {
			name = "crafttweaker:" + name;
		}

		List<MachineSlotItem> itemInputs = inputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotItem).map(s -> (MachineSlotItem) s).collect(Collectors.toList());
		@Nonnull List<MachineSlotFluid> fluidInputs = inputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotFluid).map(s -> (MachineSlotFluid) s).collect(Collectors.toList());
		;
		@Nonnull List<MachineSlotItem> itemOutputs = outputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotItem).map(s -> (MachineSlotItem) s).collect(Collectors.toList());
		@Nonnull List<MachineSlotFluid> fluidOutputs = outputSlots.stream().map(ObjWrapper::getInternal).filter(s -> s instanceof MachineSlotFluid).map(s -> (MachineSlotFluid) s).collect(Collectors.toList());
		;
		Machine machine = new Machine(name, energyBufferSize, energyTransferLimit, itemInputs, fluidInputs, itemOutputs, fluidOutputs, frontTexture, frontTextureActive, energyMode, color, textureTop, textureBase, textureBottom, textureTopOverlay);
		GenericAction.run(() -> MachineRegistry.register(machine), "Creating new machine: " + machine.name);
		new RunnableClient() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				Textures.register(frontTexture, frontTextureActive, textureBase, textureBottom, textureTopOverlay, textureTop);
			}
		}.run();

		return new IMachine(machine);
	}

	@ZenMethod
	public static IMachine getMachine(String name) {
		if (name.indexOf(':') == -1) {
			name = "crafttweaker:" + name;
		}
		Machine machine = MachineRegistry.getMachine(name);
		if (machine == null) return null;
		return new IMachine(machine);
	}

	@ZenMethod
	public static List<IMachine> getRegisteredMachineNames() {
		return MachineRegistry.getMachineValues().stream().map(IMachine::new).collect(Collectors.toList());
	}

	@ZenGetter("crusher")
	public static IMachine getCrusher() {
		return new IMachine(XUMachineCrusher.INSTANCE);
	}

	@ZenGetter("enchanter")
	public static IMachine getEnchanter() {
		return new IMachine(XUMachineEnchanter.INSTANCE);
	}
}
