# Telegram Media Extractor Bot

## Overview

Telegram Media Extractor Bot is a Scala 3 project designed to download media and post it to specified integrations such as Telegram, Twitter, and Mastodon.

Instagram is currently under development, but it can likely download images if valid `WEB_CLIENT_COOKIES` are provided.

The bot retrieves media URLs from Telegram messages and downloads the associated media files, which can then be posted to other platforms based on the configured integrations.

## Usage

1. Send a media URL to the bot via a Telegram message (e.g., `https://x.com/alexnivak/status/1871251416050020525`).
2. The bot validates the user ID against the `TELEGRAM_VALID_USERS` list.
3. If valid, the bot uses Selenium to download the media. Supported scenarios include:
    - JPEG images
    - MP4 files (GIFs from Twitter are natively stored as MP4 and downloaded in this format)
    - Videos (including MP4 format)
    - Direct links to images or MP4 files
4. The downloaded media is uploaded to the Telegram group, sent back to Twitter, or posted on Mastodon based on the configured integrations.

### Current Features

#### Supported Media Types

- JPEG images
- GIFs (stored as MP4 in both Twitter and Telegram)
- Videos (MP4)

#### Platform Integration

**Integrations (Where the bot sends media):**

- **Telegram**: Posts downloaded media to a group or chat. Requires `TELEGRAM_TARGET_BOT_API_KEY` and `TELEGRAM_TARGET_CHAT_ID` to be specified.
- **Twitter**: Posts downloaded media back to Twitter. Requires `TWITTER_API_KEY`, `TWITTER_API_SECRET`, `TWITTER_ACCESS_TOKEN`, and `TWITTER_ACCESS_TOKEN_SECRET` to be specified.
- **Mastodon**: Posts downloaded media to a Mastodon instance. Requires `MASTODON_BASE_URL` and `MASTODON_ACCESS_TOKEN` to be specified.

**External Platform (Where the bot retrieves media):**

- **Telegram**: The bot only accepts media URLs sent to it via Telegram messages.

> **Note**: Links to the middle of a thread or complex URLs may fail. For higher reliability, use direct links to single posts with media or direct media file URLs.

### Planned Improvements

- Improving edge-case handling for Twitter media
- Developing stable support for downloading media from Instagram
- Enhancing Mastodon integration with additional features

## Getting Started

### Prerequisites

1. **Scala 3**: Ensure you have Scala 3 installed on your machine.
2. **Selenium WebDriver**: Install and configure the necessary Selenium drivers for your browser.
3. **Telegram Bot API Key** (Optional): Obtain an API key by creating a bot through [BotFather](https://core.telegram.org/bots#botfather).
4. **Twitter API Keys** (Optional): Obtain API keys on [Twitter Dev Platform](https://developer.x.com/en).
5. **Mastodon Instance URL and Access Token** (Optional): Obtain API access details from your Mastodon instance.

### Environment Variables

To run the bot, the following environment variables must be set:

| Variable Name                 | Description                                                                                        |
|-------------------------------|----------------------------------------------------------------------------------------------------|
| `TELEGRAM_BOT_API_KEY`        | The API key for your Telegram bot to receive messages.                                             |
| `TELEGRAM_VALID_USERS`        | A comma-separated list of user IDs allowed to interact with the bot.                               |
| `TELEGRAM_TARGET_BOT_API_KEY` | *(Optional)* The API key for your Telegram bot to post media to the target group.                  |
| `TELEGRAM_TARGET_CHAT_ID`     | *(Optional)* The chat ID of the Telegram group where media will be posted.                         |
| `WEB_CLIENT_COOKIES`          | *(Optional)* Cookies in the format `key:value;key1:value1`, used for passing authorization tokens. |
| `TWITTER_API_KEY`             | *(Optional)* The API key for accessing Twitter's API.                                              |
| `TWITTER_API_SECRET`          | *(Optional)* The API secret key for accessing Twitter's API.                                       |
| `TWITTER_ACCESS_TOKEN`        | *(Optional)* The access token for accessing Twitter's API.                                         |
| `TWITTER_ACCESS_TOKEN_SECRET` | *(Optional)* The access token secret for accessing Twitter's API.                                  |
| `MASTODON_BASE_URL`           | *(Optional)* The base URL of your Mastodon instance.                                               |
| `MASTODON_ACCESS_TOKEN`       | *(Optional)* The access token for accessing the Mastodon API.                                      |

### Installation

1. Clone the repository:
   ```bash
   git clone git@github.com:UnknownNPC/telegram-media-extractor-bot.git
   cd telegram-media-extractor-bot
2. Set the required environment variables in your shell or `.env` file.
3. Build the project using your preferred Scala build tool (e.g., sbt):
   ```bash
   sbt compile
   ```

### Running the Bot:

Run the bot with the following command:

```bash
sbt run
```

### Running the Bot with Docker:

1. Create a `.env` file in the root directory of the project and add the required environment variables:

   ```env
   TELEGRAM_BOT_API_KEY=your_bot_api_key
   TELEGRAM_VALID_USERS=user1,user2
   TELEGRAM_TARGET_BOT_API_KEY=your_target_bot_api_key
   TELEGRAM_TARGET_CHAT_ID=your_chat_id
   WEB_CLIENT_COOKIES="key:value;key1:value1"
   TWITTER_API_KEY=your_twitter_api_key
   TWITTER_API_SECRET=your_twitter_api_secret
   TWITTER_ACCESS_TOKEN=your_twitter_access_token
   TWITTER_ACCESS_TOKEN_SECRET=your_twitter_access_token_secret
   MASTODON_BASE_URL=https://mastodon.example.com
   MASTODON_ACCESS_TOKEN=your_mastodon_access_token
   ```

2. Start the bot using Docker Compose:

   ```bash
   docker-compose --env-file .env up
   ```

3. To run the bot in detached mode:

   ```bash
   docker-compose --env-file .env up -d
   ```

4. Check the logs for the running container:

   ```bash
   docker-compose logs telegram-media-extractor-bot
   ```

5. Stop the bot:

   ```bash
   docker-compose down
   ```

## Contributing:

1. Fork the repository.
2. Create a feature branch.
3. Submit a pull request with a detailed description of your changes.

---

For issues or feature requests, please create a ticket in the issue tracker.

---
