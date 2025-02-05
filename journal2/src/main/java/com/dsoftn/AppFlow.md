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
- [CSS Management](#css-management-⤴)

### Dialogs <sup>[⤴](#table-of-contents)</sup>
- [Login Dialog](#login-dialog-⤴)
- [MsgBox Dialog](#msgbox-dialog-⤴)
- [Main Window Dialog](#main-window-dialog-⤴)

### Models <sup>[⤴](#table-of-contents)</sup>
- [General Models Rules](#general-models-rules-⤴)
- [How to add new Model](#how-to-add-new-model-⤴)
- [How Events work between Models](#how-events-work-between-models-⤴)
- [Users-User Model](#users-user-model-⤴)
- [Blocks-Block Model](#blocks-block-model-⤴)
- [BlockTypes-BlockType Model](#blocktypes-blocktype-model-⤴)
- [Definitions-Definition Model](#definitions-definition-model-⤴)
- [DefVariants-DefVariant Model](#defvariants-defvariant-model-⤴)
- [Attachments-Attachment Model](#attachments-attachment-model-⤴)
- [Tags-Tag Model](#tags-tag-model-⤴)
- [Categories-Category Model](#categories-category-model-⤴)
- [Actors-Actor Model](#actors-actor-model-⤴)
- [Relations-Relation Model](#relations-relation-model-⤴)
- [ModelEnum (known as ScopeEnum)](#modelenum-⤴)

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
  4. **Create active user**:
     - In `OBJECTS` class is created object `ACTIVE_USER` of class `User`
  5. **Load Settings**:
     - Setting for `ACTIVE_USER` is loaded from user directory in `Settings` class.
  6. **Database connection**:
     - In `OBJECTS` class is created object `DATABASE` of class `SQLiteDB`
     - This connection stays open until application is closed.
     - Line of code `Runtime.getRuntime().addShutdownHook` ensures that connection is closed when application is closed.
  7. **Start Splash Screen**:
     - `SplashScreenController` will load all models data.
     - Order of models loading is important because some models depend on others.
  8. **Check if all models are loaded**:
     - Calling `getErrorsInModelLoading()` will check if all models are loaded successfully.
  9. **Main Window** [⬅](#main-window-dialog-⤴):
     - Start `MainWinController.startMe()`.

## CSS Management <sup>[⤴](#application-flow-⤴)</sup>
## Overview
- **main.css** contains all css rules used in the application.
- For most of widgets is defined **default** class. This class needs to be set in `SceneBuilder` file for each widget.
- Only for some widgets (for example *separator*) CSS is set directly (like *Native*). That means that this widget will have those *CSS* without setting **default** class in `SceneBuilder`.
- Every *Scene* has its own *CSS* file named same as **FXML** file but in snake style (e.g. `MsgBox.fxml` -> `msg_box.css`).

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

#### How to add new **Icon** to MsgBoxDialog
- Just add new icon in `MsgBoxIcon` enum which is part of `MsgBoxController` class

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

## How to add new Model <sup>[⤴](#models-⤴)</sup>
1. Create `Model classes` **Repository** and **Entity** classes
2. Add **Repository** class to `OBJECTS` class
3. Make **events** for `Model` if necessary
4. Add **Events** to `Relations` class if necessary
5. Add new model to `ScopeEnum` class
6. Add new model to `GuiMain.getErrorsInModelLoading` to properly check loading errors
7. Update `SplashScreenController` to `load()` model properly

#### How to add events to `Relations` class
- Add event for new *Model*  in *Relations* **Constructor** to register with **EventHandler**
- In `onCustomEvent` method add **elseIf** for new model events
- Make for each new event appropriate **private Method**

#### How to update `SplashScreenController` class
- In **SceneBuilder** open `SplashScreen.fxml` file and add *Label* for new model
- Add new *Label* in `@FXML` *Widgets* section
```java
@FXML
private Label my_new_label; // Add your label here !
```
- Update `setupWidgetText` method to add text in new *Label*
- Set icon for new *Label* in `initialize()` method:
```java
// Set images for labels
lblTaskBlocks.setGraphic(imgSelected.get(ScopeEnum.BLOCK.toString()));
lblTaskDefinitions.setGraphic(imgSelected.get(ScopeEnum.DEFINITION.toString()));
lblTaskAttachments.setGraphic(imgSelected.get(ScopeEnum.ATTACHMENT.toString()));
lblTaskCategories.setGraphic(imgSelected.get(ScopeEnum.CATEGORY.toString()));
lblTaskTags.setGraphic(imgSelected.get(ScopeEnum.TAG.toString()));
lblTaskDefVariants.setGraphic(imgSelected.get(ScopeEnum.DEF_VARIANT.toString()));
lblTaskRelations.setGraphic(imgSelected.get(ScopeEnum.RELATION.toString()));
// Add your label here !
```
- In `onCustomEvent` method add case for new model
    - **NOTE**: If model is BlockType no need to change `onCustomEvent` method
```java
case NEW_MODEL:
    changeTaskWidget(newModelLabel, "text_NewModel", taskStateEvent);
    break;
```
- In `createGlobalDataModels` method add new model
    - **NOTE**: If model is BlockType change `createGlobalBlockTypeModels` method instead
```java
if (!OBJECTS.NEW_MODEL.load()) {
    UError.error("GuiMain.createGlobalDataModels -> OBJECTS.NEW_MODEL.load() failed");
    return false;
}
```

## How Events work between Models <sup>[⤴](#models-⤴)</sup>
### Behavior
1. When you *add*, *update* or *delete* model entity event is triggered
2. Class `Relations` listens to all events for every model that can be related with other model
3. When `Relations` class receive event it make all necessary relations and update information in `Relations` class and in database
4. `Relations` class trigger his own events for every relation that has been changed and for this event sets **isLoopEvent** property to `true`
5. Then Model receives event from `Relations` class but has no need to update information because it is loop event and Model itself is causing this event so it will be ignored
6. If event received from `Relations` class is not loop event that means that information in `Relations` have been changed directly and Model will update information about his relations automatically
- NOTE
    - Only Models that can be related with other models listen to `Relations` events
    - `Relations` class listens only Models that can be related with other models

#### Block and BlockType don't listen events between each other
1. `Block` class doesn't listen to `BlockType` events
2. `BlockType` class doesn't listen to `Block` events
3. NOTE
    - That means that every time you changing any of this models you must update information in both of them

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

### How to add new property to user
Update following code in `User` class:
1. Add variable with new property
2. Add getter and setter for new property
3. If property is not related with user path:
   - Add new property in methods `User.fromMap` and `User.toMap`
4. If property is related with user path:
    - Add property to `User.setPathName` method
    - No need to change `User.fromMap` and `User.toMap`

## Blocks-Block Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Every *Block* in **MyJournal** contain two parts.
  - *Block* classes that contain information about block (Relations, block type, date, text that is used for searching, etc.)
  - [Block Type](#blocktypes-blocktype-model-⤴) classes that contain block content (For each block type there is different content, and each Block Type has its own classes)
  - **Important**: *Block* and *Block Type* should be added, updated and deleted separately. They do not have any knowledge of each other, therefore updating *Block* will not update *Block Type*, and updating *Block Type* will not update *Block* automatically.
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
5. Database and `Blocks` class will be updated automatically
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
4. Update `isValid()` method if needed
4. Add new property in method `Block.add` sql query
5. Add new property in method `Block.update` sql query
6. Add new property in method `Block.duplicate`
7. Update methods `Block.equals` and `Block.hashCode`
8. Update `Blocks` class docstring
9. Update `Block` and `Blocks` **onCustomEvent** methods if needed
10. Update **DatabaseTables** settings
11. If property is relation with other model - update in `Relations` class events **add** and **update**

## BlockTypes-BlockType Model <sup>[⤴](#models-⤴)</sup>
### Overview
- You must perform `load()` method before using `BlockTypes` class.
- Each block type has `baseBlockID()` method that returns ID of base block.
- See [Blocks-Block Model](#blocks-block-model-⤴) for more information

### How to add new property to BlockType
Update following code in `BlockType` class:
1. Add variable with new property
2. Add getter and setter for new property
3. Add new property in method `BlockType.loadFromResultSet`
4. Update `isValid()` method if needed
4. Add new property in method `BlockType.add` sql query
5. Add new property in method `BlockType.update` sql query
6. Add new property in method `BlockType.duplicate`
7. Update methods `BlockType.equals` and `BlockType.hashCode`
8. Update `BlocksType` class docstring
9. Update **DatabaseTables** settings

### How to add new BlockType Model
1. Create classes `BlockType` and `BlocksType` (**Repository** and **Entity** classes)
2. Add **Repository** class to `OBJECTS` class
3. Add new **BlockType** to `BlockTypeEnum` enum
4. In **Events** add new property and getter in classes:
    - `BlockTypeAddedEvent`
    - `BlockTypeUpdatedEvent`
    - `BlockTypeDeletedEvent`
5. Add new `BlockType` to `Block` class in `getBlockTypeObject` method
6. Add new model to `GuiMain.getErrorsInModelLoading` to properly check loading errors
7. Update `SplashScreenController` to `load()` model properly
    - To update `SplashScreenController` class see [How to add new Model](#how-to-add-new-model-⤴)

## Definitions-Definition Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Load all definitions with `Definitions.load()` method, this should be called before any other action.
- Dates are stored in database in JSON format.
- Getters for date properties as `date`, `created` and `updated` methods give date in NORMAL format.
- Pass to setters date in NORMAL format or object.
 - `DefVariants` model stores information about variants of definitions.

### To Add, Update or Delete definition
Use `Definition` class only. DO NOT USE `Definitions` class.
1. Make instance of `Definition` class
2. Call `load(id)` method if you want to load definition from database
3. Change definition properties
4. Call `add()`, `update()` or `delete()` method. It is good idea to call `canBeAdded()`, `canBeUpdated()` and `canBeDeleted()` methods before calling `add()`, `update()` or `delete()` method
5. Database and `Definitions` class will be updated automatically
```java
// Change block name example
Definition definition = new Definition();
definition.load(id);
definition.setName("My Definition");
definition.update();
```
### How to add new property to definition
Update following code in `Definition` class:
1. Add variable with new property
2. Add getter and setter for new property
3. Add new property in method `Definition.loadFromResultSet`
4. Update `isValid()` method if needed
4. Add new property in method `Definition.add` sql query
5. Add new property in method `Definition.update` sql query
6. Add new property in method `Definition.duplicate`
7. Update `Definitions` class docstring
8. Update `Definition` and `Definitions` **onCustomEvent** methods if needed
9. Update **DatabaseTables** settings
10. If property is relation with other model - update in `Relations` class events **add** and **update**

## DefVariants-DefVariant Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Load all defVariants with `DefVariants.load()` method, this should be called before any other action.
- This classes stores information about variants of definitions.
- When you call on `Definition` object method 'add', 'update' or 'delete' methods, `DefVariants` class will be updated automatically with methods `OBJECT.DefVariants.updateVariantsDefinitionAdd`, `OBJECT.DefVariants.updateVariantsDefinitionUpdate` and `OBJECT.DefVariants.updateVariantsDefinitionDelete`
- When updating data, `DefVariants` will first delete all variants for definition and then add new variants

#### Note
- If you want to add new property to `DefVariant` class, you must also update SQL in method `insertMany` in class `SQLiteDB` !

## Attachments-Attachment Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Load all attachments with `Attachments.load()` method, this should be called before any other action.
- Dates are stored in database in JSON format.
- Getters for date properties as `created`, `file_created`, `file_modified` and `file_accessed` methods give date in NORMAL format.
- Pass to setters date in NORMAL format or object.

### To Add, Update or Delete attachment
Use `Attachment` class only. DO NOT USE `Attachments` class.
1. Make instance of `Attachment` class
2. Call `load(id)` method if you want to load attachment from database
3. Change attachment properties
4. Call `add()`, `update()` or `delete()` method. It is good idea to call `canBeAdded()`, `canBeUpdated()` and `canBeDeleted()` methods before calling `add()`, `update()` or `delete()` method
5. Database and `Attachments` class will be updated automatically
```java
// Change block name example
Attachment attachment = new Attachment();
attachment.load(id);
attachment.setName("My Attachment");
attachment.update();
```
### How to add new property to attachment
Update following code in `Attachment` class:
1. Add variable with new property
2. Add getter and setter for new property
3. Add new property in method `Attachment.loadFromResultSet`
4. Update `isValid()` method if needed
4. Add new property in method `Attachment.add` sql query
5. Add new property in method `Attachment.update` sql query
6. Add new property in method `Attachment.duplicate`
7. Update `Attachments` class docstring
8. Update `Attachment` and `Attachments` **onCustomEvent** methods if needed
9. Update **DatabaseTables** settings
10. If property is relation with other model - update in `Relations` class events **add** and **update**


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
5. Database and `Tags` class will be updated automatically
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
4. Update `isValid()` method if needed
4. Add new property in method `Tag.add` sql query
5. Add new property in method `Tag.update` sql query
6. Add new property in method `Tag.duplicate`
7. Update `Tags` class docstring
8. Update `Tag` and `Tags` **onCustomEvent** methods if needed
9. Update **DatabaseTables** settings
10. If property is relation with other model - update in `Relations` class events **add** and **update**

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
5. Database and `Categories` class will be updated automatically
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
4. Update `isValid()` method if needed
4. Add new property in method `Category.add` sql query
5. Add new property in method `Category.update` sql query
6. Add new property in method `Category.duplicate`
7. Update `Categories` class docstring
8. Update `Category` and `Categories` **onCustomEvent** methods if needed
9. Update **DatabaseTables** settings
10. If property is relation with other model - update in `Relations` class events **add** and **update**

**NOTE**: `parent` field is **NOT** deep copy. If you try to make deep copy of `parent` field, it will cause that every Category object has its own tree.

## Actors-Actor Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Load all actors with `Actor.load()` method, this should be called before any other action.
- Dates are stored in database in JSON format.
- Getters for date properties as `date`, `created` and `updated` methods give date in NORMAL format.
- Pass to setters date in NORMAL format or object.

### To Add, Update or Delete actor
Use `Actor` class only. DO NOT USE `Actors` class.
1. Make instance of `Actor` class
2. Call `load(id)` method if you want to load actor from database
3. Change actor properties
4. Call `add()`, `update()` or `delete()` method. It is good idea to call `canBeAdded()`, `canBeUpdated()` and `canBeDeleted()` methods before calling `add()`, `update()` or `delete()` method
5. Database and `Actors` class will be updated automatically
```java
// Change block name example
Actor actor = new Actor();
actor.load(id);
actor.setName("My Actor");
actor.update();
```
### How to add new property to actor
Update following code in `Actor` class:
1. Add variable with new property
2. Add getter and setter for new property
3. Add new property in method `Actor.loadFromResultSet`
4. Update `isValid()` method if needed
4. Add new property in method `actor.add` sql query
5. Add new property in method `Actor.update` sql query
6. Add new property in method `Actor.duplicate`
7. Update `Actors` class docstring
8. Update `Actor` and `Actors` **onCustomEvent** methods if needed
9. Update **DatabaseTables** settings
10. If property is relation with other model - update in `Relations` class events **add** and **update**

## Relations-Relation Model <sup>[⤴](#models-⤴)</sup>
### Overview
- Contains all relations between models.
- Load all relations with `Relations.load()` method, this should be called before any other action.
- **Structure**
    - `BaseModel` - model type that contain this relation
    - `BaseID` - id of model type that contain this relation
    - `RelatedModel` - model type that is related to `BaseModel`
    - `RelatedID` - id of model type that is related to `BaseModel`
- `Relations` class listens to *add*, *update* and *delete* events of `BaseModel` and updates relations automatically.

### To Add, Update or Delete relation
Most common and recommended way is to use `BaseModel` itself.
- Change your `BaseModel` and call `add()`, `update()` or `delete()` method on `BaseModel`.
- `BaseModel` will trigger event (*add*, *update* or *delete*)
- `Relations` class will respond to event and update relations automatically

Another way is to manage *Relations* model directly.
- Only use `Relation` class, do not use `Relations` class.
- Use methods `add()`, `update()` or `delete()` on `Related` class.
- In order this actions be performed correctly, `BaseModel` must have `Listener` for *add*, *update* and *delete* events that `Relation` class will trigger.
- `BaseModel` *Listener* must be in *Repository* class of `BaseModel`. This is very important because that way you can avoid **Events loop problem**. *See* [Events loop problem](#events-loop-problem)
- If `BaseModel` does not have `Listener` for *add*, *update* and *delete* events that `Relation` class will trigger, you will get incorrect data in `BaseModel`. After restarting *Application* `BaseModel` will contain correct data based on *Relations* you have changed.

### Events loop problem - DEPRECATED

|**NOTE**: *This problem has been solved! No need to read this section.*|
|----------------------------------------------------------

#### Problem
- When `BaseModel` updates his properties, `BaseModel` will trigger *add*, *update* and *delete* events.
- This events will be processed by `Relations` class.
- `Relations` class will update relations automatically and for each *relation* that is changed will trigger *add*, *update* or *delete* event.
- This will cause that `BaseModel` will receive *add*, *update* and *delete* events in from `Relations` class.
- Generally you don't want to respond to `Relations` events when they are triggered because `BaseModel` is causing them.
- You only want to respond to `Relations` events when they are triggered because `Relation` is directly changed, without knowledge of `BaseModel`.

#### Solution
- *Events* that are triggered by `Relations` class have property `isLoopEvent` set to `true` if they are caused by `BaseModel` class.
- Check *Event* property `isLoopEvent` before responding to event.
- If `isLoopEvent` is `true` then you know that event is caused by `BaseModel` class and generally you don't want to respond to it because `BaseModel` is already properly updated.
- If `isLoopEvent` is `false` then you know that event is caused by `Relation` class and you can respond to it to update `BaseModel` with new **Relations** data.

```java
public void onRelationAddedEvent(RelationAddedEvent event) {
    if (event.isLoopEvent()) {
        // do nothing
    } else {
        // update BaseModel with new data
    }
}
```

## ModelEnum <sup>[⤴](#models-⤴)</sup>
### Overview
- `ModelEnum` class is used to set scope of **tag**.
Based on *scope* you can distinguish **tags** that belong to various models like **BLOCKS**, **DEFINITIONS**, etc.
- It is also used in **Relations**  model to define models entities that are related.
- `ModelEnum` class is globally used to mark **Model** type.
- `ModelEnum.BLOCK_TYPE` is used to mark **BlockType** models list in `BlockTypeEnum` enum.

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
- If registered class has no strong reference to it, it will be garbage collected and automatically unregistered.
- You can manually unregister class with `EventHandler.unregister` method.
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
    - Every dialog or element has its own css file
- **fxml** - contains all fxml files
- **images** - contains all images
- **gifs** - contains all gifs


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

