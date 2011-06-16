package com.fullwall.Citizens.Properties.Properties;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.minecraft.server.InventoryPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.fullwall.Citizens.Constants;
import com.fullwall.Citizens.Interfaces.Saveable;
import com.fullwall.Citizens.NPCs.NPCData;
import com.fullwall.Citizens.NPCs.NPCDataManager;
import com.fullwall.Citizens.NPCs.NPCManager;
import com.fullwall.Citizens.Properties.PropertyHandler;
import com.fullwall.Citizens.Properties.PropertyManager;
import com.fullwall.Citizens.Utils.StringUtils;
import com.fullwall.resources.redecouverte.NPClib.HumanNPC;

public class BasicProperties extends PropertyManager implements Saveable {
	public static Logger log = Logger.getLogger("Minecraft");
	private final String name = ".basic.name";
	private final String color = ".basic.color";
	private final String items = ".basic.items";
	private final String inventory = ".basic.inventory";
	private final String location = ".basic.location";
	private final String lookWhenClose = ".basic.look-when-close";
	private final String talkWhenClose = ".basic.talk-when-close";
	private final String owner = ".basic.owner";
	private final String text = ".basic.text";
	public final PropertyHandler counts = new PropertyHandler(
			"plugins/Citizens/Basic NPCs/Citizens.counts");

	public void saveName(int UID, String npcName) {
		profiles.setString(UID + name, npcName);
	}

	public String getName(int UID) {
		return profiles.getString(UID + name);
	}

	public int getNPCAmountPerPlayer(String name) {
		return counts.getInt(name);
	}

	public void saveNPCAmountPerPlayer(String name, int totalNPCs) {
		counts.setInt(name, totalNPCs);
	}

	public Location getLocation(int UID) {
		String[] values = profiles.getString(UID + location).split(",");
		if (values.length != 6) {
			log.info("getLocationFromName didn't have 6 values in values variable! Length: "
					+ values.length);
			return null;
		} else {
			Location loc = new Location(Bukkit.getServer().getWorld(values[0]),
					Double.parseDouble(values[1]),
					Double.parseDouble(values[2]),
					Double.parseDouble(values[3]), Float.parseFloat(values[4]),
					Float.parseFloat(values[5]));
			return loc;
		}
	}

	public void saveLocation(Location loc, int UID) {
		String locale = loc.getWorld().getName() + "," + loc.getX() + ","
				+ loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + ","
				+ loc.getPitch();
		profiles.setString(UID + location, locale);
	}

	private void saveInventory(int UID, PlayerInventory inv) {
		StringBuffer save = new StringBuffer();
		for (ItemStack i : inv.getContents()) {
			if (i == null || i.getType() == Material.AIR) {
				save.append("AIR,");
			} else {
				save.append(i.getTypeId() + "/" + i.getAmount() + "/"
						+ ((i.getData() == null) ? 0 : i.getData().getData())
						+ ",");
			}
		}
		profiles.setString(UID + inventory, save.toString());
	}

	private PlayerInventory getInventory(int UID) {
		String save = profiles.getString(UID + inventory);
		if (save.isEmpty()) {
			return null;
		}
		ArrayList<ItemStack> array = new ArrayList<ItemStack>();
		for (String s : save.split(",")) {
			String[] split = s.split("/");
			if (!split[0].equals("AIR") && !split[0].equals("0")) {
				array.add(new ItemStack(StringUtils.parse(split[0]),
						StringUtils.parse(split[1]), (short) 0,
						(byte) StringUtils.parse(split[2])));
			} else {
				array.add(null);
			}
		}
		PlayerInventory inv = new CraftInventoryPlayer(
				new InventoryPlayer(null));
		ItemStack[] stacks = inv.getContents();
		inv.setContents(array.toArray(stacks));
		return inv;
	}

	public ArrayList<Integer> getItems(int UID) {
		ArrayList<Integer> array = new ArrayList<Integer>();
		String current = profiles.getString(UID + items);
		if (current.isEmpty()) {
			current = "0,0,0,0,0,";
			profiles.setString(UID + items, current);
		}
		for (String s : current.split(",")) {
			array.add(Integer.parseInt(s));
		}
		return array;
	}

	private void saveItems(int UID, ArrayList<Integer> items2) {
		StringBuffer toSave = new StringBuffer();
		for (Integer i : items2) {
			toSave.append(i + ",");
		}
		profiles.setString(UID + items, toSave.toString());
	}

	public int getColour(int UID) {
		try {
			return profiles.getInt(UID + color, 0xf);
		} catch (NumberFormatException ex) {
			int colour = 0xf;
			if (profiles.pathExists(UID + color)
					&& !profiles.getString(UID + color).isEmpty()) {
				try {
					colour = Integer.parseInt(""
							+ profiles.getString(UID + color)
									.charAt(profiles.getString(UID + color)
											.length() - 1));
				} catch (NumberFormatException e) {
					try {
						colour = Integer
								.parseInt(
										""
												+ profiles.getString(
														UID + color).charAt(
														profiles.getString(
																UID + color)
																.length() - 1),
										16);
					} catch (NumberFormatException exa) {
					}
				}
			}
			saveColour(UID, colour);
			return colour;
		}
	}

	private void saveColour(int UID, int colour) {
		profiles.setInt(UID + color, colour);
	}

	public ArrayDeque<String> getText(int UID) {
		String current = profiles.getString(UID + text);
		if (!current.isEmpty()) {
			ArrayDeque<String> texts = new ArrayDeque<String>();
			for (String string : current.split(";")) {
				texts.push(string);
			}
			return texts;
		}
		return null;
	}

	public void getSetText(int UID) {
		String current = profiles.getString(UID + text);
		if (!current.isEmpty()) {
			ArrayDeque<String> texts = new ArrayDeque<String>();
			for (String string : current.split(";")) {
				texts.push(string);
			}
			NPCManager.setText(UID, texts);
		}
	}

	private void saveText(int UID, ArrayDeque<String> texts) {
		StringBuffer buf = new StringBuffer();
		if (texts != null) {
			for (String string : texts) {
				buf.append(string + ";");
			}
		}
		profiles.setString(UID + text, buf.toString());
	}

	public boolean getLookWhenClose(int UID) {
		return profiles.getBoolean(UID + lookWhenClose,
				Constants.defaultFollowingEnabled);
	}

	public void saveLookWhenClose(int UID, boolean value) {
		profiles.setBoolean(UID + lookWhenClose, value);
	}

	public boolean getTalkWhenClose(int UID) {
		return profiles.getBoolean(UID + talkWhenClose,
				Constants.defaultTalkWhenClose);
	}

	public void saveTalkWhenClose(int UID, boolean value) {
		profiles.setBoolean(UID + talkWhenClose, value);
	}

	public String getOwner(int UID) {
		return profiles.getString(UID + owner);
	}

	public void setOwner(int UID, String name) {
		profiles.setString(UID + owner, name);
	}

	public int getNewNpcID() {
		int count = 0;
		while (profiles.pathExists(count)) {
			count++;
		}
		return count;
	}

	@Override
	public void saveState(HumanNPC npc) {
		int UID = npc.getUID();
		NPCData npcdata = npc.getNPCData();

		saveName(npc.getUID(), npcdata.getName());
		saveLocation(npcdata.getLocation(), UID);
		saveColour(UID, npcdata.getColour());
		saveItems(UID, npcdata.getItems());
		saveInventory(UID, npc.getPlayer().getInventory());
		saveText(UID, npcdata.getTexts());
		saveLookWhenClose(UID, npcdata.isLookClose());
		saveTalkWhenClose(UID, npcdata.isTalkClose());
		setOwner(UID, npcdata.getOwner());
	}

	@Override
	public void loadState(HumanNPC npc) {
		int UID = npc.getUID();
		NPCData npcdata = npc.getNPCData();

		npcdata.setName(getName(UID));
		npcdata.setLocation(getLocation(UID));
		npcdata.setColour(getColour(UID));
		npcdata.setItems(getItems(UID));
		npcdata.setTexts(getText(UID));
		npcdata.setLookClose(getLookWhenClose(UID));
		npcdata.setTalkClose(getTalkWhenClose(UID));
		npcdata.setOwner(getOwner(UID));
		if (getInventory(npc.getUID()) != null) {
			npc.getInventory().setContents(
					getInventory(npc.getUID()).getContents());
		}

		NPCDataManager.addItems(npc, npcdata.getItems());
		saveState(npc);
	}

	@Override
	public void register(HumanNPC npc) {
	}

	@Override
	public void setEnabled(HumanNPC npc, boolean value) {
	}

	@Override
	public boolean getEnabled(HumanNPC npc) {
		return true;
	}

	@Override
	public PropertyType type() {
		return PropertyType.BASIC;
	}

	@Override
	public void copy(int UID, int nextUID) {
		if (profiles.pathExists(UID + name)) {
			profiles.setString(nextUID + name, profiles.getString(UID + name));
		}
		if (profiles.pathExists(UID + text)) {
			profiles.setString(nextUID + text, profiles.getString(UID + text));
		}
		if (profiles.pathExists(UID + location)) {
			profiles.setString(nextUID + location,
					profiles.getString(UID + location));
		}
		if (profiles.pathExists(UID + color)) {
			profiles.setString(nextUID + color, profiles.getString(UID + color));
		}
		if (profiles.pathExists(UID + owner)) {
			profiles.setString(nextUID + owner, profiles.getString(UID + owner));
		}
		if (profiles.pathExists(UID + items)) {
			profiles.setString(nextUID + items, profiles.getString(UID + items));
		}
		if (profiles.pathExists(UID + inventory)) {
			profiles.setString(nextUID + inventory,
					profiles.getString(UID + inventory));
		}
		if (profiles.pathExists(UID + talkWhenClose)) {
			profiles.setString(nextUID + talkWhenClose,
					profiles.getString(UID + talkWhenClose));
		}
		if (profiles.pathExists(UID + lookWhenClose)) {
			profiles.setString(nextUID + lookWhenClose,
					profiles.getString(UID + lookWhenClose));
		}
	}
}