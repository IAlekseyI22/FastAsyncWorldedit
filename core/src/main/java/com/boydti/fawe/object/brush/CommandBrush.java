package com.boydti.fawe.object.brush;

import com.boydti.fawe.object.FawePlayer;
import com.boydti.fawe.wrappers.LocationMaskedPlayerWrapper;
import com.boydti.fawe.wrappers.PlayerWrapper;
import com.boydti.fawe.wrappers.SilentPlayerWrapper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVectorFace;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.util.Location;

public class CommandBrush implements Brush {

    private final String command;
    private final int radius;

    public CommandBrush(String command, double radius) {
        this.command = command;
        this.radius = (int) radius;
    }

    @Override
    public void build(EditSession editSession, Vector position, Pattern pattern, double size) throws MaxChangedBlocksException {
        CuboidRegionSelector selector = new CuboidRegionSelector(editSession.getWorld(), position.subtract(radius, radius, radius), position.add(radius, radius, radius));
        String replaced = command.replace("{x}", position.getBlockX() + "")
                .replace("{y}", position.getBlockY() + "")
                .replace("{z}", position.getBlockZ() + "")
                .replace("{world}", editSession.getQueue().getWorldName())
                .replace("{size}", radius + "");

        FawePlayer fp = editSession.getPlayer();
        Player player = fp.getPlayer();
        WorldVectorFace face = player.getBlockTraceFace(256, true);
        if (face == null) {
            position = position.add(0, 1, 1);
        } else {
            position = face.getFaceVector();
        }
        fp.setSelection(selector);
        PlayerWrapper wePlayer = new SilentPlayerWrapper(new LocationMaskedPlayerWrapper(player, new Location(player.getExtent(), position)));
        String[] cmds = replaced.split(";");
        for (String cmd : cmds) {
            CommandEvent event = new CommandEvent(wePlayer, cmd);
            CommandManager.getInstance().handleCommandOnCurrentThread(event);
        }
    }
}