package com.dsoftn;

import com.dsoftn.Settings.Settings;
import com.dsoftn.models.Users;
import com.dsoftn.models.User;


public class OBJECTS {

    // Singletons
    
    public static final Settings SETTINGS = new Settings();

    public static final Users USERS = new Users();

    // Objects

    public static User ACTIVE_USER;

}
