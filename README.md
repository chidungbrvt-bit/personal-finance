# Evergreen Finance

Evergreen Finance is a personal finance management application that helps users track accounts, income, expenses, transactions, and monthly budgets from one dashboard.

## Live Application

- Frontend: https://personal-finance-blond-seven.vercel.app
- Backend API: https://personal-finance-backend-production-51f2.up.railway.app

> All monetary values are currently managed in Vietnamese Dong (VND).

## Features

### Authentication

- Register with a name, email, and password.
- Each email address must be unique.
- Sign in using email and password.
- JWT-based authentication.
- Role-based authorization.
- Each user can access only their own financial data.

### Password Recovery

The system generates a personal recovery code when a user registers.

- Each recovery code is generated securely and randomly.
- The original code is displayed only once after registration.
- Only a BCrypt hash is stored in the database.
- The recovery code is not saved in localStorage.
- Resetting a password requires the correct email and recovery code.

> Store your recovery code in a password manager. If you lose both your password and recovery code, your account cannot be recovered automatically.

### Financial Accounts

Users can create and manage multiple financial accounts:

- Cash
- Bank accounts
- Electronic wallets

Each account contains:

- Account name
- Account type
- Initial balance
- Active status
- VND currency

### Categories

Users can create custom categories for:

- Income
- Expenses

Examples:

- Salary
- Bonus
- Food
- Transportation
- Shopping
- Rent

Expense categories can also be used to create monthly budgets.

### Transactions

Users can:

- Record income
- Record expenses
- Edit transactions
- Delete transactions
- Filter by account
- Filter by category
- Filter by transaction type
- Filter by date or period

Each transaction contains:

- Transaction type
- Amount
- Date
- Account
- Category
- Optional description

### Monthly Budgets

Users can create budgets based on:

- Expense category
- Month
- Year

The application displays:

- Budget amount
- Amount spent
- Remaining amount
- Percentage used
- Over-budget status

### Dashboard

The dashboard provides an overview of:

- Total account balance
- Monthly income
- Monthly expenses
- Net amount
- Monthly cash flow
- Budget progress
- Recent transactions

## How to Use the Application

### 1. Create an Account

1. Open the application.
2. Select **Create an account**.
3. Enter your name, email, and password.
4. Click **Create account**.
5. Save the recovery code displayed on the screen.
6. Click **I saved it — continue**.

### 2. Create a Financial Account

1. Open **Accounts**.
2. Select the option to create a new account.
3. Enter an account name.
4. Choose an account type.
5. Enter the initial balance.
6. Save the account.

Example:

- Name: Cash Wallet
- Type: CASH
- Initial balance: 2,000,000 VND

### 3. Create Categories

1. Open **Categories**.
2. Create at least one income category.
3. Create at least one expense category.

Example:

- Salary — INCOME
- Food — EXPENSE
- Transportation — EXPENSE

### 4. Record a Transaction

1. Open **Transactions**.
2. Select the option to add a transaction.
3. Choose income or expense.
4. Enter the amount.
5. Select an account and category.
6. Select the transaction date.
7. Save the transaction.

> You must create an account and category before recording a transaction.

### 5. Create a Monthly Budget

1. Open **Budgets**.
2. Select an expense category.
3. Choose the month and year.
4. Enter the budget amount.
5. Save the budget.

Example:

- Category: Food
- Budget: 1,500,000 VND
- Month: September
- Year: 2026

### 6. View the Dashboard

Open **Dashboard** to review:

- Current balance
- Monthly income
- Monthly expenses
- Monthly cash flow
- Budget usage

## Resetting a Password

1. Open the login page.
2. Select **Forgot password?**
3. Enter your account email.
4. Enter the recovery code saved during registration.
5. Enter and confirm a new password.
6. Click **Reset password**.
7. Sign in using the new password.

## Technology Stack

### Backend

- Java
- Spring Boot
- Spring Security
- JSON Web Token
- Spring Data JPA
- MySQL
- Flyway
- Maven

### Frontend

- React
- TypeScript
- Vite
- Tailwind CSS
- Axios
- React Router
- Recharts

### Deployment

- Frontend: Vercel
- Backend: Railway
- Database: MySQL on Railway
- Source code: GitHub

## Security

- Passwords are hashed with BCrypt.
- Recovery codes are stored only as hashes.
- Protected API requests require a JWT Bearer Token.
- Business endpoints require authentication.
- Users can access only resources they own.
- Database credentials and JWT secrets are stored in environment variables.
- Backend CORS access is restricted to approved frontend origins.

## Data Storage

Application data is stored in MySQL on Railway:

- `user_info`
- `account_info`
- `category_info`
- `financial_transaction_info`
- `budget_info`

The Vercel frontend does not directly store users’ financial data.

## Current Limitations

- Only VND is supported.
- Email verification is not implemented.
- Google authentication is not implemented.
- Recovery codes are not sent by email.
- There is no user administration interface.
- Excel and PDF exports are not implemented.
- Recurring transactions are not supported.
- Multiple currencies are not supported.

## Author

Developed by Bùi Chí Dũng.
