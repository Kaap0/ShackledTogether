package gg.kaapo.shackledtogether.commands;

import gg.kaapo.shackledtogether.ShackledTogether;
import gg.kaapo.shackledtogether.chain.Chain;
import gg.kaapo.shackledtogether.chain.ChainConfiguration;
import gg.kaapo.shackledtogether.chain.ChainCreationResult;
import gg.kaapo.shackledtogether.events.ChainLeaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static gg.kaapo.shackledtogether.ShackledTogether.prefix;

public class ShackleCommand implements CommandExecutor {

    private final ShackledTogether shackledTogether = ShackledTogether.getInstance();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("specify-subcommand"));
            return true;
        }

        String subcommand = args[0].toLowerCase();
        return switch (subcommand) {
            case "create" -> handleCreateCommand(commandSender, args);
            case "leave" -> handleLeaveCommand(commandSender);
            case "reload" -> handleReloadCommand(commandSender);
            case "info" -> handleInfoCommand(commandSender);
            default -> {
                commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("unknown-subcommand"));
                yield true;
            }
        };
    }

    //====================================
    // CHAIN
    //====================================
    private boolean handleCreateCommand(CommandSender commandSender, String[] args) {
        if (!commandSender.hasPermission("shackledtogether.create")) {
            commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("not-enough-players"));
            return true;
        }

        ArrayList<Player> players = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            Player target = Bukkit.getPlayer(args[i]);
            if (target != null) {
                players.add(target);
            }
        }

        ChainCreationResult chainCreationResult = shackledTogether.getAPI().createChain(players, null);
        switch (chainCreationResult.getResult()) {
            case SUCCESS ->
                    commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("chain-created"));
            case PARTIAL ->
                    commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("chain-created-partially"));
            case NOT_ENOUGH_PLAYERS ->
                    commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("not-enough-players"));
            case TOO_MANY_PLAYERS ->
                    commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("too-many-players"));
        }
        return true;
    }

    //====================================
    // LEAVE
    //====================================
    private boolean handleLeaveCommand(CommandSender commandSender) {
        if (!commandSender.hasPermission("shackledtogether.leave")) {
            commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("no-permission"));
            return true;
        }

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("player-only-command"));
            return true;
        }

        if (!shackledTogether.getAPI().isChained(player)) {
            player.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("not-part-of-chain"));
            return true;
        }

        ChainLeaveEvent chainLeaveEvent = new ChainLeaveEvent(player, shackledTogether.getAPI().getChain(player), ChainLeaveEvent.LeaveReason.COMMAND);
        ShackledTogether.getInstance().getServer().getPluginManager().callEvent(chainLeaveEvent);
        shackledTogether.getAPI().getChain(player).remove(player);
        player.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("left-chain"));
        return true;
    }

    //====================================
    // RELOAD
    //====================================
    private boolean handleReloadCommand(CommandSender commandSender) {
        if (!commandSender.hasPermission("shackledtogether.reload")) {
            commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("no-permission"));
            return true;
        }

        ShackledTogether.getLanguageManager().loadLanguage();
        shackledTogether.reloadConfig();
        shackledTogether.saveDefaultConfig();

        FileConfiguration config = shackledTogether.getConfig();

        for (Chain chain : shackledTogether.getAPI().getChains()) {
            if (!chain.hasCustomConfiguration()) {
                chain.getChainConfiguration().setChainCollision(config.getBoolean("chain-collision"));
                chain.getChainConfiguration().setChainLength(config.getDouble("chain-length"));
                chain.getChainConfiguration().setChainElasticity(config.getDouble("chain-elasticity"));
                chain.getChainConfiguration().setChainStretch(config.getDouble("chain-stretch"));
                chain.getChainConfiguration().setPullMechanic(config.getBoolean("pull-mechanic"));
                chain.getChainConfiguration().setPullEfficiency(config.getDouble("pull-efficiency"));
                chain.getChainConfiguration().setPullRange(config.getDouble("pull-range"));
                chain.getChainConfiguration().setPullMethod(ChainConfiguration.PullMethod.fromString(config.getString("pull-method")));
            }
        }

        commandSender.sendMessage(prefix + ShackledTogether.getLanguageManager().getMessage("successful-reload"));
        return true;
    }

    //====================================
    // INFO
    //====================================
    private boolean handleInfoCommand(CommandSender commandSender) {
        commandSender.sendMessage(prefix);
        commandSender.sendMessage("§8Version: §7" + shackledTogether.getDescription().getVersion());
        commandSender.sendMessage("§8Authors: §7" + shackledTogether.getDescription().getAuthors());
        commandSender.sendMessage("§8Github:§7 https://github.com/Kaap0/ShackledTogether");
        commandSender.sendMessage("§8Donate:§7 https://github.com/sponsors/Kaap0");
        commandSender.sendMessage("§8Licence: §7GNU AGPL");
        commandSender.sendMessage(prefix);
        return true;
    }
}
