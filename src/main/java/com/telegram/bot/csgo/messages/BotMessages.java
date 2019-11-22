package com.telegram.bot.csgo.messages;

import java.util.ArrayList;

public class BotMessages {

	private static ArrayList<String> stickers = new ArrayList<>();
	private static ArrayList<String> lastSticker = new ArrayList<>();
	
    public static ArrayList<String> getStickers() {
        return stickers;
    }

    public static void setStickers(ArrayList<String> stickers) {
        BotMessages.stickers = stickers;
    }

    public static ArrayList<String> getLastSticker() {
        return lastSticker;
    }

    public static void setLastSticker(ArrayList<String> lastSticker) {
        BotMessages.lastSticker = lastSticker;
    }

    static {	
		stickers.add("CAADAgADIQAD9mOfG2LGtCrsw7bFFgQ");
		stickers.add("CAADAgADHgAD9mOfG7X25hWYzpI1FgQ");
		stickers.add("CAADAgADHwAD9mOfG_Ba2iIqOnazFgQ");
		stickers.add("CAADAgADIgAD9mOfG4hfcToK4DCYFgQ");
		stickers.add("CAADAgADJQAD9mOfG963ItgypxoIFgQ");
		stickers.add("CAADAgADKgAD9mOfG7fCrBPbLEDJFgQ");
		stickers.add("CAADAgADFAAD9mOfGxFXaqquJHwYFgQ");
		stickers.add("CAADAgADEQAD9mOfG94SbA2pBiwnFgQ");
		stickers.add("CAADAgADCwAD9mOfG8RskvZsrlZsFgQ");
		stickers.add("CAADAgADDQAD9mOfGxyG9FhomVn0FgQ");
		stickers.add("CAADAgADBwAD9mOfGwvUQUWU0Bv_FgQ");
		stickers.add("CAADAgADBgAD9mOfG-4M62fmXafEFgQ");
		stickers.add("CAADAgADAwAD9mOfGzeICv_hr6IOFgQ");
		stickers.add("CAADAgADCgAD9mOfG91GVm2tjQaEFgQ");
		stickers.add("CAADAgADJAAD9mOfGw-taxRFVDWeFgQ");
		stickers.add("CAADAgADKAAD9mOfG-AyIRVUq8l0FgQ");
		stickers.add("CAADAgADJgAD9mOfG-rYuchCMU8-FgQ");
		stickers.add("CAADAgADHAAD9mOfG8QGYzOYAXv9FgQ");
		stickers.add("CAADAgADFgAD9mOfG7tdoHpun4KJFgQ");
		stickers.add("CAADAgADIwAD9mOfG7uEav_8NSjTFgQ");
	}

}
