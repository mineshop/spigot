package br.com.mineshop.spigot.mineshop;

import br.com.mineshop.spigot.msdk.Endpoints.Queue;
import br.com.mineshop.spigot.msdk.Exceptions.WSException;
import br.com.mineshop.spigot.msdk.MSDK;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Main extends JavaPlugin {
  private JavaPlugin plugin;
  private MSDK msdk = (MSDK) Bukkit.getServer().getPluginManager().getPlugin("msdk");
  private Commands commands = new Commands(this, this.msdk);

  @Override
  public void onEnable() {
    this.saveDefaultConfig();

    this.plugin = this;
    this.getCommand(this.commands.cmd1).setExecutor(this.commands);

    try {
      this.msdk.setAuthorization(this.getConfig().getString("authorization"));
    } catch (WSException ex) {
      Bukkit.getLogger().warning(String.format("[%s] Falha ao tentar definir token de autorização", this.getDescription().getName()));
    }

    long firstExecution = (this.getConfig().getLong("scheduler.firstExecution") * 20);
    long repeat = (this.getConfig().getLong("scheduler.repeat") * 20);

    BukkitScheduler scheduler = this.getServer().getScheduler();
    scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            Queue[] queue = null;

            try {
              queue = msdk.getQueue();
            } catch (WSException ex) {
              Bukkit.getLogger().warning(String.format("[%s] %s", getDescription().getName(), ex.getMessage()));
            }

            if (queue != null) {
              int requestsCount = 0;

              for (Queue currentQueue : queue) {
                if (currentQueue.getType().equalsIgnoreCase("online")) {
                  Player target = getServer().getPlayerExact(currentQueue.getNickname());
                  if (target == null) {
                    continue;
                  }
                }

                try {
                  msdk.delivered(currentQueue);
                  for (int i = 0; i < currentQueue.getCommands().length; i++) {
                    int currentKey = i;
                    Bukkit.getScheduler().runTask(plugin, new Runnable() {
                      @Override
                      public void run() {
                        getServer().dispatchCommand(getServer().getConsoleSender(), currentQueue.getCommands()[currentKey]);
                      }
                    });
                  }
                } catch (WSException ex) {
                  Bukkit.getLogger().warning(String.format("[%s] %s", getDescription().getName(), ex.getMessage()));
                }

                requestsCount++;

                if (requestsCount >= 5) {
                  break;
                }
              }
            }
          }
        };

        Thread thread = new Thread(runnable);
        thread.start();
      }
    }, firstExecution, repeat);
  }
}
