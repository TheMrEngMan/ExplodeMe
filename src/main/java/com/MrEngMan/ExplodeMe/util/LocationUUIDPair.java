package com.MrEngMan.ExplodeMe.util;

import org.bukkit.Location;
import java.util.UUID;

public class LocationUUIDPair {
    public Location location;
    public UUID uuid;

    public LocationUUIDPair(Location location, UUID uuid) {
        this.location = location;
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "Loc: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() +  " | UUID(4): " + uuid.toString().substring(0, 4);
    }

}
