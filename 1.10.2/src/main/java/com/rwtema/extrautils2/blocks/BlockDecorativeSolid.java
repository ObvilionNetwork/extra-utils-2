package com.rwtema.extrautils2.blocks;

import com.rwtema.extrautils2.backend.PropertyEnumSimple;
import com.rwtema.extrautils2.backend.XUBlockConnectedTextureBase;
import com.rwtema.extrautils2.backend.XUBlockStateCreator;
import com.rwtema.extrautils2.backend.entries.IItemStackMaker;
import com.rwtema.extrautils2.backend.entries.XU2Entries;
import com.rwtema.extrautils2.crafting.CraftingHelper;
import com.rwtema.extrautils2.textures.ConnectedTexture;
import com.rwtema.extrautils2.textures.ISolidWorldTexture;
import com.rwtema.extrautils2.textures.TextureRandom;
import com.rwtema.extrautils2.tile.TileResonator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.List;

public class BlockDecorativeSolid extends XUBlockConnectedTextureBase {
	public static final PropertyEnumSimple<DecorStates> decor = new PropertyEnumSimple<>(DecorStates.class);

	public BlockDecorativeSolid() {
		super(Material.ROCK);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerTextures() {
		for (DecorStates decorState : DecorStates.values()) {
			decorState.tex = decorState.createTexture(this);
		}
	}

	@Nonnull
	@Override
	protected XUBlockStateCreator createBlockState() {
		return new XUBlockStateCreator(this, false, decor);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}

	@Override
	public ISolidWorldTexture getConnectedTexture(IBlockState state, EnumFacing side) {
		return state.getValue(decor).tex;
	}

	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) return 0;
		return state.getValue(decor).enchantBonus;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		xuBlockState.getStateFromItemStack(stack).getValue(decor).addInformation(stack, playerIn, tooltip, advanced);
	}

	public enum DecorStates implements IItemStackMaker {
		borderstone {
			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless(name(), newStack(4), "stone", "bricksStone", "stone", "bricksStone");
			}
		},
		//		endstone {
//			@Override
//			public void addRecipes() {
//				CraftingHelper.addShaped(newStack(4), "SS", "SS", 'S', "endstone");
//			}
//		},
		stonecross {
			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless(name(), newStack(4), borderstone, borderstone, borderstone, borderstone);
			}
		},
		stoneslab {
			@Override
			public void addRecipes() {
				CraftingHelper.addShaped("stoneslab", newStack(4), "SS", "SS", 'S', Blocks.STONEBRICK);
			}
		},
		stoneburnt {
			@Override
			public void addRecipes() {
				if (XU2Entries.resonator.enabled) {
					TileResonator.register(stoneslab.newStack(1), newStack(1), 800);
				} else
					CraftingHelper.addShaped("stoneburnt", newStack(8), "SSS", "ScS", "SSS", 'S', stoneslab.newStack(1), 'c', Items.COAL);
			}
		},
		sandy_glass {
			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless(name(), newStack(4), "sand", "blockGlassColorless", "sand", "blockGlassColorless");
			}
		},
		truchet {
			@Override
			public void addRecipes() {
				CraftingHelper.addShapeless("truchet", newStack(4), stoneslab, borderstone, stoneslab, borderstone);
			}

			@Nonnull
			@Override
			@SideOnly(Side.CLIENT)
			public ISolidWorldTexture createTexture(XUBlockConnectedTextureBase block) {
				return new TextureRandom("truchet") {
					@Override
					protected void assignBaseTextures() {
						for (int i = 0; i < 6; i++) {
							baseTexture[i] = textures[0];
						}
					}
				};
			}
		},
		burnt_quartz {
			@Override
			public void addRecipes() {
				if (XU2Entries.resonator.enabled) {
					TileResonator.register(new ItemStack(Blocks.QUARTZ_BLOCK), newStack(1), 800);
				}
			}
		},
//		block_evil{
//			@Override
//			public void addRecipes() {
//
//			}
//		}

//		,
//		stonelayers{
//			@Override
//			public void addRecipes() {
//
//			}
//		}

//		,
//		endstone_brick {
//			@Override
//			public void addRecipes() {
//
//			}
//		}
		;

		@SideOnly(Side.CLIENT)
		public ISolidWorldTexture tex;
		public float enchantBonus = 0;

		public abstract void addRecipes();

		public ItemStack newStack(int amount) {
			return XU2Entries.decorativeSolid.newStack(amount, decor, this);
		}

		public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

		}


		@Nonnull
		@SideOnly(Side.CLIENT)
		public ISolidWorldTexture createTexture(XUBlockConnectedTextureBase block) {
			return new ConnectedTexture(toString(), block.xuBlockState.defaultState.withProperty(decor, this), block);
		}

		@Override
		public ItemStack newStack() {
			return newStack(1);
		}
	}
}
