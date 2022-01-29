package com.rwtema.extrautils2.grids;

import gnu.trove.set.hash.TLinkedHashSet;

import java.util.Set;

@SuppressWarnings("SuspiciousMethodCalls")
public class Grid {
	public final Set<TileGridRef<XUTileGrid>> refList = new TLinkedHashSet<>();

	public final GridType gridType;
	boolean isValid = true;

	public Grid(GridType gridType) {
		this.gridType = gridType;
	}

	public void add(XUTileGrid t) {
		TileGridRef<XUTileGrid> myRef = t.myRef;
		myRef.grids.add(this);
		refList.add(new TileGridRef<>(t));
	}

	public void remove(XUTileGrid t) {
		if (refList.remove(t.myRef)) {
			t.myRef.grids.remove(this);
			destroy();
		}
	}

	public void destroy() {
		isValid = false;
		for (TileGridRef<XUTileGrid> xuTileGridTileGridRef : refList) {
			XUTileGrid tileGrid = xuTileGridTileGridRef.get();
			if (tileGrid != null) {
				xuTileGridTileGridRef.grids.remove(this);
				GridHandler.pendingTiles.add(tileGrid);
			}
		}
	}

	public void check() {
		for (TileGridRef<XUTileGrid> xuTileGridTileGridRef : refList) {
			XUTileGrid tileGrid = xuTileGridTileGridRef.get();
			if (tileGrid == null || !tileGrid.isLoaded()) {
				destroy();
				break;
			}
		}
	}

	public void onMerge() {

	}

	public static class TileSet extends TLinkedHashSet<TileGridRef<XUTileGrid>> {

	}
}
