package com.rwtema.extrautils2.commands;

import com.rwtema.extrautils2.ExtraUtils2;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;

// CL start - Custom command to toggle cursed earth
public class CommandToggleCursedEarth extends CommandBase {
    public static final String COMMAND_NAME = "cursed-earth-toggle";

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Nonnull
    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getUsage(ICommandSender iCommandSender) {
        return "/" + COMMAND_NAME;
    }

    @Override
    public void execute(MinecraftServer minecraftServer, ICommandSender iCommandSender, String[] strings) throws CommandException {
        if (ExtraUtils2.cursedEarthEnabled) {
            ExtraUtils2.cursedEarthEnabled = false;
            iCommandSender.sendMessage(new TextComponentString("Cursed Earth has been disabled!"));
        }
        else {
            ExtraUtils2.cursedEarthEnabled = true;
            iCommandSender.sendMessage(new TextComponentString("Cursed Earth has been enabled!"));
        }
    }
}
// CL end