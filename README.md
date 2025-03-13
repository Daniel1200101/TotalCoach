Trainer-Trainee App
A mobile application that help trainers to magnage their trainees data and progress, by allowing to edit their trainee profile,
assign tasks, upload media, track progress, and handle trainees payments efficiently.
The app integrates Firebase for authentication, real-time data management, and media storage.

Authentication & User Roles
Supports Google One Tap Sign-In and Email/Password authentication.
After login, the app checks the user’s role (Trainer or Trainee).
Users are redirected to their respective Main Activity based on their role.
Trainer's Main Activity
Trainers have access to multiple features, each represented as a tab (fragment):

Profile Section
help
Displays the trainer’s profile picture and name.
Task List Tab

Shows daily tasks.
Trainers can manage and track his tasks.

Trainee List Tab-

Displays all trainees linked to the trainer.
Trainers can add new trainees by filling in their details.
A trainee’s login credentials are automatically sent via email.

Calendar Tab-

Integrates with the Task List to show scheduled tasks for the selected date.

Payment List Tab-

Collects trainee payment data.
Provides a summary and insights for the trainer.



Trainee's Main Activity
Trainees have access to the following features:

Task List & Calendar Tab-

The trainee can view assigned tasks, while the trainer can edit them.

Media Content Tab-

Both trainees and trainers can upload media (images/videos).
A filter button allows users to view media based on the uploader.

Progress Tab-

Integrates with Google Sheets.
Displays the trainee’s progress sheet and workout plan sheet.
Firebase Integration
The app uses Firebase for seamless backend management:

Firestore → Stores user and trainee data.
Firebase Storage → Manages images and videos uploaded by trainees and trainers.
Cloud Functions → Sends automated emails with login credentials to new trainees.
