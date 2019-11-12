# myCalendar <img src="https://i.ibb.co/55RSrLC/ic-luncher.png" width=50>

Second version of calendar application that gives possibility to add an events. Main function of the application is access to the shared
calendar and to added events. All events added by users are stored on Firebase Realtime Database. Thanks to that solution, every user 
has an attempt to see, add or remove events on real time. All users using the same login have access to the shared calendar. Events added by one of the users will be available to all users.

#Sources:
- Firebase Realtime Database: https://firebase.google.com
- calendar library: https://github.com/tizisdeepan/EventsCalendar
- carousel picker library: https://github.com/GoodieBag/CarouselPicker
- images: https://www.vectorportal.com/

# Main screen:
This is the screen, where user can log in to application. Application check validation of email and password and if it's not in database, 
show proper message (second screen). User can also restore his password by using proper button on main screen. After input his profile email, user receives email to reset password.
New user can register theirs email by clicking the text below login button. Then user go next screen, where he can register (third screen). 


<img src="https://i.ibb.co/6s8S1xX/main-activity-1.jpg" width=250><img src="https://i.ibb.co/xFhRzZx/main-activity-2.jpg" width=250> <img src="https://i.ibb.co/Dwsp35j/registration-activity-1.jpg" height=500 width=250>


After proper register user is automatically moved to screen with calendar.
User have to login only for the first time. Until he logout, every lunch application will moved user to calendar screen (forth screen).

# Calendar screen
This is the screen, where is the main calendar (forth screen). User can selected day and swipe left or right to change the month. 
Every month has different background and image. Dots on calendars represents added events.
When user click on options icon (on top right corner) appears options menu (sixth screen). On options menu user may close application, logout, go to information about application or open settings menu.


<img src="https://i.ibb.co/3ygYgQn/calendar-activity-1.jpg" width=250> <img src="https://i.ibb.co/3fpRxbD/calendar-activity-2.jpg" width=250> <img src="https://i.ibb.co/h2LcZ1r/options-1.jpg" width=250>

At settings user can: add or remove events types to calendar,  delete user profile (with all his data) ore change font size of events type picker (seventh screen).
When user long press day with no dot, he will be moved to next screen, where he can add an event on that day (eight screen). 
User can chose: evet by horizontal swipe carousel picker and time by vertical swipe begin and end time pickers.

Otherwise, when user long press day with dot, he will be moved to next screen, where he will see all events on picked day (ninth screen).
At this screen user can also remove each event and/or add new event by clicking on the icon in bottom right corner (which open 
add screen).


<img src="https://i.ibb.co/Nm3872S/settings-1.jpg" width=250> <img src="https://i.ibb.co/6P2jfZH/add-activity-1.jpg" width=250> <img src="https://i.ibb.co/jzvBvPH/day-activity-1.jpg" width=250> 


# Developed By
- Maciej Kosecki
