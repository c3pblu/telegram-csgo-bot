package com.telegram.bot.csgo.model;

public enum Commands {
    
    HELP (".хелп"),
    MENU (".меню"),
    MATCHES (".матчи"),
    RESULTS (".результаты"),
    TOP_10 (".топ10"),
    TOP_20 (".топ20"),
    TOP_30 (".топ30"),
    TOP_10_PLAYERS (".топ10игроков"),
    TOP_20_PLAYERS (".топ20игроков"),
    TOP_30_PLAYERS (".топ30игроков"),
    STREAMS (".стримы"),
    TEAMS (".команды"),
	STOP (".стоп");
    
    private String name;

    private Commands(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
