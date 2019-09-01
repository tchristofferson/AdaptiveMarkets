package com.tchristofferson.adaptivemarkets.core;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final Map<UUID, Merchant> listeningForBuyRemoval = new HashMap<>();
    private final Map<UUID, Merchant> listeningForSellRemoval = new HashMap<>();
    private final Map<UUID, Merchant> currentMerchant = new HashMap<>();

    public void listenForBuyRemoval(Player player, Merchant merchant) {
        listeningForBuyRemoval.put(player.getUniqueId(), merchant);
    }

    public Merchant getListeningForBuyRemoval(Player player) {
        return listeningForBuyRemoval.get(player.getUniqueId());
    }

    public Merchant removeListeningForBuyRemoval(Player player) {
        return listeningForBuyRemoval.remove(player.getUniqueId());
    }

    public void listenForSellRemoval(Player player, Merchant merchant) {
        listeningForSellRemoval.put(player.getUniqueId(), merchant);
    }

    public Merchant getListeningForSellRemoval(Player player) {
        return listeningForSellRemoval.get(player.getUniqueId());
    }

    public Merchant removeListeningForSellRemoval(Player player) {
        return listeningForSellRemoval.remove(player.getUniqueId());
    }

    public void setCurrentMerchant(Player player, Merchant merchant) {
        currentMerchant.put(player.getUniqueId(), merchant);
    }

    public Merchant removeCurrentMerchant(Player player) {
        return currentMerchant.remove(player.getUniqueId());
    }

    public Merchant getCurrentMerchant(Player player) {
        return currentMerchant.get(player.getUniqueId());
    }

}
