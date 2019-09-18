package com.tchristofferson.adaptivemarkets;

import co.aikar.commands.BukkitCommandManager;
import com.tchristofferson.adaptivemarkets.commands.AdaptiveMarketsCommand;
import com.tchristofferson.adaptivemarkets.core.Merchant;
import com.tchristofferson.adaptivemarkets.core.MerchantManager;
import com.tchristofferson.adaptivemarkets.core.PlayerManager;
import com.tchristofferson.adaptivemarkets.listeners.InventoryListener;
import com.tchristofferson.pagedinventories.PagedInventoryAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptiveMarkets extends JavaPlugin {

    private Economy econ;
    private PagedInventoryAPI pagedInventoryAPI;
    private MerchantManager merchantManager;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            Bukkit.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        pagedInventoryAPI = new PagedInventoryAPI(this);

        Map<String, Merchant> map = new HashMap<>(1);
        map.put("DEFAULT", new Merchant(pagedInventoryAPI, "DEFAULT", "&cDefault Merchant", new HashMap<>(), new HashMap<>(), Villager.Profession.NONE, Villager.Type.PLAINS));
        Map<String, List<NPC>> spawnedMap = new HashMap<>();
        spawnedMap.put("DEFAULT", new ArrayList<>());
        merchantManager = new MerchantManager(map, spawnedMap);

        playerManager = new PlayerManager();
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);

        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.enableUnstableAPI("help");
        commandManager.registerCommand(new AdaptiveMarketsCommand(this));
        CitizensAPI.registerEvents(merchantManager);
    }

    public PagedInventoryAPI getPagedInventoryAPI() {
        return pagedInventoryAPI;
    }

    public MerchantManager getMerchantManager() {
        return merchantManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public Economy getEconomy() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
            return false;

        econ = rsp.getProvider();
        return econ != null;
    }
}
