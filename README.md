# MovieRater Application

This is a command-line Java application for managing movie viewing habits, interacting with a SQLite database using JDBC. It fulfills the requirements of Assignment 2: MovieRater.

## Features

The application provides the following functionalities:

1.  Add a new user to the database.
2.  View all viewing habit data for a specific user.
3.  Change the title of an existing movie.
4.  Delete a viewing record from the `ViewingHabit` table.
5.  Calculate and display the mean age of all users.
6.  Count the total number of unique users who have watched a specific movie.
7.  Calculate and display the total minutes watched by all users.
8.  Count the total number of users who have watched more than one movie.

## Database Schema

The application uses a SQLite database named `movierater.db` with the following tables:

-   **User**: Stores user information (`UserID`, `Age`, `Email`).
-   **Movie**: Stores movie details (`MovieID`, `Title`, `ReleaseYear`, `Director`, `Genre`).
-   **ViewingHabit**: Links users to movies and records minutes watched (`UserID`, `MovieID`, `MinutesWatched`).

## Setup 

-   Java Development Kit (JDK) 17 or higher.
-   SQLite JDBC Driver (included in the `lib` directory).

### Steps to Run

1.  **Navigate to the project directory:**
    ```bash
    cd /MovieRater
    ```

2.  **Compile the Java code:**
   

3.  **Run the application:**
   


## Project Structure

```
MovieRater/
├── bin/                      # Compiled Java classes
├── lib/                      # SQLite JDBC driver
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── movierater/
│                   └── MovieRater.java # Main application logic
├── movierater.db             # SQLite database file 
└── README.md                 # This file
```
