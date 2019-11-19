package com.telegram.bot.csgo.messages;

public enum CallBackData {
    
    TOP_10 ("top10"),
    TOP_20 ("top20"),
    TOP_30 ("top30"),
    TOP_10_PLAYERS ("top10players"),
    TOP_20_PLAYERS ("top20players"),
    TOP_30_PLAYERS ("top30players"),
    MATCHES ("matches"),
    RESULTS ("results"),
    STREAMS ("streams");
    
    private String name;

    private CallBackData(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

}
