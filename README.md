## Telegram CS:GO Bot
Get information about CS:GO events, teams, players, streams etc.

### Environment variables
To run locally please have a look at next mandatory variables:

- `DB_URL` - JDBC URL
- `DB_USER` - DB username
- `DB_PASS` - DB password
- `BOT_NAME` - Telegram Bot username
- `BOT_TOKEN` - Telegram Bot token
- `TWITCH_CLIENT_ID` - Twitch client ID (to get list of streams)
- `TWITCH_CLIENT_SECRET` - Twitch client secret (to get list of streams)

Optional:

- `BOT_CALLBACK_TIMEOUT` - Timeout (in seconds) after each callback will be ignored
- `BOT_MESSAGE_TIMEOUT` - Timeout (in seconds) after each message will be ignored
- `BOT_MESSAGE_UNIQ_COUNT` - Count of not repeated stickers bot will send
- `BOT_SCHEDULER_CHAT_ID` - Chat ID for which scheduled messages will be sent
- `BOT_SCHEDULER_MATCHES_CRON` - Cron expression for 'matches' scheduled message
- `BOT_SCHEDULER_RESULTS_CRON` - Cron expression for 'results' scheduled message
- `ROOT_LOGGING_LEVEL` - Application root logging level
- `BOT_LOGGING_LEVEL` - Application logging level
- `DB_DEBUG_SQL` - If true will show SQL in logs

### You can find running bot in the Telegram
https://t.me/k0ntraBot