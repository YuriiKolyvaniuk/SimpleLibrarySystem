# Library Management System
This is a simple library management system that allows users to add books, search for books by title, author, or ISBN, rent books, and view rented books.

## Technologies
This application was built using the following technologies:

- Maven
- MySQL database
- Hashing for login security
- Singleton design pattern
- Java streams (if possible)
- JUnit for testing (not all methods)
## Features
### User Authentication
* User must login with a username and password before accessing the system
* Passwords are hashed for security purposes
### Adding Books
* User can add books to the system by entering the book's title, author, and ISBN
### Searching for Books
* User can search for books by title, author, or ISBN (partial information is also accepted)
* The search results will show whether the book is available for rent or currently rented out, along with information about the person who rented the book and the rental dates
### Renting Books
* User can rent a book by entering the name of the person who will be renting the book
* The rental period is two weeks
### Viewing Rented Books
* User can view all currently rented books, along with information about the person who rented the book and the rental dates
### Overdue Books
* User can view all rented books that are overdue, along with information about the person who rented the book and the rental dates
### Viewing All Books
* User can view a list of all books in the system
## How to Run
1. Clone the repository to your local machine
2. Navigate to the project directory
3. Set up a MySQL database and update the database connection details in the DatabaseConnection.java file
4. Run mvn package to build the application
5. Run java -jar target/library-system.jar to start the application

## Conclusion
This library management system provides basic functionality for managing books in a library setting. The system is secure and user-friendly, making it easy for library staff to add, search for, and rent books. Future improvements could include the ability to reserve books, track overdue fees, and integrate with online library catalogs.
