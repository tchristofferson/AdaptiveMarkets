package com.tchristofferson.adaptivemarkets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.tchristofferson.adaptivemarkets.AdaptiveMarkets;
import com.tchristofferson.adaptivemarkets.core.MarketItemInfo;
import com.tchristofferson.adaptivemarkets.core.Merchant;
import com.tchristofferson.adaptivemarkets.core.MerchantManager;
import com.tchristofferson.adaptivemarkets.core.PlayerManager;
import com.tchristofferson.pagedinventories.PagedInventoryAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

@CommandAlias("adaptiveMarkets|adaptiveMarket|aMarkets|am")
public class AdaptiveMarketsCommand extends BaseCommand {

    private final PagedInventoryAPI pagedInventoryAPI;
    private final MerchantManager merchantManager;
    private final PlayerManager playerManager;

    public AdaptiveMarketsCommand(AdaptiveMarkets plugin) {
        this.pagedInventoryAPI = plugin.getPagedInventoryAPI();
        this.merchantManager = plugin.getMerchantManager();
        this.playerManager = plugin.getPlayerManager();
    }

    @Subcommand("spawnMerchant|sm")
    @Syntax("<merchantType> &e- Spawn a new merchant")
    @CommandPermission(Permissions.SPAWN_MERCHANT)
    public void spawnMerchant(Player player, String merchantType) {
        if (merchantManager.create(merchantType, player.getLocation()) == null) {
            player.sendMessage(ChatColor.RED + "Merchant type not found!");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Successfully spawned merchant!");
    }

    @Subcommand("createMerchant|cm")
    @Syntax("<merchantType> <displayName> <villagerProfession> <villagerType> &e- Create a new merchant type")
    @CommandPermission(Permissions.CREATE_MERCHANT)
    public void createMerchant(CommandSender sender, String merchantType, String displayName, String villagerProfession, String villagerType) {
        if (merchantManager.getMerchant(merchantType) != null) {
            sender.sendMessage(ChatColor.RED + "That merchant type already exists!");
            return;
        }

        Villager.Profession profession;
        Villager.Type type;

        try {
            profession = Villager.Profession.valueOf(villagerProfession.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String prefix = ChatColor.RED + "Invalid profession!\n" + ChatColor.GRAY + "Available professions:\n" + ChatColor.YELLOW;
            sender.sendMessage(getValuesAsMessage(prefix, Villager.Profession.values()));
            return;
        }

        try {
            type = Villager.Type.valueOf(villagerType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String prefix = ChatColor.RED + "Invalid villager type!\n" + ChatColor.GRAY + "Available villager types:\n" + ChatColor.YELLOW;
            sender.sendMessage(getValuesAsMessage(prefix, Villager.Type.values()));
            return;
        }

        Merchant merchant = new Merchant(pagedInventoryAPI, merchantType, displayName.replace("_", " "), new HashMap<>(), new HashMap<>(), profession, type);
        merchantManager.addMerchant(merchantType, merchant);
        sender.sendMessage(ChatColor.GREEN + "Successfully create merchant!");
    }

    //Not used for removing individually spawned merchants, but rather whole merchant types along with merchants that are spawned of specified type
    @Subcommand("deleteMerchant|deMerchant|deMerch|dm")
    @Syntax("<merchantType> &e- Delete a merchant type and all of its merchants that are spawned")
    @CommandPermission(Permissions.DELETE_MERCHANT)
    public void deleteMerchant(CommandSender sender, String merchantType) {
        if (merchantManager.removeMerchant(merchantType)) {
            sender.sendMessage(ChatColor.GREEN + "Successfully deleted merchant type!");
            return;
        }

        sender.sendMessage(ChatColor.RED + "Merchant type not found!");
    }

    @Subcommand("addBuyItem|abi")
    @Syntax("<merchantType> <price> <priceChange> <priceDecreaseCondition> <supply> <minPrice> <maxPrice> &e- Add the item your holding to the merchant's buy inventory so they can sell it to player's")
    @CommandPermission(Permissions.MODIFY_ITEMS)
    public void addBuyItem(Player player, String merchantType, double price, double priceChange, int priceChangeCondition,
                           int supply, double minPrice, double maxPrice) {
        ItemStack holding = player.getInventory().getItemInMainHand();
        if (holding.getType().name().endsWith("AIR")) {
            player.sendMessage(ChatColor.RED + "Hold the item you want to add!");
            return;
        }

        if (handleNumberChecksFails(player, price, priceChange, supply, minPrice, maxPrice))
            return;

        if (priceChangeCondition > supply) {
            player.sendMessage(ChatColor.RED + "The price change condition cannot higher than supply!");
            return;
        }

        Merchant merchant = merchantManager.getMerchant(merchantType);

        if (merchant == null) {
            player.sendMessage(ChatColor.RED + "Merchant type not found!");
            return;
        }

        ItemStack clone = holding.clone();
        MarketItemInfo marketItemInfo = new MarketItemInfo(clone, price, priceChange, minPrice, maxPrice, priceChangeCondition, supply);
        merchant.addBuyItem(clone, marketItemInfo, true);
        player.sendMessage(ChatColor.GREEN + "Successfully added item to merchant's buy inventory!");
    }

    @Subcommand("addSellItem|asi")
    @Syntax("<merchantType> <price> <priceChange> <priceIncreaseCondition> <supply> <minPrice> <maxPrice> &e- Add the item your holding to the merchant's sell inventory so they can buy it from players")
    @CommandPermission(Permissions.MODIFY_ITEMS)
    public void addSellItem(Player player, String merchantType, double price, double priceChange, int priceChangeCondition,
                            int supply, double minPrice, double maxPrice) {
        ItemStack holding = player.getInventory().getItemInMainHand();
        if (holding.getType().name().endsWith("AIR")) {
            player.sendMessage(ChatColor.RED + "Hold the item you want to add!");
            return;
        }

        if (handleNumberChecksFails(player, price, priceChange, supply, minPrice, maxPrice))
            return;

        if (priceChangeCondition < 1) {
            player.sendMessage(ChatColor.RED + "The price change condition cannot be less than 0 or be greater than 1!");
            return;
        }

        Merchant merchant = merchantManager.getMerchant(merchantType);

        if (merchant == null) {
            player.sendMessage(ChatColor.RED + "Merchant type not found!");
            return;
        }

        ItemStack clone = holding.clone();
        MarketItemInfo marketItemStack = new MarketItemInfo(clone, price, priceChange, minPrice, maxPrice, priceChangeCondition, supply);
        merchant.addSellItem(clone, marketItemStack, true);
        player.sendMessage(ChatColor.GREEN + "Successfully added item to merchant's sell inventory!");
    }

    @Subcommand("removeBuyItem|rbi")
    @Syntax("<merchantType> &e- Remove an item from a merchant's buy inventory")
    @CommandPermission(Permissions.MODIFY_ITEMS)
    public void removeBuyItem(Player player, String merchantType) {
        Merchant merchant = merchantManager.getMerchant(merchantType);

        if (merchant == null) {
            player.sendMessage(ChatColor.RED + "Merchant type not found!");
            return;
        }

        merchant.openBuyInventory(player);
        playerManager.listenForBuyRemoval(player, merchant);
    }

    @Subcommand("removeSellItem|rsi")
    @Syntax("<merchantType> &e- Remove an item from a merchant's sell inventory")
    @CommandPermission(Permissions.MODIFY_ITEMS)
    public void removeSellItem(Player player, String merchantType) {
        Merchant merchant = merchantManager.getMerchant(merchantType);

        if (merchant == null) {
            player.sendMessage(ChatColor.RED + "Merchant type not found!");
            return;
        }

        playerManager.listenForSellRemoval(player, merchant);
        merchant.openSellInventory(player);
    }

    @Subcommand("merchants|listMerchants|list|lm")
    @Description("List merchant types")
    @CommandPermission(Permissions.LIST_MERCHANT_TYPES)
    public void listMerchantTypes(CommandSender sender) {
        Collection<String> types = merchantManager.getMerchantTypes();
        StringBuilder typesString = new StringBuilder(ChatColor.GRAY + "Merchant Types:\n");
        typesString.append(ChatColor.YELLOW);

        types.forEach(type -> typesString.append(type).append("\n"));
        String message = typesString.toString().trim();
        sender.sendMessage(message);
    }

    private boolean anyLessThanZero(double ... doubles) {
        for (double d : doubles) {
            if (d < 0) {
                return true;
            }
        }

        return false;
    }

    private String getValuesAsMessage(String prefix, Enum[] e) {
        StringBuilder stringBuilder = new StringBuilder(prefix);

        for (int i = 0; i < e.length; i++) {
            stringBuilder.append(e[i].name());

            if (i != e.length - 1)
                stringBuilder.append(", ");
        }

        return stringBuilder.toString();
    }

    private boolean handleNumberChecksFails(Player player, double price, double priceChange, double moneyOrSupply, double minPrice, double maxPrice) {
        if (price < minPrice) {
            player.sendMessage(ChatColor.RED + "Price cannot be lower than the minimum price!");
            return true;
        }

        if (price > maxPrice) {
            player.sendMessage(ChatColor.RED + "Price cannot be higher than the maximum price!");
            return true;
        }

        if (anyLessThanZero(price, priceChange, moneyOrSupply, minPrice, maxPrice)) {
            player.sendMessage(ChatColor.RED + "Money amounts and/or supply cannot be less than 0!");
            return true;
        }

        return false;
    }
}
