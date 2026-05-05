package ru.minecraft.pickupz.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;
import ru.minecraft.pickupz.Managers.CarryManager;
import ru.minecraft.pickupz.PickupZ;

import java.util.UUID;

public class PlayerCarryListener implements Listener {

    private final PickupZ plugin;
    private final CarryManager carryManager;

    public PlayerCarryListener(PickupZ plugin, CarryManager carryManager) {
        this.plugin = plugin;
        this.carryManager = carryManager;
    }

    @EventHandler
    public void onRightClickPlayer(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;

        Player carrier = event.getPlayer();
        Player passenger = (Player) event.getRightClicked();

        event.setCancelled(true);

        if (carrier.getGameMode() == GameMode.SPECTATOR) return;

        if (carryManager.isCarrying(carrier)) {
            return;
        }

        if (carryManager.isBeingCarried(passenger)) {
            return;
        }

        if (carryManager.isBeingCarried(carrier)) {
            return;
        }

        carryManager.startCarry(carrier, passenger);
    }

    @EventHandler
    public void onLeftClickAirOrBlock(PlayerInteractEvent event) {
        Player carrier = event.getPlayer();

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (carryManager.isCarrying(carrier)) {
                event.setCancelled(true);

                carryManager.throwPassengerWithDelay(carrier, 8); // Выбрасываем на 8 блоков через 0.5 секунды
            }
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (event.isSneaking()) {
            if (carryManager.isBeingCarried(player)) {
                carryManager.passengerDrop(player);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (carryManager.isCarrying(player)) {
            carryManager.stopCarry(player);
        }
        if (carryManager.isBeingCarried(player)) {
            carryManager.passengerDrop(player);
        }
    }
}