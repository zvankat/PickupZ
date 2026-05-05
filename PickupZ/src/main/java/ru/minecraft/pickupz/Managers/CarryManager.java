package ru.minecraft.pickupz.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import ru.minecraft.pickupz.PickupZ;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class CarryManager {

    private final PickupZ plugin;
    private final Map<UUID, UUID> carrying = new HashMap<>();
    private final Map<UUID, BukkitRunnable> runnables = new HashMap<>();
    private final Random random = new Random();

    public CarryManager(PickupZ plugin) {
        this.plugin = plugin;
    }

    public boolean startCarry(Player carrier, Player passenger) {
        UUID carrierId = carrier.getUniqueId();
        UUID passengerId = passenger.getUniqueId();

        if (carrier.equals(passenger)) return false;
        if (carrying.containsKey(carrierId)) return false;
        if (carrying.containsValue(passengerId)) return false;

        passenger.setGravity(false);
        passenger.setInvulnerable(true);
        passenger.setCollidable(false);
        passenger.setFallDistance(0);

        carrier.addPassenger(passenger);

        carrying.put(carrierId, passengerId);

        return true;
    }

    public boolean passengerDrop(Player passenger) {
        UUID passengerId = passenger.getUniqueId();
        UUID carrierId = null;

        for (Map.Entry<UUID, UUID> entry : carrying.entrySet()) {
            if (entry.getValue().equals(passengerId)) {
                carrierId = entry.getKey();
                break;
            }
        }

        if (carrierId == null) return false;

        Player carrier = Bukkit.getPlayer(carrierId);
        stopCarry(carrier);
        return true;
    }

    public boolean throwPassenger(Player carrier) {
        UUID carrierId = carrier.getUniqueId();
        UUID passengerId = carrying.get(carrierId);

        if (passengerId == null) return false;

        Player passenger = Bukkit.getPlayer(passengerId);
        if (passenger == null) return false;

        carrier.removePassenger(passenger);
        stopCarry(carrier);

        Vector direction = carrier.getLocation().getDirection().normalize();
        double distance = 5 + random.nextInt(6);
        direction.multiply(distance);
        direction.setY(0.5 + random.nextDouble() * 0.5);

        passenger.setVelocity(direction);

        passenger.setGravity(true);
        passenger.setInvulnerable(false);
        passenger.setCollidable(true);

        return true;
    }

    public boolean throwPassengerWithDelay(Player carrier, int distance) {
        UUID carrierId = carrier.getUniqueId();
        UUID passengerId = carrying.get(carrierId);

        if (passengerId == null) return false;

        Player passenger = Bukkit.getPlayer(passengerId);
        if (passenger == null) return false;

        carrier.removePassenger(passenger);

        stopCarry(carrier);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Vector direction = carrier.getLocation().getDirection().normalize().multiply(distance);
            direction.setY(0.5);

            passenger.setVelocity(direction);

            passenger.setGravity(true);
            passenger.setInvulnerable(false);
            passenger.setCollidable(true);
        }, 5L);

        return true;
    }

    public void stopCarry(Player carrier) {
        if (carrier == null) return;

        UUID carrierId = carrier.getUniqueId();
        UUID passengerId = carrying.remove(carrierId);

        if (passengerId != null) {
            Player passenger = Bukkit.getPlayer(passengerId);
            if (passenger != null && passenger.isOnline()) {
                carrier.removePassenger(passenger);
                passenger.setGravity(true);
                passenger.setInvulnerable(false);
                passenger.setCollidable(true);
                passenger.setFallDistance(0);
            }
        }

        BukkitRunnable task = runnables.remove(carrierId);
        if (task != null) task.cancel();
    }

    public boolean isCarrying(Player player) {
        return carrying.containsKey(player.getUniqueId());
    }

    public boolean isBeingCarried(Player player) {
        return carrying.containsValue(player.getUniqueId());
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

    public UUID getPassenger(Player carrier) {
        return carrying.get(carrier.getUniqueId());
    }
}