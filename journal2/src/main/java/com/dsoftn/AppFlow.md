# My Journal 2 - AppFlow

## On App start (Login)
- **Main.java** call **GuiMain.java** class
- In `start()` method:
    1. Check if ***settings.json*** and ***language.json*** exist
        - If not exist, allow user to find those files
        - Method `isSettingsAndLanguageFilesExist()` is responsible for this
        - If exist, load them, otherwise exit app
    2. Start **LoginController.java** (`LoginController.startMe()`)
    3. Read `getAuthenticatedUser()` from **LoginController** to find logged in user
    4. Start **MainWinController.java** (`MainWinController.startMe()`)

