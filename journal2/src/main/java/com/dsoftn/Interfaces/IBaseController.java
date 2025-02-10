package com.dsoftn.Interfaces;

import javafx.stage.Stage;

public interface IBaseController {

    public void setStage(Stage stage);
    
    public void startMe();

    public void closeMe();

    public String getMyName();

}
