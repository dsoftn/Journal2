# My Journal 2 - AppFlow

# Table of Contents
- [Application Flow](#application-flow-⤴)
- [Dialogs](#dialogs-⤴)
- [Services](#services-⤴)
- [Folder Structure](#folder-structure-⤴)
- [Special classes](#special-classes-⤴)
- [Files Explanation](#files-explanation-⤴)


### Application Flow <sup>[⤴](#table-of-contents)</sup>
- [On App start](#on-app-start-⤴)

### Dialogs <sup>[⤴](#table-of-contents)</sup>
- [Login Dialog](#login-dialog-⤴)
- [MsgBox Dialog](#msgbox-dialog-⤴)
- [Main Window Dialog](#main-window-dialog-⤴)

### Models <sup>[⤴](#table-of-contents)</sup>
- [General Models Rules](#general-models-rules-⤴)
- [Users-User Model](#users-user-model-⤴)
- [Blocks-Block Model](#blocks-block-model-⤴)
- [Tags-Tag Model](#tags-tag-model-⤴)
- [Categories-Category Model](#categories-category-model-⤴)
- [ScopeEnum](#scopeenum-⤴)

### Services <sup>[⤴](#table-of-contents)</sup>
- [SQLiteDB Service](#sqlitedb-service-⤴)
- [RichText Service](#richtext-service-⤴)
- [EventHandler Service](#eventhandler-service-⤴)

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

## Main Window Dialog <sup>[⤴](#dialogs-⤴)</sup>
### Overview

## General Models Rules <sup>[⤴](#models-⤴)</sup>
### Overview
All models contain 2 classes:
1. `Model ALL` class that store all data and is part of `OBJECTS` class
    - Do not use methods of this class for `add`, `update` and `delete` operations. This methods is meant to be used by `Model SINGLE` class. Those methods only update information in `Model ALL` class and not in database.
    - Method `load()` is used to load all data from database and this should be called before any other action.
2. `Model SINGLE` class that store single object.
    - Use methods of this class for `add`, `update` and `delete` operations. This methods update information in `Model ALL` class and in database.
3. `Relations` model stores information about relations between models.

## Users-User Model <sup>[⤴](#models-⤴)</sup>
### Overview
Users - User is special model that is not stored in database.
Every user has its own folder in `data/users/` folder.
#### Loading all Users
Start `Users.loadAllUsers()` method.
This method reads all folders in `data/users/` folder.
Each folder is sent to `User.load(folder_path)` method, then this method try to load `username_info.json` and return `true` if successful.
#### Selecting User
For selecting user is responsible `Login Dialog`.
Information about selected user is stored in `OBJECTS.ACTIVE_USER` object.

## Blocks-Block Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Load all blocks with `Blocks.load()` method, this should be called before any other action.
- Dates are stored in database in JSON format.
- Getters for date properties as `date`, `created` and `updated` methods give date in NORMAL format.
- Pass to setters date in NORMAL format or object.

### To Add, Update or Delete block
Use `Block` class only. DO NOT USE `Blocks` class.
1. Make instance of `Block` class
2. Call `load(id)` method if you want to load block from database
3. Change block properties
4. Call `add()`, `update()` or `delete()` method. It is good idea to call `canBeAdded()`, `canBeUpdated()` and `canBeDeleted()` methods before calling `add()`, `update()` or `delete()` method
5. Database and `Block` class will be updated automatically
```java
// Change block name example
Block block = new Block();
block.load(id);
block.setName("My Block");
block.update();
```
### How to add new property to block
Update following code in `Block` class:
1. Add variable with new property
2. Add getter and setter for new property
3. Add new property in method `Block.loadFromResultSet`
4. Add new property in method `Block.add` sql query
5. Add new property in method `Block.update` sql query
6. Add new property in method `Block.duplicate`
7. Update `Blocks` class docstring
8. Update **DatabaseTables** settings

## Tags-Tag Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Load all tags with `Tag.load()` method, this should be called before any other action.
- Dates are stored in database in JSON format.
- Getters for date properties `created` methods give date in NORMAL format.
- Pass to setters date in NORMAL format or object.
- Use `Tag.scope` (`ScopeEnum`) to set scope of tag (BLOCK, DEFINITION...).


### To Add, Update or Delete tag
Use `Tag` class only. DO NOT USE `Tags` class.
1. Make instance of `Tag` class
2. Call `load(id)` method if you want to load tag from database
3. Change tag properties
4. Call `add()`, `update()` or `delete()` method. It is good idea to call `canBeAdded()`, `canBeUpdated()` and `canBeDeleted()` methods before calling `add()`, `update()` or `delete()` method
5. Database and `Tag` class will be updated automatically
```java
// Change tag name example
Tag tag = new Tag();
tag.load(id);
tag.setName("My Tag");
tag.update();
```
### How to add new property to tag
Update following code in `Tag` class:
1. Add variable with new property
2. Add getter and setter for new property
3. Add new property in method `Tag.loadFromResultSet`
4. Add new property in method `Tag.add` sql query
5. Add new property in method `Tag.update` sql query
6. Add new property in method `Tag.duplicate`
7. Update `Tags` class docstring
8. Update **DatabaseTables** settings

## Categories-Category Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Load all categories with `Category.load()` method, this should be called before any other action.
- Dates are stored in database in JSON format.
- Getters for date properties `created` methods give date in NORMAL format.
- Pass to setters date in NORMAL format or object.
- Use `Category.scope` (`ScopeEnum`) to set scope of category (BLOCK, DEFINITION...).


### To Add, Update or Delete category
Use `Category` class only. DO NOT USE `Categories` class.
1. Make instance of `Category` class
2. Call `load(id)` method if you want to load category from database
3. Change category properties
4. Call `add()`, `update()` or `delete()` method. It is good idea to call `canBeAdded()`, `canBeUpdated()` and `canBeDeleted()` methods before calling `add()`, `update()` or `delete()` method
5. Database and `Category` class will be updated automatically
```java
// Change category name example
Category category = new Category();
category.load(id);
category.setName("My Category");
category.update();
```

### How to add new property to category
Update following code in `Category` class:
1. Add variable with new property
2. Add getter and setter for new property
3. Add new property in method `Category.loadFromResultSet`
4. Add new property in method `Category.add` sql query
5. Add new property in method `Category.update` sql query
6. Add new property in method `Category.duplicate`
7. Update `Categories` class docstring
8. Update **DatabaseTables** settings

## ScopeEnum <sup>[⤴](#models-⤴)</sup>
### Overview
- `ScopeEnum` class is used to set scope of **tag**.
Based on *scope* you can distinguish **tags** that belong to various models like **BLOCKS**, **DEFINITIONS**, etc.
- It is also used in **Relations** model to define models entities that are related.

## SQLiteDB Service <sup>[⤴](#services-⤴)</sup>
### Usage
1. Make instance of `SQLiteDb` class.
2. Perform database operations.
3. Close database connection with `disconnect()`.

### Example
```java
id = 2
SQLiteDb db = new SQLiteDb();
// Database will be automatically connected to "OBJECTS.ACTIVE_USER.getDbPath()"
PreparedStatement statement = db.prepareStatement("SELECT * FROM blocks WHERE id = ?", id);
ResultSer result = db.select(statement);
// This will return ResultSet, you can walk through it with `next()` method
db.disconnect();
```

## RichText Service <sup>[⤴](#services-⤴)</sup>
### Usage
1. Make instance of `RichText` class.
2. Set `generalRule`, you can set `text` in this rule
3. Alternatively set text with `setText()` method
4. Add `RichTextRule` to `RichText.rules` list.
5. Get `TextFlow` object with `getTextFlow()` method.

### Example
```java
RichText richText = new RichText();
richText.generalRule = new RichTextRule("This is example");
RichTextRule rule = new RichTextRule("is");
rule.setFontColor("#ffff00");
richText.rules.add(rule);
TextFlow textFlow = richText.getTextFlow();
```

## EventHandler Service <sup>[⤴](#services-⤴)</sup>
### Overview
- Every Dialog or Class should register to `EventHandler` through `EventHandler.register` method. Class that registers itself should implement **CustomEventListener** interface with method `onCustomEvent`.
With process of registration class will provide list of Events it is interested in.
- When class is closed `EventHandler.unregister` method should be called.
- When event is fired `EventHandler.fireEvent` method should be called. This method will pass event to all interested classes.
- `EventHandler` is part of `OBJECTS` class.

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

