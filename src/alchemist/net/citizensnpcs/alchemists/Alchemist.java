package net.citizensnpcs.alchemists;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.PermissionManager;
import net.citizensnpcs.commands.CommandHandler;
import net.citizensnpcs.npctypes.CitizensNPC;
import net.citizensnpcs.properties.Properties;
import net.citizensnpcs.resources.npclib.HumanNPC;
import net.citizensnpcs.utils.InventoryUtils;
import net.citizensnpcs.utils.MessageUtils;
import net.citizensnpcs.utils.Messaging;

public class Alchemist extends CitizensNPC {
	// ItemStack = result, String = recipe
	private HashMap<ItemStack, String> recipes = new HashMap<ItemStack, String>();

	public HashMap<ItemStack, String> getRecipes() {
		return recipes;
	}

	public String getRecipe(ItemStack result) {
		return recipes.get(result);
	}

	@Override
	public String getType() {
		return "alchemist";
	}

	@Override
	public Properties getProperties() {
		return new AlchemistProperties();
	}

	@Override
	public CommandHandler getCommands() {
		return new AlchemistCommands();
	}

	@Override
	public void onRightClick(Player player, HumanNPC npc) {
		if (!PermissionManager.generic(player,
				"citizens.alchemist.use.interact")) {
			Messaging.sendError(player, MessageUtils.noPermissionsMessage);
			return;
		}
		InventoryUtils.showInventory(npc, player);
	}
}