/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.Pattern;

public class GravityBrush implements Brush {

    private final boolean fullHeight;

    public GravityBrush(boolean fullHeight, BrushTool tool) {
        this.fullHeight = fullHeight;
    }

    @Override
    public void build(EditSession editSession, Vector position, Pattern pattern, double sizeDouble) throws MaxChangedBlocksException {
        Mask mask = editSession.getMask();
        if (mask == Masks.alwaysTrue() || mask == Masks.alwaysTrue2D()) {
            mask = null;
        }
        int size = (int) sizeDouble;
        int endY = position.getBlockY() + size;
        int startPerformY = Math.max(0, position.getBlockY() - size);
        int startCheckY = fullHeight ? 0 : startPerformY;
        Vector mutablePos = new Vector(0, 0, 0);
        for (int x = position.getBlockX() + size; x > position.getBlockX() - size; --x) {
            for (int z = position.getBlockZ() + size; z > position.getBlockZ() - size; --z) {
                int freeSpot = startCheckY;
                for (int y = startCheckY; y <= endY; y++) {
                    if (y < startPerformY) {
                        if (editSession.getLazyBlock(x, y, z) != EditSession.nullBlock) {
                            freeSpot = y + 1;
                        }
                        continue;
                    }
                    BaseBlock block = editSession.getLazyBlock(x, y, z);
                    mutablePos.mutX(x);
                    mutablePos.mutY(y);
                    mutablePos.mutZ(z);
                    if (block != EditSession.nullBlock && (mask == null || mask.test(mutablePos))) {
                        if (freeSpot != y) {
                            mutablePos.mutY(freeSpot);
                            editSession.setBlockFast(mutablePos, block);
                            mutablePos.mutY(y);
                            editSession.setBlockFast(mutablePos, EditSession.nullBlock);
                        }
                        freeSpot++;
                    }
                }
            }
        }
    }

    public static Class<?> inject() {
        return GravityBrush.class;
    }

}
