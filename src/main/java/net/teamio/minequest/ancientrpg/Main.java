/*
 * This file is part of MineQuest-AncientRPG, AncientRPG addon to MineQuest.
 * MineQuest-AncientRPG is licensed under GNU General Public License v3.
 * Copyright (C) 2012 The MineQuest Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.teamio.minequest.ancientrpg;

import java.io.File;

import net.teamio.minequest.ancientrpg.event.AssignClassEvent;
import net.teamio.minequest.ancientrpg.event.RewardExpEvent;
import net.teamio.minequest.ancientrpg.group.AncientQuestGroupManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.theminequest.MineQuest.API.Managers;
import com.theminequest.MineQuest.API.Quest.Quest;
import com.theminequest.MineQuest.API.Quest.QuestDetails;
import com.theminequest.MineQuest.API.Utils.PropertiesFile;

public class Main extends JavaPlugin implements Listener {

	public static final String PROPERTIES_NAME = "ancientrpg.properties";
	private PropertiesFile properties;
	
	public static AncientQuestGroupManager questGroupManager = null;
	
	@Override
	public void onDisable() {
		
	}

	@Override
	public void onEnable() {
		if (getServer().getPluginManager().getPlugin("MineQuest") == null) {
			getServer().getLogger().severe("============ teamIO : AncientRPG ============");
			getServer().getLogger().severe("MineQuest is required for this addon!");
			getServer().getLogger().severe("Please install MineQuest first!");
			getServer().getLogger().severe("You can find the latest version here:");
			getServer().getLogger().severe("http://dev.bukkit.org/server-mods/minequest/");
			getServer().getLogger().severe("=============================================");
			setEnabled(false);
			return;
		}
		if (getServer().getPluginManager().getPlugin("AncientRPG") == null) {
			getServer().getLogger().severe("============= teamIO : AncientRPG =============");
			getServer().getLogger().severe("AncientRPG is required for this addon!");
			getServer().getLogger().severe("Please install AncientRPG first!");
			getServer().getLogger().severe("You can find the latest version here:");
			getServer().getLogger().severe("http://dev.bukkit.org/server-mods/ancient-rpg/");
			getServer().getLogger().severe("===============================================");
			setEnabled(false);
			return;
		}
		properties = new PropertiesFile(Managers.getActivePlugin().getDataFolder().getAbsolutePath()+File.separator+PROPERTIES_NAME);
		questGroupManager = new AncientQuestGroupManager();
		getServer().getPluginManager().registerEvents(questGroupManager, this);
		
		Managers.setGroupManager(questGroupManager);
		Managers.setQuestGroupManager(questGroupManager);
		
		Managers.getEventManager().addEvent("AssignClassEvent", AssignClassEvent.class);
		Managers.getEventManager().addEvent("RewardExpEvent", RewardExpEvent.class);
	}
	
}