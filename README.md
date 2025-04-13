# STACS Project - Finance-Pepjekt

The Finance Project focuses on creating a platform for managing financial assets with the following key functionalities:

1. **Account Management**: Users can deposit stocks into their accounts by specifying the desired amount.
2. **Portfolio Management**: Users can use deposited stocks to purchase them, which are stored in their portfolio.
3. **Trading Stocks**: Users can execute buy or sell orders for stocks, with options for immediate or scheduled 
   execution based on real-time market prices.
4. **Portfolio Overview**: A dashboard displays purchased stocks with details
5. **Search for Stocks**: Users can search for stocks

# Screenshots
![Depotübersicht](/documentation/screenshots/depot.png)

![Assets](/documentation/screenshots/assets.png)

# How to Execute

## 1. Prepare Environment
1. **Install Docker**
    - Install Docker and start it on your local machine.

2. **Download Finnhub-Recreate (Dummy Finnhub)**
    - Repository URL: https://gitlab.cs.hs-rm.de/dthom001/finnhub-recreate

3. **Start Finnhub-Recreate**
    - Run the following command:
      ```bash
      ./gradlew bootRun
      ```

## 2. Install and Run Backend
1. **Create .env file**
    - Create in root an .env file
    - Insert these properties:
   
      DB_NAME=mydb
      DB_USERNAME=my_user
      DB_PASSWORD=my_password
      FINNHUB_TOKEN=ct2r2bhr01qiurr42bq0ct2r2bhr01qiurr42bqg

2. **Start Backend**
    - Use the following command:
      ```bash
      ./gradlew bootRun
      ```


## 3. Start Frontend
1. **Navigate to the Frontend Directory**
    - Change to the `frontend` directory:
      ```bash
      cd /frontend
      ```

2. **Install Packages**
    - Install required packages using:
      ```bash
      npm i
      ```

3. **Run Frontend**
    - Start the frontend with:
      ```bash
      npm run dev
      ```
