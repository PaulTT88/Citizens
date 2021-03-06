package net.citizensnpcs.guards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.citizensnpcs.api.CitizensManager;
import net.citizensnpcs.api.event.npc.NPCCreateEvent.NPCCreateReason;
import net.citizensnpcs.api.event.npc.NPCRemoveEvent.NPCRemoveReason;
import net.citizensnpcs.guards.flags.FlagList;
import net.citizensnpcs.misc.NPCLocation;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.resources.npclib.NPCManager;
import net.citizensnpcs.utils.LocationUtils;
import net.citizensnpcs.utils.PathUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GuardTask implements Runnable {
	private final static Map<String, NPCLocation> toRespawn = new HashMap<String, NPCLocation>();

	@Override
	public void run() {
		for (HumanNPC npc : CitizensManager.getList().values()) {
			if (npc.isType("guard")) {
				Guard guard = npc.getType("guard");
				if (guard.isAttacking()) {
					boolean cancel = false;
					if (!npc.getHandle().hasTarget() || !guard.isAggressive()) {
						cancel = true;
					} else if (npc.getHandle().hasTarget()
							&& !LocationUtils.withinRange(npc.getLocation(),
									npc.getHandle().getTarget().getLocation(),
									guard.getProtectionRadius())) {
						cancel = true;
					} else if (npc.getHandle().hasTarget()
							&& guard.isBodyguard()
							&& Bukkit.getServer().getPlayer(npc.getOwner()) != null) {
						Player player = Bukkit.getServer().getPlayer(
								npc.getOwner());
						if (npc.getHandle().getTarget() != player
								&& !LocationUtils.withinRange(
										npc.getLocation(),
										player.getLocation(),
										guard.getProtectionRadius())) {
							cancel = true;
						}
					}
					if (cancel) {
						npc.getHandle().cancelTarget();
						GuardManager.returnToBase(guard, npc);
						guard.setAttacking(false);
					}
				}
				if (guard.isAttacking()) {
					continue;
				}
				if (npc.isPaused()
						&& LocationUtils.withinRange(npc.getLocation(), npc
								.getNPCData().getLocation())) {
					npc.setPaused(false);
				}
				if (guard.isBouncer()) {
					handleTarget(npc.getPlayer(), npc, guard);
				} else if (guard.isBodyguard()) {
					if (!npc.isPaused()) {
						npc.setPaused(true);
					}
					Player p = Bukkit.getServer().getPlayer(npc.getOwner());
					if (p != null) {
						handleTarget(p, npc, guard);
						if (LocationUtils.withinRange(npc.getLocation(),
								p.getLocation(), guard.getProtectionRadius())) {
							PathUtils.target(npc, p, false, -1, -1, 25);
						} else {
							npc.teleport(p.getLocation());
						}
					} else {
						if (CitizensManager.getNPC(npc.getUID()) != null) {
							toRespawn.put(
									npc.getOwner(),
									new NPCLocation(npc.getLocation(), npc
											.getUID(), npc.getOwner()));
							NPCManager.despawn(npc.getUID(),
									NPCRemoveReason.DEATH);
						}
					}
				}
			}
		}
	}

	private void handleTarget(Player player, HumanNPC npc, Guard guard) {
		if (!guard.isAggressive()) {
			return;
		}
		FlagList flags = guard.getFlags();
		flags.processEntities(npc, player.getLocation(),
				getNearby(player, guard));
		if (flags.getResult() != null) {
			guard.target(flags.getResult(), npc);
		}
	}

	private List<Entity> getNearby(Player player, Guard guard) {
		return player.getNearbyEntities(guard.getHalvedProtectionRadius(),
				guard.getProtectionRadius(), guard.getHalvedProtectionRadius());
	}

	public static void checkRespawn(Player player) {
		String owner = player.getName();
		if (toRespawn.containsKey(owner)) {
			NPCManager.register(toRespawn.get(owner).getUID(), owner,
					NPCCreateReason.RESPAWN);
			CitizensManager.getNPC(toRespawn.get(owner).getUID()).teleport(
					player.getLocation());
		}
	}
}