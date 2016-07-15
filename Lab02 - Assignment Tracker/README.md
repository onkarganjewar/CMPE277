# Assignment Tracker
An android application to keep track of all the assignments of a particular user.

## Features
1. Create an assignment
2. View assignments

### How does it work?
User will first create an account in the application. User will then have the option to either create an assignment or view an assignment. User will also have the option of either "Signout" or "Exit" the application.

* Used SQLite database to store user credentials and assignment details. There are two table in my database namely Assignment and User which stores respective details about the models.
* Used explicit intent for storing the information about user to be used later on such “id” of the user.
