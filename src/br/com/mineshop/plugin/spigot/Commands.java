package br.com.mineshop.plugin.spigot;

import br.com.mineshop.msdk.MSDK;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands implements Listener, CommandExecutor {
  private MSDK msdk;
  private JavaPlugin plugin;

  String cmd1 = "mineshop";

  Commands(MSDK msdk, JavaPlugin plugin) {
    this.msdk = msdk;
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
    if (sender instanceof Player) {
      sender.sendMessage(ChatColor.RED + "Este comando não pode ser executado fora do console do servidor");
    } else if (command.getName().equalsIgnoreCase(this.cmd1)) {
      if (args.length > 0) {
        String token = args[0].trim().toLowerCase();

        this.msdk.setCredentials(token);
        this.plugin.getConfig().set("token", token);
        this.plugin.saveConfig();

        sender.sendMessage(String.format(
          "%sPronto! Se o token informado estiver correto, este servidor irá sincronizar com sua loja em " +
          "alguns instantes.",
          ChatColor.GREEN
        ));
      } else {
        sender.sendMessage(String.format("%s <token>", this.cmd1));
      }
    }

    return true;
  }
}
