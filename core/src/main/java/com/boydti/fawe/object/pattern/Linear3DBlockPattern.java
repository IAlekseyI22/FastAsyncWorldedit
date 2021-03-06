package com.boydti.fawe.object.pattern;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import java.util.Arrays;
import java.util.Collection;

public class Linear3DBlockPattern extends AbstractPattern {

    private final Collection<Pattern> patterns;
    private final Pattern[] patternsArray;

    public Linear3DBlockPattern(Pattern[] patterns) {
        this.patternsArray = patterns;
        this.patterns = Arrays.asList(patterns);
    }

    @Override
    public BaseBlock apply(Vector position) {
        int index = (position.getBlockX() + position.getBlockY() + position.getBlockZ()) % patternsArray.length;
        if (index < 0) {
            index += patternsArray.length;
        }
        return patternsArray[index].apply(position);
    }
}
