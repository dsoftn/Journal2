# My Journal 2 - AppFlow

# Table of Contents
- [Application Flow](#application-flow-⤴)
- [Dialogs](#dialogs-⤴)
- [Folder Structure](#folder-structure-⤴)


### Application Flow <sup>[⤴](#table-of-contents)</sup>
- [On App start](#on-app-start-⤴)

### Dialogs <sup>[⤴](#table-of-contents)</sup>
- [Login Dialog](#login-dialog-⤴)
- [MsgBox Dialog](#msgbox-dialog-⤴)
- [Main Window Dialog](#main-window-dialog-⤴)

### Folder Structure <sup>[⤴](#table-of-contents)</sup>
- [Java Classes Structure](#java-classes-structure-⤴)
- [Resources Structure](#resources-structure-⤴)
- [Data Folder Structure](#data-folder-structure-⤴)

### Special classes <sup>[⤴](#table-of-contents)</sup>
- [Class CONSTANTS](#class-constants-⤴)
- [Class DIALOGS](#class-dialogs-⤴)
- [Class OBJECTS](#class-objects-⤴)

### Files Explanation <sup>[⤴](#table-of-contents)</sup>
- In `data/user` folder: <sup>[⤴](#files-explanation-⤴)</sup>
    - [File **username_info.json**](#file-username_infojson-⤴)
    - [File **username_settings.json**](#file-username_settings.json-⤴)
    - [File **username_app_settings.json**](#file-username_app_settings.json-⤴)
    - [File **username.db**](#file-username.db-⤴)
    - [Folder **attachments**](#folder-attachments-⤴)
    

---
---
---

## On App start <sup>[⤴](#application-flow-⤴)</sup>
- **Main.java** call **GuiMain.java** class
- In `start()` method:
  1. **Create Folders** `/data...`:
     - Call `createDirectoryStructure()` method.
  2. **Check Files**:
     - Call `isSettingsAndLanguageFilesExist()` method.
     - If `settings.json` and `language.json` do not exist:
       - Allow the user to find those files.
       - Copy them to `data/app/settings/` as `settings.json` and `language.json`.
       - If files are not provided, exit the application.
  3. **Login Dialog** [⬅](#login-dialog-⤴):
     - Start `LoginController.startMe()`.
     - Retrieve authenticated user with `getAuthenticatedUser()`.
  4. **Main Window** [⬅](#main-window-dialog-⤴):
     - Start `MainWinController.startMe()`.

## Login Dialog <sup>[⤴](#dialogs-⤴)</sup>
### Overview
- Purpose: Login existing or create new user
- Controller: **LoginController.java**
- View: **LoginDialog.fxml**

## Main Window Dialog <sup>[⤴](#dialogs-⤴)</sup>
### Overview


## MsgBox Dialog <sup>[⤴](#dialogs-⤴)</sup>
### Overview
- Purpose: Show message or question to user
- Contains 2 Enums: `MsgBoxButton` and `MsgBoxIcon`
- Controller: **MsgBoxController.java**
- View: **MsgBoxDialog.fxml**

### Concepts
- MsgBox Dialog has 3 sections:
    1. *Header*
        - Has icon and text
        - By default set to hidden
    2. *Content*
        - Has icon and text
        - By default set to hidden
    3. *Buttons*
        - Contains buttons for user to click
        - By default contain only `MsgBoxButton.OK` button
- When you set icon or text to any section makes it automatically visible
- Some buttons have additional actions when clicked.
    - Example: `MsgBoxButton.FIND_FILE` opens Select File Dialog
- When button is clicked MsgBoxDialog is closed and result can be retrieved from `getSelectedButton()` and `getResult()` methods
    - Button like `MsgBoxButton.FIND_FILE` for instance store path to selected file in `getResult()` method

### How to use
1. Make a new `MsgBoxController` object
2. Setup *Window Title*, *Header Icon*, *Header Text*, *Content Icon*, *Content Text* and *Buttons*
3. Call `startMe()` method
4. Get result with `getSelectedButton()` and `getResult()` methods

### Example
```java
// Create a new MsgBoxController
MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(primaryStage);
// Setup the dialog
msgBoxController.setTitle("My Title");
msgBoxController.setHeaderIcon(MsgBoxIcon.INFO);
msgBoxController.setHeaderText("My Header Text");
msgBoxController.setContentIcon(MsgBoxIcon.INFO);
msgBoxController.setContentText("My Content Text");
msgBoxController.addButton(MsgBoxButton.OK, MsgBoxButton.CANCEL);
// Show the dialog
msgBoxController.startMe();
// Get the result
MsgBoxButton selectedButton = msgBoxController.getSelectedButton();
String result = msgBoxController.getResult();
```


## Java Classes Structure <sup>[⤴](#folder-structure-⤴)</sup>
**java.com.dsoftn** - root package
  - **controllers** - contains all controllers
  - **interfaces** - contains all interfaces
  - **models** - contains all models (e.g. users, blocks, definitions, ...)
  - **settings** - contains `settings class` and necessary classes
  - **utils** - contains all utils

Root contains 3 global classes:
- **CONSTANTS.java** - all constants
- **DIALOGS.java** - starting all dialogs go here
- **OBJECTS.java** - all objects resident in memory throughout the application (e.g. user, settings, etc.)


## Resources Structure <sup>[⤴](#folder-structure-⤴)</sup>
Folders:
- **css** - contains all css files
- **fxml** - contains all fxml files
- **images** - contains all images


## Data Folder Structure <sup>[⤴](#folder-structure-⤴)</sup>
- **data** - contains all data files
  - **app** - contains all application files
      - **settings** - contains all settings files (***settings.json*** and ***language.json***)
  - **user** - contains all user folders
      - Every user has folder with name equal to `username` (Special characters are replaced with `_`)
      - User folder contains files:
          - **username_info.json** - contains user information
          - **username_settings.json** - contains user settings
          - **username_app_settings.json** - contains application settings like Dialogs position, size, etc.
          - **username.db** - contains database with user data
          - folder **attachments** - contains all attachments


## Class CONSTANTS <sup>[⤴](#special-classes-⤴)</sup>
This class contains all constants used in the application
### Usage
```java
System.out.println(CONSTANTS.APPLICATION_NAME);
```

## Class DIALOGS <sup>[⤴](#special-classes-⤴)</sup>
This class contains all dialogs used in the application
Starting all dialogs goes from this class
This class contains methods to start any dialog controller
### Usage
```java
MsgBoxController msgBoxController = DIALOGS.getMsgBoxController(primaryStage);
```

### Note
**MainWinController** and **LoginController** are only classes that starts directly and not through **DIALOGS** class


## Class OBJECTS <sup>[⤴](#special-classes-⤴)</sup>
This class contains all objects used in the application that must be accessible from anywhere in the application
### Usage
```java
System.out.println(OBJECTS.SETTINGS.getv("key"));
```

## File username_info.json <sup>[⤴](#files-explanation-⤴)</sup>
File contain 6 variables:
- `UserName` - user name
- `PathName` - name that is used as folder name
- `Language` - language
- `Password` - First password field
- `Data` - Second password field
- `Settings` - Third password field

Password is stored by following algorithm:
- (1.) password character: 3 position in *Password* string
- (2.) password character: 3 position in *Data* string
- (3.) password character: 3 position in *Settings* string
- Shift position by 3
- (4.) password character: 6 position in *Password* string
- (5.) password character: 6 position in *Data* string
- (6.) password character: 6 position in *Settings* string
- Shift position by 6
- ....
- Maximum 100 characters in password
- All characters in between are random characters

```json
{
    "UserName": "username",
    "PathName": "username",
    "Language": "en",
    "Password": "password characters on pos 3,6,9...",
    "Data": "password characters on pos 3,6,9...",
    "Settings": "password characters on pos 3,6,9..."
}
```

