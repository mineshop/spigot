package br.com.mineshop.spigot.mineshop;

import br.com.mineshop.spigot.msdk.Exceptions.WSException;
import br.com.mineshop.spigot.msdk.MSDK;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands implements Listener, CommandExecutor {
  private JavaPlugin plugin;
  private MSDK msdk;

  String cmd1 = "mineshop";

  Commands(JavaPlugin plugin, MSDK msdk) {
    this.plugin = plugin;
    this.msdk = msdk;
  }

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
    if (commandSender instanceof Player) {
      commandSender.sendMessage("Este comando só pode ser executado no console do servidor");
    } else {
      if (command.getName().equalsIgnoreCase(this.cmd1)) {
        if (strings.length != 0) {
          String token = strings[0].trim().toLowerCase();
          try {
            this.msdk.setAuthorization(token);
            this.plugin.getConfig().set("authorization", token);
            this.plugin.saveConfig();
          } catch (WSException ex) {
            commandSender.sendMessage(ex.getMessage());
          }
        } else {
          commandSender.sendMessage(String.format("Use: %s <token-do-servidor>", this.cmd1));
        }
      }
    }

    return true;
  }
}
