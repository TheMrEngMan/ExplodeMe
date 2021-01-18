package com.MrEngMan.ExplodeMe.listeners;

import com.MrEngMan.ExplodeMe.ExplodeMe;
import com.MrEngMan.ExplodeMe.util.LocationUUIDPair;
import com.MrEngMan.ExplodeMe.util.Utils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.UUID;

public class Listeners implements Listener {

    // used to store which player caused explosions
    public static final String METADATA_KEY = "EMInteractedPlayerName";
    public ArrayList<LocationUUIDPair> blockExplosionCauseList = new ArrayList<>();
    
    public Listeners() {}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // If a player right clicks a block
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Block block = event.getClickedBlock();
            Material blockType = event.getClickedBlock().getType();
            boolean blockIsBed = Tag.BEDS.getValues().contains(blockType);
            boolean blockIsRespawnAnchor = (blockType == Material.RESPAWN_ANCHOR);

            // If it's a bed or respawn anchor
            if (blockIsBed || blockIsRespawnAnchor) {
                if (block.getLocation().getWorld() != null) {

                    // If it's a bed in the nether or end
                    if (blockIsBed && (block.getLocation().getWorld().getEnvironment() == World.Environment.NETHER || block.getLocation().getWorld().getEnvironment() == World.Environment.THE_END)) {
                        Utils.debugPrint(Utils.SendChatMessage("&cBed will explode, triggered by " + player.getName()) + " @ " + block.getLocation());
                        blockExplosionCauseList.add(new LocationUUIDPair(block.getLocation(), player.getUniqueId()));
                    }

                    // If it's a respawn anchor in the overworld
                    else if (blockIsRespawnAnchor && block.getLocation().getWorld().getEnvironment() == World.Environment.NORMAL) {
                        RespawnAnchor respawnAnchor = ((RespawnAnchor)block.getBlockData());
                        if(respawnAnchor.getCharges() > 0) {

                            // If it is charged enough to explode after this click
                            if(player.getInventory().getItemInMainHand().getType() != Material.GLOWSTONE || respawnAnchor.getCharges() == 4){
                                Utils.debugPrint(Utils.SendChatMessage("&cRespawn anchor will explode, triggered by " + player.getName()) + " @ " + block.getLocation());
                                blockExplosionCauseList.add(new LocationUUIDPair(block.getLocation(), player.getUniqueId()));
                            }

                        }

                    }
                }
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();
        Entity damagedEntity = event.getEntity();

        // End crystal damaged by player or projectile shot by player
        if (damagedEntity.getType() == EntityType.ENDER_CRYSTAL) {
            if (damagerEntity instanceof Player && ((Player) damagerEntity).getPlayer() != null) {
                Player player = ((Player) damagerEntity).getPlayer();
                Utils.debugPrint(Utils.SendChatMessage("&cEnd crystal will explode, triggered by " + player.getName()) + " @ " + damagedEntity.getLocation());
                damagedEntity.setMetadata(METADATA_KEY, new FixedMetadataValue(ExplodeMe.getPlugin(), event.getDamager().getUniqueId()));
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();
                ProjectileSource shooter = projectile.getShooter();
                if (shooter instanceof Player && ((Player) shooter).getPlayer() != null) {
                    Player player = ((Player) shooter).getPlayer();
                    Utils.debugPrint(Utils.SendChatMessage("&cEnd crystal will explode, triggered by projectile of " + player.getName()) + " @ " + damagedEntity.getLocation());
                    damagedEntity.setMetadata(METADATA_KEY, new FixedMetadataValue(ExplodeMe.getPlugin(), player.getUniqueId()));
                }
            }
        }

        // Player damaged by end crystal
        else if (damagedEntity.getType() == EntityType.PLAYER && damagerEntity.getType() == EntityType.ENDER_CRYSTAL) {

            // Don't damage this player if explosion was caused by different player
            MetadataValue metadataValue = damagerEntity.getMetadata(METADATA_KEY).get(0);
            if(metadataValue != null && metadataValue.value() != null) {
                if(!metadataValue.value().toString().equals(damagedEntity.getUniqueId().toString())) {
                    Utils.debugPrint(Utils.SendChatMessage("&eDid NOT damage: " + damagedEntity.getName()));
                    event.setCancelled(true);
                }
                else {
                    Utils.debugPrint(Utils.SendChatMessage("&eDamaged: " + damagedEntity.getName()));
                }
            }

        }

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
        Entity damagedEntity = event.getEntity();

        // Player damaged by block explosion
        if (damagedEntity.getType() == EntityType.PLAYER && event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            Utils.debugPrint(Utils.SendChatMessage("&3Check cause list: " + blockExplosionCauseList.toString()));

            // Find which player triggered an explosion close by
            UUID explodeCauseUUID = null;
            for(LocationUUIDPair locationUUIDPair : blockExplosionCauseList) {
                if(locationUUIDPair.location.getWorld() == damagedEntity.getWorld() && locationUUIDPair.location.distance(damagedEntity.getLocation()) <= ExplodeMe.getPlugin().getBlockExplodeCheckRadius()) {
                    explodeCauseUUID = locationUUIDPair.uuid;
                }
            }
            Utils.debugPrint(Utils.SendChatMessage("&3UUID of explosion cause: " + (explodeCauseUUID == null ? "null" : explodeCauseUUID)));

            // Don't damage this player if explosion was caused by different player
            if(explodeCauseUUID != null ) {
                if(!explodeCauseUUID.toString().equals(damagedEntity.getUniqueId().toString())) {
                    Utils.debugPrint(Utils.SendChatMessage("&eDid NOT damage: " + damagedEntity.getName()));
                    event.setCancelled(true);
                }
                else {
                    Utils.debugPrint(Utils.SendChatMessage("&eDamaged: " + damagedEntity.getName()));
                }
            }

        }

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockExplodeEvent(BlockExplodeEvent event) {

        // After a block exploded, clear data about the player who caused it to explode
        Block damagerBlock = event.getBlock();
        Utils.debugPrint(Utils.SendChatMessage("&e" + damagerBlock.getType() + " exploded @ location: " + damagerBlock.getLocation()));
        blockExplosionCauseList.removeIf(locationUUIDPair -> locationUUIDPair.location.distance(damagerBlock.getLocation()) < 2);
        Utils.debugPrint(Utils.SendChatMessage("&3Set cause list: " + blockExplosionCauseList.toString()));

    }

}
