package br.com.mineshop.plugin.spigot;

import br.com.mineshop.msdk.exceptions.MsdkException;
import br.com.mineshop.msdk.webservice.endpoints.v1.QueueItem;
import br.com.mineshop.msdk.exceptions.WebServiceException;
import br.com.mineshop.msdk.MSDK;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin {
  private MSDK msdk = new MSDK();
  private JavaPlugin plugin;
  private Commands commands = new Commands(this.msdk, this);

  @Override
  public void onEnable() {
    this.saveDefaultConfig();

    this.plugin = this;
    this.msdk.setCredentials(this.getConfig().getString("token"));
    this.getCommand(this.commands.cmd1).setExecutor(this.commands);

    int timerAfterRestart = this.getConfig().getInt("eventLoop.timer.afterRestart");
    int timerDelay = this.getConfig().getInt("eventLoop.timer.delay");

    if (timerAfterRestart < 20) {
      Bukkit.getLogger().warning(String.format(
        "[%s] O event loop está configurado para ser executado em %s segundo(s) logo após a " +
        "reinicialização do servidor ou do plugin! Recomendamos um delay entre 20 e 300 segundos neste campo.",
        this.getDescription().getName(),
        Integer.toString(timerAfterRestart)
      ));
    }

    if (timerDelay < 10) {
      Bukkit.getLogger().warning(String.format(
        "[%s] O event loop está configurado para ser executado a cada %s segundo(s)! Recomendamos um " +
        "delay entre 10 e 60 segundos neste campo.",
        this.getDescription().getName(),
        Integer.toString(timerDelay)
      ));
    }

    new BukkitRunnable() {
      @Override
      public void run() {
        QueueItem[] queueItems = null;

        try {
          queueItems = msdk.getQueueItems();
        } catch (WebServiceException | MsdkException e) {
          Bukkit.getLogger().warning(String.format("[%s] %s", getDescription().getName(), e.getMessage()));
        }

        if (queueItems == null) {
          return;
        }

        for (QueueItem queueItem : queueItems) {
          new BukkitRunnable() {
            @Override
            public void run() {
              if (queueItem.getType().equalsIgnoreCase("online")) {
                Player player = getServer().getPlayerExact(queueItem.getNickname());

                if (player == null) {
                  return;
                }

                int emptySlots = 0;

                for (ItemStack item : player.getInventory().getContents()) {
                  if (item == null) {
                    emptySlots++;
                  }
                }

                if (queueItem.getSlotsNeeded() > emptySlots) {
                  player.sendMessage(String.format(
                    "%sNão pudemos entregar todos os itens que você comprou em nossa loja porque seu " +
                    "inventário não tem espaço suficiente. O restante dos itens serão entregues em %s segundo(s). " +
                    "Para recebê-los, por favor, esvazie seu inventário.",
                    ChatColor.LIGHT_PURPLE,
                    Integer.toString(timerDelay)
                  ));

                  return;
                }
              }

              try {
                msdk.hasBeenDelivered(queueItem.getNickname(), queueItem.getUuid());
                getServer().dispatchCommand(getServer().getConsoleSender(), queueItem.getCommand());
              } catch (WebServiceException | MsdkException e) {
                Bukkit.getLogger().warning(String.format("[%s] %s", getDescription().getName(), e.getMessage()));
              }
            }
          }.runTask(plugin);
        }
      }
    }.runTaskTimerAsynchronously(this, timerAfterRestart * 20, timerDelay * 20);
  }
}
