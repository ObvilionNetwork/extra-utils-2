package com.rwtema.extrautils2.gui.backend;

import com.rwtema.extrautils2.render.IVertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class WidgetTileBackground extends WidgetBase {


	private final World world;
	private final BlockPos pos;

	public WidgetTileBackground(World world, BlockPos pos, int y, int size) {
		super((170 - size) / 2, y, size, size);
		this.world = world;
		this.pos = pos;
	}

	@Override
	public void addToContainer(DynamicContainer container) {
		super.addToContainer(container);
		x = (container.width - w) / 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderBackground(TextureManager manager, DynamicGui gui, int guiLeft, int guiTop) {

    }

	public void renderQuads(IVertexBuffer t, List<BakedQuad> quadList, float x, float y, float z) {
		for (BakedQuad bakedQuad : quadList) {
			renderQuad(t, bakedQuad, x, y, z);
		}
	}

	private void renderQuad(IVertexBuffer t, BakedQuad bakedQuad, float x, float y, float z) {
		int[] vertexData = bakedQuad.getVertexData();
		for (int i = 0; i < 4; i++) {
			t.pos(
					x + w * (Float.intBitsToFloat(vertexData[i * 7])),
					y + h * (1 - Float.intBitsToFloat(vertexData[i * 7 + 1])),
					z).endVertex();
		}
	}

	@Override
	public List<String> getToolTip() {
		return null;
	}
}
