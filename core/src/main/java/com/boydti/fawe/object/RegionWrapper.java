package com.boydti.fawe.object;

import com.sk89q.worldedit.Vector;

public class RegionWrapper {
    public int minX;
    public int maxX;
    public int minZ;
    public int maxZ;

    public static RegionWrapper GLOBAL() {
        return new RegionWrapper(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public RegionWrapper(final int minX, final int maxX, final int minZ, final int maxZ) {
        this.maxX = maxX;
        this.minX = minX;
        this.maxZ = maxZ;
        this.minZ = minZ;
    }

    public RegionWrapper(final Vector pos1, final Vector pos2) {
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public RegionWrapper[] toArray() {
        return new RegionWrapper[]{this};
    }

    public boolean isIn(final int x, final int z) {
        return ((x >= this.minX) && (x <= this.maxX) && (z >= this.minZ) && (z <= this.maxZ));
    }

    public int distanceX(int x) {
        if (x >= minX) {
            if (x <= maxX) {
                return 0;
            }
            return maxX - x;
        }
        return minX - x;
    }

    public int distanceZ(int z) {
        if (z >= minZ) {
            if (z <= maxZ) {
                return 0;
            }
            return maxZ - z;
        }
        return minZ - z;
    }

    public boolean intersects(RegionWrapper other) {
        return other.minX <= this.maxX && other.maxX >= this.minX && other.minZ <= this.maxZ && other.maxZ >= this.minZ;
    }

    public int distance(int x, int z) {
        if (isIn(x, z)) {
            return 0;
        }
        int dx1 = Math.abs(x - minX);
        int dx2 = Math.abs(x - maxX);
        int dz1 = Math.abs(z - minZ);
        int dz2 = Math.abs(z - maxZ);
        if (x >= minX && x <= maxX) {
            return Math.min(dz1, dz2);
        } else if (z >= minZ && z <= maxZ) {
            return Math.min(dx1, dx2);
        } else {
            int dx = Math.min(dx1, dx2);
            int dz = Math.min(dz1, dz2);
            return (int) Math.sqrt(dx * dx + dz * dz);
        }
    }

    @Override
    public String toString() {
        return this.minX + "," + this.minZ + "->" + this.maxX + "," + this.maxZ;
    }

    public Vector getBottomVector() {
        return new Vector(this.minX, 1, this.minZ);
    }

    public Vector getTopVector() {
        return new Vector(this.maxX, 255, this.maxZ);
    }

    public boolean isGlobal() {
        return minX == Integer.MIN_VALUE && minZ == Integer.MIN_VALUE && maxX == Integer.MAX_VALUE && maxZ == Integer.MAX_VALUE;
    }

    public boolean contains(RegionWrapper current) {
        return current.minX >= minX && current.maxX <= maxX && current.minZ >= minZ && current.maxZ <= maxZ;
    }
}
