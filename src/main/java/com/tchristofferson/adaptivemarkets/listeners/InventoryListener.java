package com.tchristofferson.adaptivemarkets.listeners;

import com.tchristofferson.adaptivemarkets.AdaptiveMarkets;
import com.tchristofferson.adaptivemarkets.commands.Permissions;
import com.tchristofferson.adaptivemarkets.core.Merchant;
import com.tchristofferson.adaptivemarkets.core.MerchantManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class InventoryListener implements Listener {

    private final Plugin plugin;
    private final MerchantManager merchantManager;

    public InventoryListener(AdaptiveMarkets plugin) {
        this.plugin = plugin;
        this.merchantManager = plugin.getMerchantManager();
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if (inventory == null || !inventory.equals(merchantManager.getNavigationPage()))
            return;

        event.setCancelled(true);
        ItemStack itemStack = event.getCurrentItem();

        if (itemStack == null)
            return;

        Player player = (Player) event.getWhoClicked();

        if (itemStack.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            Merchant merchant = merchantManager.removeInteractingMerchant(player);

            if (player.hasPermission(Permissions.USE_BUY_INV)) {
                Bukkit.getScheduler().runTask(plugin, () -> merchant.openBuyInventory(player));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Permission Denied!");
            });
        } else if (itemStack.getType() == Material.RED_STAINED_GLASS_PANE) {
            Merchant merchant = merchantManager.removeInteractingMerchant(player);

            if (player.hasPermission(Permissions.USE_SELL_INV)) {
                Bukkit.getScheduler().runTask(plugin, () -> merchant.openSellInventory(player));
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Permission Denied!");
            });
        }
    }

}
