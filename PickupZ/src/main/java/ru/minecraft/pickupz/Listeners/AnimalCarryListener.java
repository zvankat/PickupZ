package ru.minecraft.pickupz.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import ru.minecraft.pickupz.Managers.AnimalCarryManager;
import ru.minecraft.pickupz.PickupZ;

public class AnimalCarryListener implements Listener {

    private final PickupZ plugin;
    private final AnimalCarryManager animalCarryManager;

    public AnimalCarryListener(PickupZ plugin, AnimalCarryManager animalCarryManager) {
        this.plugin = plugin;
        this.animalCarryManager = animalCarryManager;
    }

    @EventHandler
    public void onRightClickAnimal(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Animals)) return;

        Player carrier = event.getPlayer();
        Animals animal = (Animals) event.getRightClicked();

        event.setCancelled(true);

        if (animalCarryManager.isCarrying(carrier)) {
            return;
        }

        animalCarryManager.startCarry(carrier, animal);
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        Player carrier = event.getPlayer();

        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
                && animalCarryManager.isCarrying(carrier)) {
            event.setCancelled(true);

            animalCarryManager.throwPassenger(carrier, 8);
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (event.isSneaking() && animalCarryManager.isCarrying(player)) {
            animalCarryManager.dropPassenger(player);
        }
    }
}