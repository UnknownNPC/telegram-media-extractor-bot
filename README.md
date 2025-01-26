# Telegram Media Extractor Bot

## Overview

Telegram Media Extractor Bot is a Scala 3 application that downloads media from Telegram messages and posts it to integrations like Telegram, Twitter, and Mastodon. The bot supports non-complex Twitter posts and experimental Instagram posts with videos, images, and Reels. Direct links to JPEG and MP4 files are also supported. Instagram and Twitter may require authorization via `WEB_CLIENT_COOKIES` for private or restricted posts.

## Features

### Supported Media Types
- JPEG images
- GIFs (stored and downloaded as MP4)
- Videos (MP4)

### Integrations
- **Telegram**: Posts media to a group or chat using `TELEGRAM_TARGET_BOT_API_KEY` and `TELEGRAM_TARGET_CHAT_ID`.
- **Twitter**: Posts media using `TWITTER_API_KEY`, `TWITTER_API_SECRET`, `TWITTER_ACCESS_TOKEN`, and `TWITTER_ACCESS_TOKEN_SECRET`.
- **Mastodon**: Posts media using `MASTODON_INSTANCE_NAME` and `MASTODON_ACCESS_TOKEN`.

### Usage
1. Send a media URL to the bot via Telegram (e.g., `https://x.com/example/status/1234567890`).
2. The bot validates the user ID against `TELEGRAM_VALID_USERS`.
3. If valid, the bot uses Selenium to download and upload media to the configured platforms.

### Planned Improvements
- Enhanced edge-case handling for Twitter media
- Instagram integration refinement

## Setup

### Prerequisites
- **Scala 3** installed
- **Selenium WebDriver** configured
- **API Keys** for Telegram, Twitter, and Mastodon

### Environment Variables
Configure the following variables:

| Variable Name                 | Description                                        |
|-------------------------------|----------------------------------------------------|
| `TELEGRAM_BOT_API_KEY`        | API key for the Telegram bot                      |
| `TELEGRAM_VALID_USERS`        | Comma-separated list of allowed user IDs          |
| `TELEGRAM_TARGET_BOT_API_KEY` | *(Optional)* API key for posting media to a Telegram group     |
| `TELEGRAM_TARGET_CHAT_ID`     | *(Optional)* Target Telegram group chat ID                    |
| `WEB_CLIENT_COOKIES`          | *(Optional)* Cookies for authentication (e.g., Instagram, private Twitter posts)     |
| `TWITTER_API_KEY`             | *(Optional)* Twitter API key                                   |
| `TWITTER_API_SECRET`          | *(Optional)* Twitter API secret                                |
| `TWITTER_ACCESS_TOKEN`        | *(Optional)* Twitter access token                              |
| `TWITTER_ACCESS_TOKEN_SECRET` | *(Optional)* Twitter access token secret                       |
| `MASTODON_INSTANCE_NAME`      | *(Optional)* Base URL of the Mastodon instance                |
| `MASTODON_ACCESS_TOKEN`       | *(Optional)* Mastodon access token                             |

### Installation
1. Clone the repository:
   ```bash
   git clone git@github.com:UnknownNPC/telegram-media-extractor-bot.git
   cd telegram-media-extractor-bot
   ```
2. Configure environment variables in your shell or `.env` file.
3. Build the project:
   ```bash
   sbt compile
   ```

### Running the Bot
Run the bot:
```bash
sbt run
```

### Docker Setup
1. Create a `.env` file with environment variables.
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
   MASTODON_INSTANCE_NAME=mastodon.example.com
   MASTODON_ACCESS_TOKEN=your_mastodon_access_token
   ```
2. Run the bot:
   ```bash
   docker-compose --env-file .env up
   ```
3. To run in detached mode:
   ```bash
   docker-compose --env-file .env up -d
   ```
4. Check logs:
   ```bash
   docker-compose logs telegram-media-extractor-bot
   ```
5. Stop the bot:
   ```bash
   docker-compose down
   ```

## Contributing
1. Fork the repository.
2. Create a feature branch.
3. Submit a pull request with a detailed description of changes.

For issues or feature requests, use the issue tracker.

