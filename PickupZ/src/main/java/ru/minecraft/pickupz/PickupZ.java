package ru.minecraft.pickupz;

import org.bukkit.plugin.java.JavaPlugin;
import ru.minecraft.pickupz.Listeners.PlayerCarryListener;
import ru.minecraft.pickupz.Listeners.AnimalCarryListener;
import ru.minecraft.pickupz.Managers.AnimalCarryManager;
import ru.minecraft.pickupz.Managers.CarryManager;

public final class PickupZ extends JavaPlugin {

    private CarryManager carryManager;
    private AnimalCarryManager animalCarryManager;

    @Override
    public void onEnable() {
        getLogger().info("[PickupZ] Plugin has been enabled!");

        carryManager = new CarryManager(this);
        getServer().getPluginManager().registerEvents(
                new PlayerCarryListener(this, carryManager), this);

        animalCarryManager = new AnimalCarryManager(this);
        getServer().getPluginManager().registerEvents(
                new AnimalCarryListener(this, animalCarryManager), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("[PickupZ] Plugin has been disabled!");

        if (carryManager != null) {
            carryManager.clearAll();
        }

        if (animalCarryManager != null) {
            animalCarryManager.clearAll();
        }
    }

    public CarryManager getCarryManager() {
        return carryManager;
    }

    public AnimalCarryManager getAnimalCarryManager() {
        return animalCarryManager;
    }
}