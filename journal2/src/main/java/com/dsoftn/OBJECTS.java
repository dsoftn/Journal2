package com.dsoftn;

import com.dsoftn.Settings.Settings;
import com.dsoftn.models.Users;
import com.dsoftn.models.Blocks;
import com.dsoftn.models.Definitions;
import com.dsoftn.models.DefVariants;
import com.dsoftn.models.Categories;
import com.dsoftn.models.Tags;
import com.dsoftn.models.Attachments;
import com.dsoftn.models.Relations;
import com.dsoftn.models.Actors;
import com.dsoftn.models.User;
import com.dsoftn.services.EventHandler;
import com.dsoftn.services.SQLiteDB;


public class OBJECTS {

    // Services
    
    public static final Settings SETTINGS = new Settings();

    public static final EventHandler EVENT_HANDLER = new EventHandler();

    public static SQLiteDB DATABASE;

    // Models
    
    public static final Users USERS = new Users();

    public static final Blocks BLOCKS = new Blocks();

    public static final Definitions DEFINITIONS = new Definitions();

    public static final DefVariants DEFINITIONS_VARIANTS = new DefVariants();

    public static final Tags TAGS = new Tags();

    public static final Categories CATEGORIES = new Categories();

    public static final Attachments ATTACHMENTS = new Attachments();

    public static final Relations RELATIONS = new Relations();

    public static final Actors ACTORS = new Actors(); 

    // Objects

    public static User ACTIVE_USER;

}
