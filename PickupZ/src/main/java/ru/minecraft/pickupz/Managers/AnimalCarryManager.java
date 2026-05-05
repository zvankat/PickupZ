package ru.minecraft.pickupz.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.minecraft.pickupz.PickupZ;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AnimalCarryManager {

    private final PickupZ plugin;
    private final Map<UUID, UUID> carrying = new HashMap<>(); // носитель -> животное
    private final Map<UUID, BukkitRunnable> runnables = new HashMap<>();
    private final Random random = new Random();

    public AnimalCarryManager(PickupZ plugin) {
        this.plugin = plugin;
    }

    public boolean startCarry(Player carrier, Animals animal) {
        UUID carrierId = carrier.getUniqueId();
        UUID animalId = animal.getUniqueId();

        if (carrying.containsKey(carrierId)) return false;
        if (carrying.containsValue(animalId)) return false;

        // Отключаем физику животного
        animal.setGravity(false);
        animal.setInvulnerable(true);
        animal.setCollidable(false);

        // Сажаем животное на игрока
        carrier.addPassenger(animal);

        carrying.put(carrierId, animalId);
        return true;
    }

    public boolean dropPassenger(Player carrier) {
        UUID carrierId = carrier.getUniqueId();
        UUID animalId = carrying.get(carrierId);

        if (animalId == null) return false;

        Animals animal = (Animals) Bukkit.getEntity(animalId);
        if (animal == null) return false;

        carrier.removePassenger(animal);
        stopCarry(carrier);

        return true;
    }

    public boolean throwPassenger(Player carrier, int distance) {
        UUID carrierId = carrier.getUniqueId();
        UUID animalId = carrying.get(carrierId);

        if (animalId == null) return false;

        Animals animal = (Animals) Bukkit.getEntity(animalId);
        if (animal == null) return false;

        carrier.removePassenger(animal);
        stopCarry(carrier);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Vector direction = carrier.getLocation().getDirection().normalize().multiply(distance);
            direction.setY(0.5);
            animal.setVelocity(direction);

            animal.setGravity(true);
            animal.setCollidable(true);
            animal.setInvulnerable(false);
        });

        return true;
    }

    public void stopCarry(Player carrier) {
        UUID carrierId = carrier.getUniqueId();
        UUID animalId = carrying.remove(carrierId);

        if (animalId != null) {
            Animals animal = (Animals) Bukkit.getEntity(animalId);
            if (animal != null) {
                carrier.removePassenger(animal);
                animal.setGravity(true);
                animal.setCollidable(true);
                animal.setInvulnerable(false);
            }
        }

        BukkitRunnable task = runnables.remove(carrierId);
        if (task != null) task.cancel();
    }

    public boolean isCarrying(Player player) {
        return carrying.containsKey(player.getUniqueId());
    }

    public UUID getPassenger(Player carrier) {
        return carrying.get(carrier.getUniqueId());
    }

    public void clearAll() {
        for (UUID carrierId : carrying.keySet()) {
            Player carrier = Bukkit.getPlayer(carrierId);
            if (carrier != null) stopCarry(carrier);
        }
        carrying.clear();
        runnables.values().forEach(BukkitRunnable::cancel);
        runnables.clear();
    }
}