/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.tex.jpg;

import nexusvault.format.tex.jpg.tools.Constants;
import nexusvault.format.tex.jpg.tools.Constants.CompressionType;
import nexusvault.format.tex.jpg.tools.Constants.JPGType;
import nexusvault.format.tex.jpg.tools.huffman.BitConsumer;
import nexusvault.format.tex.jpg.tools.huffman.BitSupply;

final class Context {
	public static final class Component {
		public final int index;
		public final CompressionType compressionType;
		public final int[] pixels;
		public int dc = 0;

		private Component(int index, CompressionType compressionType, int[] pixels) {
			this.index = index;
			this.compressionType = compressionType;
			this.pixels = pixels;
		}
	}

	// public final ExecutorService executor = Executors.newWorkStealingPool();// .newCachedThreadPool();
	// public CountDownLatch latch;

	public final int[] uncompressed = new int[Constants.BLOCK_SIZE];
	public final int[] block = new int[Constants.BLOCK_SIZE];

	public final JPGType jpgType;

	public int[] defaultValues;
	public float[][] quantTables;

	public final int componentsWidth;
	public final int componentsHeight;
	public final Component[] components = new Component[4];

	public BitSupply jpgInput;
	public BitConsumer jpgOutput;
	public final int numberOfBlocks;
	public final int blocksPerRow;

	public final int imageWidth;
	public final int imageHeight;
	public final byte[] image;

	public Context(JPGType jpgType, int width, int height) {
		this.jpgType = jpgType;

		this.imageWidth = width;
		this.imageHeight = height;
		this.image = new byte[this.imageWidth * this.imageHeight * 4];

		this.componentsWidth = this.imageWidth + 7 & 0xFFFFF8; // multiple of 8
		this.componentsHeight = this.imageHeight + 7 & 0xFFFFF8; // multiple of 8
		this.numberOfBlocks = this.componentsWidth * this.componentsHeight / Constants.BLOCK_SIZE;
		this.blocksPerRow = this.componentsWidth / Constants.BLOCK_WIDTH;
	}

	public void setComponentTypes(CompressionType component0, CompressionType component1, CompressionType component2, CompressionType component3) {
		this.components[0] = new Component(0, component0, new int[this.componentsWidth * this.componentsHeight]);
		this.components[1] = new Component(1, component1, new int[this.componentsWidth * this.componentsHeight]);
		this.components[2] = new Component(2, component2, new int[this.componentsWidth * this.componentsHeight]);
		this.components[3] = new Component(3, component3, new int[this.componentsWidth * this.componentsHeight]);
	}

}
