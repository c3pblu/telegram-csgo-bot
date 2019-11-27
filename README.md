## telegram-csgo-bot
Get data from [HLTV.org](https://HLTV.org/) about CS:GO events, teams, players etc. 

### What Is Done:
- Top Teams, Players, Matches, Results messages (Appache Http, Jsoup)
- Menu (Telegram Bot API)
- Streams from Twitch.tv (Appache Http, [Twitch API](https://dev.twitch.tv/docs/v5), Json)
- Favorite teams and Flags control for each chat (Hibernate with MySQL) 
- Scheduled messages for Results and Matches (cron Scheduler, configured value)
- Randomize private bot response, w/o repeating messages (configured value)
- Timeout for text and menu messages (configured values)
- MBean for refresh data from DB w/o application restart

### Backlog:

- Flags for Players
- Cache for "Top" requests
