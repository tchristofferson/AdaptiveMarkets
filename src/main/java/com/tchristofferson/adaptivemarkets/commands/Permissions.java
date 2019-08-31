package com.tchristofferson.adaptivemarkets.commands;

class Permissions {

    private static final String BEG_PERMISSION = "adaptivemarkets.";
    static final String SPAWN_MERCHANT = BEG_PERMISSION + "spawnmerchant";
    static final String CREATE_MERCHANT = BEG_PERMISSION + "createmerchant";
    static final String DELETE_MERCHANT = BEG_PERMISSION + "deletemerchant";
    static final String MODIFY_ITEMS = BEG_PERMISSION + "modifyitems";
    static final String LIST_MERCHANT_TYPES = BEG_PERMISSION + "listmerchanttypes";
    static final String USE_BUY_INV = BEG_PERMISSION + "use.buyinventory";
    static final String USE_SELL_INV = BEG_PERMISSION + "use.sellinventory";

}
