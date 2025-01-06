# Telegram Media Extractor Bot

## Overview

Telegram Media Extractor Bot is a Scala 3 project designed to download media from external platforms (currently Twitter) and post it to a specified Telegram group. The bot uses Selenium for downloading media files and interacts with Telegram's API to upload and share these files.

__This project does not aim to provide stability or production-grade reliability. It is primarily intended as a convenience tool for reposting media.__

## Usage

1. Send a media URL to the bot via a Telegram message (e.g., `https://x.com/alexnivak/status/1871251416050020525`).
2. The bot validates the user ID against the `TELEGRAM_VALID_USERS` list.
3. If valid, the bot uses Selenium to download the media. Supported scenarios include:
    - JPEG images
    - MP4 files (GIFs from Twitter are natively stored as MP4 and posted as-is to Telegram).
    - Videos (including MP4 format)
4. The downloaded media is uploaded to the Telegram group specified by `TELEGRAM_TARGET_CHAT_ID`.

### Current Features:

- **Supported Media Types:**
    - JPEG images
    - GIFs (stored as MP4 in both Twitter and Telegram)
    - Videos (MP4)
- **Platform Integration:** Twitter (media URLs are provided to the bot)

__The bot may also work with videos and images from Instagram, but this functionality is experimental and requires further testing.__  
__Important: Links to the middle of a thread or complex URLs may fail. For higher reliability, use direct links to single posts with media.__  
__Please check tests to understand what type of URLs work at the current moment. Testing has primarily been conducted using Twitter media links.__

### Planned Improvements:

- Expanding platform support beyond Twitter and Instagram

## Getting Started

### Prerequisites:

1. **Scala 3**: Ensure you have Scala 3 installed on your machine.
2. **Selenium WebDriver**: Install and configure the necessary Selenium drivers for your browser.
3. **Telegram Bot API Key**: Obtain an API key by creating a bot through [BotFather](https://core.telegram.org/bots#botfather).

### Environment Variables:

To run the bot, the following environment variables must be set:

| Variable Name             | Description                                                                                 |
| ------------------------- | ------------------------------------------------------------------------------------------- |
| `TELEGRAM_BOT_API_KEY`    | The API key for your Telegram bot.                                                          |
| `TELEGRAM_VALID_USERS`    | A comma-separated list of user IDs allowed to interact with the bot.                        |
| `TELEGRAM_TARGET_CHAT_ID` | The chat ID of the Telegram group where media will be posted.                               |
| `WEB_CLIENT_COOKIES`      | Optional cookies in the format `key:value;key1:value1`, used for passing authorization tokens. |

### Installation:

1. Clone the repository:
   ```bash
   git clone git@github.com:UnknownNPC/telegram-media-extractor-bot.git
   cd telegram-media-extractor-bot
   ```
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
   TELEGRAM_TARGET_CHAT_ID=your_chat_id
   WEB_CLIENT_COOKIES="key:value;key1:value1"
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

## Limitations:

- **Media Support:**
    - Currently supports JPEG images, MP4 files (originating as GIFs from Twitter), and videos.
    - Experimental support for Instagram media (videos and images) — functionality may be unstable and requires testing.
- **URL Complexity:** Links to threads or specific parts of conversations may fail. Use simple URLs pointing to single posts for better results.

## Contributing:

1. Fork the repository.
2. Create a feature branch.
3. Submit a pull request with a detailed description of your changes.

---

For issues or feature requests, please create a ticket in the issue tracker.

---