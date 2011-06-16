package com.fullwall.Citizens.NPCs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.fullwall.Citizens.Citizens;
import com.fullwall.Citizens.Constants;
import com.fullwall.Citizens.Permission;
import com.fullwall.Citizens.Properties.PropertyManager;
import com.fullwall.Citizens.Utils.StringUtils;
import com.fullwall.resources.redecouverte.NPClib.HumanNPC;
import com.fullwall.resources.redecouverte.NPClib.NPCList;
import com.fullwall.resources.redecouverte.NPClib.NPCSpawner;

public class NPCManager {

	@SuppressWarnings("unused")
	private final Citizens plugin;
	public static ConcurrentHashMap<Integer, String> GlobalUIDs = new ConcurrentHashMap<Integer, String>();
	public static ConcurrentHashMap<Integer, ArrayDeque<String>> NPCTexts = new ConcurrentHashMap<Integer, ArrayDeque<String>>();
	public static ConcurrentHashMap<String, Integer> selectedNPCs = new ConcurrentHashMap<String, Integer>();
	private static NPCList list;

	public NPCManager(Citizens plugin) {
		this.plugin = plugin;
		list = new NPCList();
	}

	/**
	 * Spawns a new npc and registers it.
	 * 
	 * @param UID
	 * @param owner
	 */
	public static void register(int UID, String owner) {
		Location loc = PropertyManager.getBasic().getLocation(UID);

		int colour = PropertyManager.getBasic().getColour(UID);
		String name = PropertyManager.getBasic().getName(UID);
		name = ChatColor.stripColor(name);
		String npcName = name;
		if (colour != 0xf) {
			npcName = ChatColor.getByCode(colour) + name;
		}
		if (Constants.convertSlashes == true) {
			String[] brokenName = npcName.split(Constants.convertToSpaceChar);
			for (int i = 0; i < brokenName.length; i++) {
				if (i == 0) {
					npcName = brokenName[i];
				} else {
					npcName += " " + brokenName[i];
				}
			}
		}
		HumanNPC npc = NPCSpawner.spawnBasicHumanNpc(UID, npcName,
				loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(),
				loc.getYaw(), 0F);
		npc.teleport(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), 0F);
		ArrayList<Integer> items = PropertyManager.getBasic().getItems(UID);

		npc.setNPCData(new NPCData(name, UID, loc, colour, items, NPCTexts
				.get(UID), PropertyManager.getBasic().getLookWhenClose(UID),
				PropertyManager.getBasic().getTalkWhenClose(UID), owner));
		PropertyManager.getBasic().setOwner(UID, owner);
		PropertyManager.load(npc);

		registerUID(UID, name);
		list.put(UID, npc);

		PropertyManager.save(npc);

		npc.getPlayer().setSleepingIgnored(true);
	}

	/**
	 * Registers a new npc.
	 * 
	 * @param name
	 * @param loc
	 * @param owner
	 * @return
	 */
	public static int register(String name, Location loc, String owner) {
		int UID = PropertyManager.getBasic().getNewNpcID();
		PropertyManager.getBasic().saveLocation(loc, UID);
		PropertyManager.getBasic().saveLookWhenClose(UID,
				Constants.defaultFollowingEnabled);
		PropertyManager.getBasic().saveTalkWhenClose(UID,
				Constants.defaultTalkWhenClose);
		PropertyManager.getBasic().saveName(UID, name);
		register(UID, owner);
		return UID;
	}

	/**
	 * Sets an npc's text to the given texts.
	 * 
	 * @param UID
	 * @param text
	 */
	public static void setText(int UID, ArrayDeque<String> text) {
		text = StringUtils.colourise(text);
		NPCTexts.put(UID, text);
		get(UID).getNPCData().setTexts(text);
	}

	/**
	 * Returns an npc's text.
	 * 
	 * @param UID
	 * @return
	 */
	public static ArrayDeque<String> getText(int UID) {
		return NPCTexts.get(UID);
	}

	/**
	 * Resets an NPC's text.
	 * 
	 * @param UID
	 */
	public static void resetText(int UID) {
		setText(UID, new ArrayDeque<String>());
	}

	/**
	 * Gets an npc from a UID.
	 * 
	 * @param UID
	 * @return
	 */
	public static HumanNPC get(int UID) {
		return list.get(UID);
	}

	/**
	 * Gets an npc from an entity.
	 * 
	 * @param entity
	 * @return
	 */
	public static HumanNPC get(Entity entity) {
		return list.getNPC(entity);
	}

	/**
	 * Gets the list of npcs.
	 * 
	 * @return
	 */
	public static NPCList getList() {
		return list;
	}

	/**
	 * Rotates an npc.
	 * 
	 * @param npc
	 * @param player
	 */
	public static void facePlayer(HumanNPC npc, Player player) {
		Location loc = npc.getLocation();
		double xDiff = player.getLocation().getX() - loc.getX();
		double yDiff = player.getLocation().getY() - loc.getY();
		double zDiff = player.getLocation().getZ() - loc.getZ();
		double DistanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
		double DistanceY = Math.sqrt(DistanceXZ * DistanceXZ + yDiff * yDiff);
		double yaw = (Math.acos(xDiff / DistanceXZ) * 180 / Math.PI);
		double pitch = (Math.acos(yDiff / DistanceY) * 180 / Math.PI) - 90;
		if (zDiff < 0.0) {
			yaw = yaw + (Math.abs(180 - yaw) * 2);
		}
		npc.teleport(loc.getX(), loc.getY(), loc.getZ(), (float) yaw - 90,
				(float) pitch);
		if (npc.getOwner().equals(player.getName()))
			npc.getNPCData().setLocation(npc.getLocation());
	}

	/**
	 * Despawns an npc.
	 * 
	 * @param UID
	 */
	public static void despawn(int UID) {
		GlobalUIDs.remove(UID);
		NPCSpawner.removeBasicHumanNpc(list.get(UID));
		list.remove(UID);
	}

	/**
	 * Removes an npc.
	 * 
	 * @param UID
	 */
	public void remove(int UID) {
		PropertyManager.remove(get(UID));
		GlobalUIDs.remove(UID);
		NPCSpawner.removeBasicHumanNpc(list.get(UID));
		list.remove(UID);
	}

	/**
	 * Removes an npc, but not from the properties.
	 * 
	 * @param UID
	 */
	public static void removeForRespawn(int UID) {
		PropertyManager.save(list.get(UID));
		NPCSpawner.removeBasicHumanNpc(list.get(UID));
	}

	/**
	 * Gets the global list of UIDs.
	 * 
	 * @return
	 */
	public ConcurrentHashMap<Integer, String> getUIDs() {
		return GlobalUIDs;
	}

	/**
	 * Registers a UID in the global list.
	 * 
	 * @param UID
	 * @param name
	 */
	private static void registerUID(int UID, String name) {
		GlobalUIDs.put(UID, name);
	}

	/**
	 * Checks if a given entity is an npc.
	 * 
	 * @param entity
	 * @return
	 */
	public static boolean isNPC(Entity entity) {
		return list.getNPC(entity) != null;
	}

	/**
	 * Checks if a player has an npc selected.
	 * 
	 * @param p
	 * @return
	 */
	public static boolean validateSelected(Player p) {
		if (selectedNPCs.get(p.getName()) != null
				&& !selectedNPCs.get(p.getName()).toString().isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the player has selected the given npc.
	 * 
	 * @param p
	 * @param npc
	 * @return
	 */
	public static boolean validateSelected(Player p, int UID) {
		if (selectedNPCs.get(p.getName()) != null
				&& !selectedNPCs.get(p.getName()).toString().isEmpty()) {
			if (selectedNPCs.get(p.getName()) == UID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Overloaded method to add an optional permission string parameter (admin
	 * overrides)
	 * 
	 * @param player
	 * @param UID
	 * @param permission
	 * @return
	 */
	public static boolean validateOwnership(Player player, int UID,
			String permission) {
		if (Permission.generic(player,
				permission.replace("citizens.", "citizens.admin."))) {
			return true;
		}
		if (validateOwnership(player, UID)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if a player owns a given npc.
	 * 
	 * @param UID
	 * @param p
	 * @return
	 */
	public static boolean validateOwnership(Player player, int UID) {
		if (get(UID).getOwner().equals(player.getName())) {
			return true;
		}
		return false;
	}
}