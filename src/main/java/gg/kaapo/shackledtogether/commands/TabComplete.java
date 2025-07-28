package gg.kaapo.shackledtogether.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> argList = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("shackle") || command.getName().equalsIgnoreCase("shackledtogether")) {
            if (args.length == 1) {
                if (sender.hasPermission("shackledtogether.create")) {
                    argList.add("create");
                }
                if (sender.hasPermission("shackledtogether.leave")) {
                    argList.add("leave");
                }
                if (sender.hasPermission("shackledtogether.reload")) {
                    argList.add("reload");
                }
                argList.add("info");
            } else if (args.length >= 2 && args[0].equalsIgnoreCase("create") && sender.hasPermission("shackledtogether.create")) {
                List<String> alreadyTyped = List.of(args).subList(1, args.length);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!alreadyTyped.contains(player.getName())) {
                        argList.add(player.getName());
                    }
                }
            }
        }
        return argList;
    }

}
