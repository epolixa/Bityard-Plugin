package com.epolixa.bityard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AFKCommandExecutor implements CommandExecutor
{
    private final AFK afk;

    public AFKCommandExecutor(AFK afk)
    {
        this.afk = afk;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("afk"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("\"/afk\" can only be used by a player");
                return false;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                if (args[i].length() > 0) {
                    if (sb.toString().length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(args[i]);
                }
            }

            afk.goAFK((Player)sender, sb.toString());

            return true;
        }
        return false;
    }
}
