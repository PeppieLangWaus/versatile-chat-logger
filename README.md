# Versatile Logger

A highly configurable chat logger for RuneLite. Log public chat, friends chat, clan chat, private
messages, game messages, collection log unlocks, valuable drops, pet drops, level-ups, and Group
Ironman group chat — each independently — to local files, a remote HTTP endpoint (such as a
Discord webhook), or both.

Every category has its own on/off switch for local logging and remote logging, its own optional
remote URL override, and its own choice of which fields (timestamp, icons, channel/clan name) get
included in the logged line. Nothing is one-size-fits-all: log clan chat quietly to disk while
pinging a Discord channel only for pet drops, or vice versa.

## Features

- **11 independently configurable categories** — public chat, friends chat, clan chat, guest clan
  chat, Group Ironman group chat, private messages, game messages, collection log unlocks, high
  value (valuable) drops, pet drops, and level-ups.
- **Per-category local and remote toggles** — nothing is forced into one bucket. Turn local
  logging on for everything and remote logging on for just the categories you care about, or any
  combination.
- **Per-category remote URL overrides** — send pet drops to one Discord channel and clan chat to a
  different one, all from the same plugin.
- **Two remote formats** — a Discord-webhook-compatible plain-text mode, or a full structured JSON
  mode with message, sender, ironman, clan rank, and channel context.
- **Configurable field inclusion** — choose whether timestamps, icons, and channel/clan names are
  included per category, independently of the raw message content.
- **Per-account local logs** — each RuneScape account gets its own folder, with a choice between
  one interleaved log file per session or a separate file per category.
- **Automatic log retention** — old local log files are cleaned up automatically after a
  configurable number of days.
- **Rate-limit-safe remote sending** — outbound requests to each destination are queued and
  throttled, so heavy chat activity won't hammer a webhook or a self-hosted receiver.

## Chat & message categories

| Category | Local (default) | Remote (default) |
|---|---|---|
| Public chat | On | Off |
| Channel chat (friends chat) | On | On |
| Clan chat | On | On |
| Game messages | Off | Off |
| Private messages | On | Off |
| Guest clan chat | On | Off |
| Group chat (Group Ironman) | On | Off |
| Collection log unlock | On | Off |
| High value drop | On | Off |
| Pet drops | On | Off |
| Level up | On | Off |

A few notes on how categories are detected and routed:

- **Collection log unlocks**, **high value drops**, and **pet drops** are all delivered by the
  game as generic game messages — the plugin recognizes them by their message text (the same
  patterns RuneLite's own core plugins use) and routes them to their own category instead of
  lumping them in with "Game messages". A message is only ever counted under its most specific
  category, never double-logged.
- **Level up** messages have their own distinct message type in the game client, so they're
  detected reliably without any text matching.
- **Join/leave and system notification lines** for friends chat, clan chat, and guest clan chat
  (e.g. "Attempting to join chat-channel...") are routed to **Game messages**, keeping the chat
  categories themselves limited to actual player-typed messages.
- **Group chat** covers all Group Ironman group communication and group-forming notifications.

## Local logging

Local logs are written under RuneLite's own data directory:

```
.runelite/versatile-chat-logger/<account name>/
```

Each RuneScape account gets its own subfolder (keyed by your current display name — renaming your
account will start a new folder). Within an account's folder, you can choose between two layouts
via the **"Split local logs per category"** setting:

**Single file** (default) — one interleaved file per session, covering every locally-enabled category:

```
versatile-chat-logger/
└── YourName/
    ├── 2026-08-09_18-04-02.txt
    └── 2026-08-10_09-12-47.txt
```

**Per-category** — one file per category per session:

```
versatile-chat-logger/
└── YourName/
    ├── public-chat/
    │   └── 2026-08-09_18-04-02-public-chat_log.txt
    ├── channel-chat/
    │   └── 2026-08-09_18-04-02-channel-chat_log.txt
    ├── clan-chat/
    │   └── 2026-08-09_18-04-02-clan-chat_log.txt
    └── ...
```


A "session" runs from login to logout — the same file(s) are appended to for the whole time
you're logged in, and a fresh one is started the next time you log in. A category with local
logging turned off simply never gets a file or folder created for it, in either layout.

Old logs are deleted automatically based on the **"Log retention (days)"** setting (default 30
days, set to 0 to keep logs forever). Cleanup runs shortly after the plugin starts and once a day
after that, and only ever touches files inside the plugin's own folder.

## Remote logging

Remote logging is controlled by a global master switch (**Remote logging**, off by default) under
**Remote Settings**, plus a **Remote URL** and an optional **Authorization token**. Each category
also has its own **Remote logging** toggle and an optional URL override — a category's toggle only
does anything once the global switch is also on, so nothing is sent anywhere until you opt in
globally first.

If an authorization token is set, every remote request carries it as:

```
Authorization: Bearer <your token>
```

If no token is set, no `Authorization` header is sent at all.

### Formatting modes

**In-game message** (default) — sends a plain-text line formatted the same way it would appear in
your chatbox, respecting that category's field-inclusion settings. The request body is
`{"content": "<line>"}`, which is exactly what a Discord webhook URL expects — paste one in as
your Remote URL and it works with no other setup.

```json
{"content": "[18:04:12] [Clan Name] SomePlayer: gz on the pet!"}
```

**Full** — sends a structured JSON object with everything the plugin knows about the message,
intended for a self-hosted receiver rather than Discord directly:

```json
{
  "message": {
    "id": 48,
    "timestamp": 1786310735,
    "type": "FRIENDSCHAT",
    "text": "gz on the pet!"
  },
  "user": {
    "name": "SomePlayer",
    "type": 0,
    "clanRank": { "rank": 126, "title": "Owner" },
    "friendsChatRank": "OWNER"
  },
  "clanChat": { "name": "Some Clan" },
  "friendsChat": { "name": "Ardy Hosts", "owner": "Ardy Hosts" }
}
```

- `user.type` is the player's ironman status (`0` normal, `1` ironman, `2` ultimate ironman,
  `3` hardcore ironman, `4`–`6` various Group Ironman states).
- `user.clanRank` and `user.friendsChatRank` describe the **sender's** standing and are included
  whenever resolvable, regardless of which channel the message came through.
- `clanChat` is only present for clan chat and guest clan chat messages; `friendsChat` only for
  friends chat messages. Neither appears for message types they don't apply to.

Outbound remote requests are queued per destination URL and sent at a steady, rate-limit-friendly
pace rather than one-at-a-time as messages arrive, so a burst of chat activity won't overwhelm a
webhook or flood a self-hosted endpoint.

## Privacy

Categories like public chat, clan chat, friends chat, and guest clan chat necessarily include
other players' names and messages, not just your own. Remote logging is opt-in and off by default
for this reason — only enable it, and only point it at destinations, that you trust. Local logs
stay entirely on your own machine under `.runelite/versatile-chat-logger/`.

## Configuration reference

- **Log retention (days)** — how long local logs are kept before automatic deletion (default 30).
- **Split local logs per category** — per-category files vs. one interleaved file per session.
- **Remote Settings** (expanded by default)
  - Remote logging (global master switch, off by default)
  - Formatting mode — In-game message / Full
  - Remote URL
  - Authorization token
- **Per category** (collapsed by default, one section per category)
  - Local logging
  - Remote logging
  - Override remote URL
  - Remote URL (used only when the override above is on)
  - Include — which of timestamp / icons / channel-or-clan-name are included in that category's
    logged lines
