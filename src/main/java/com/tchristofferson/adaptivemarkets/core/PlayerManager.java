package com.tchristofferson.adaptivemarkets.core;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    final Map<UUID, Merchant> listeningForBuyRemoval = new HashMap<>();
    final Map<UUID, Merchant> listeningForSellRemoval = new HashMap<>();

    public void listenForBuyRemoval(Player player, Merchant merchant) {
        listeningForBuyRemoval.put(player.getUniqueId(), merchant);
    }

    public void listenForSellRemoval(Player player, Merchant merchant) {
        listeningForSellRemoval.put(player.getUniqueId(), merchant);
    }

}
