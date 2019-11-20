package com.telegram.bot.csgo.messages;

public enum Emoji {
    
    INFO ("\u2139"),
    HEAVY_CHECK_MARK ("\u2714\ufe0f"),
    EXCL_MARK ("\u2757"),
    VS ("\uD83C\uDD9A"),
    SQUARE ("\u25AB"),
    STAR ("\u2b50\ufe0f"),
    CUP ("\uD83C\uDFC6"),
    MIL_MEDAL ("\ud83c\udf96"),
    SPORT_MEDAL ("\ud83c\udfc5"),
    TV ("\ud83d\udcfa"),
    FIRE ("\uD83D\uDD25"),
    RU ("\ud83c\uddf7\ud83c\uddfa"),
    EN ("\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc65\udb40\udc6e\udb40\udc67\udb40\udc7f");
    
    private String code;

    private Emoji(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
