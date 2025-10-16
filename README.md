# Smart Finance Tracker (Java Swing + SQLite)

Run
- mvn clean package
- java -jar target/smart-finance-tracker-1.0.0-jar-with-dependencies.jar

Preview demo
- Popup “Database connected (SQLite)” proves DB
- Tabs: Dashboard, Transactions, Budgets, Rules, Chat
- Add a transaction, then check finance.db with a SQLite viewer

Notes
- Database file: finance.db (auto-created next to the JAR)
- Default categories seeded on first run
- Auto-categorization: rules first, then Naive Bayes fallback trained from labeled expenses
