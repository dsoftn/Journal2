package com.dsoftn;

import com.dsoftn.Settings.Settings;
import com.dsoftn.models.Users;
import com.dsoftn.models.Blocks;
import com.dsoftn.models.Categories;
import com.dsoftn.models.Tags;
import com.dsoftn.models.User;
import com.dsoftn.services.EventHandler;;


public class OBJECTS {

    // Singletons
    
    public static final Settings SETTINGS = new Settings();

    public static final EventHandler EVENT_HANDLER = new EventHandler();

    public static final Users USERS = new Users();

    public static final Blocks BLOCKS = new Blocks();

    public static final Tags TAGS = new Tags();

    public static final Categories CATEGORIES = new Categories();

    // Objects

    public static User ACTIVE_USER;

}
