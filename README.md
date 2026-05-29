# Marqet — AI Business News App
## Setup Instructions
1. Get necessary API keys from Supabase, News API, and Gemini.
2. Create \`secrets.properties\` file in the root directory and add the keys:
   \`\`\`properties
   SUPABASE_URL=your_supabase_project_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   NEWS_API_KEY=your_news_api_key
   \`\`\`
   And make sure you have \`.env\` file for \`GEMINI_API_KEY\`.
3. Open the project in Android Studio or run Gradle from the command line \`./gradlew build\`.

## Features List
- **Personalized News:** Top stories and breaking news from the business, technology, and financial sectors.
- **Categorization:** Discover and read articles based on multiple categories like Tech, AI, Market, Startups, and more.
- **Saved Articles:** Keep a list of your most important business updates to read offline or refer to later.
- **Watched Stories:** Track trending topics or important ongoing business events to stay in loop.
- **AI Integration:** Get intelligent summaries of articles, making it easier to consume complex data points faster.
- **User Authentication:** Safe and secure login powered by Supabase Auth and PostgreSQL.

## Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Networking:** Retrofit & Ktor
- **Concurrency:** Kotlin Coroutines & Flow
- **Dependency Injection:** ViewModel with Factory
- **Authentication & Database:** Supabase (Auth, Postgres Row Level Security)
- **Local Persistence (Optional):** Room Database
- **AI summarization:** Google Gemini AI
