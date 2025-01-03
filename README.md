# Telegram Media Extractor Bot

## Overview

Telegram Media Extractor Bot is a Scala 3 project designed to download media from external platforms (currently Twitter) and post it to a specified Telegram group. The bot uses Selenium for downloading media files and interacts with Telegram's API to upload and share these files.

While the codebase currently contains some code smells, the focus has been on ensuring functionality, with plans to refine and improve the implementation in the future.

## Usage

1. Send a media URL to the bot via a Telegram message (e.g., `https://x.com/alexnivak/status/1871251416050020525`).
2. The bot validates the user ID against the `TELEGRAM_VALID_USERS` list.
3. If valid, the bot uses Selenium to download the media. Supported scenarios include:
    - JPEG images
    - MP4 files (GIFs from Twitter are natively stored as MP4 and posted as-is to Telegram).
4. The downloaded media is uploaded to the Telegram group specified by `TELEGRAM_TARGET_CHAT_ID`.

### Current Features:

- **Supported Media Types:**
    - JPEG images
    - GIFs (stored as MP4 in both Twitter and Telegram)
- **Platform Integration:** Twitter (media URLs are provided to the bot)

__Please check tests to understand what type of URLs work at current moment__

### Planned Improvements:

- Support for video downloads

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

### Installation:

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd <repository-directory>
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
    - Currently supports JPEG images and MP4 files (originating as GIFs from Twitter).
    - Videos are not yet handled.
- **Dependency on Twitter URLs:** Only works with Twitter media links at the moment.

## Contributing:

1. Fork the repository.
2. Create a feature branch.
3. Submit a pull request with a detailed description of your changes.

---

For issues or feature requests, please create a ticket in the issue tracker.
