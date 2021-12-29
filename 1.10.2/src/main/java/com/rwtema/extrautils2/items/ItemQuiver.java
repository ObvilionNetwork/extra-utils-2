package com.rwtema.extrautils2.items;

import com.rwtema.extrautils2.backend.IXUItemTexture;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemQuiver extends ItemArrow implements IXUItemTexture {
	public ItemQuiver() {
		setMaxStackSize(1);
	}

	@Override
	public boolean isInfinite(ItemStack stack, ItemStack bow, EntityPlayer player) {
		return true;
	}

	@Override
	public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
		return super.createArrow(worldIn, stack, shooter);
	}

	@Override
	public String getTexture(int i) {
		return "quiver";
	}


	@Override
	public int getMaxMetadata() {
		return 0;
	}

	@Override
	public boolean renderAsTool() {
		return false;
	}
}
