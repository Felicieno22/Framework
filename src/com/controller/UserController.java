package com.controller;

import java.util.ArrayList;
import java.util.List;

import com.annotation.Controller;
import com.annotation.GET;
import com.annotation.Param;
import com.mapping.ModelView;
import com.mapping.MySession;

@Controller
public class UserController {

    @GET("/login")
    public ModelView login(MySession mySession, @Param(name = "username") String username, @Param(name = "password") String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            mySession.add("username", username);
            List<String> userData = new ArrayList<>();
            userData.add("Fanomezantsoa");
            userData.add("MAHAFALIARIMBOLA");
            mySession.add("userData", userData);
        }
        ModelView modelView = new ModelView();
        modelView.setUrl("/views/UserInfo.jsp");
        return modelView;

    }

    
    @GET("/logout")
    public ModelView logout(MySession mySession) {
        mySession.delete("username");
        mySession.delete("userData");
        ModelView mv = new ModelView();
        mv.setUrl("/views/Login.jsp");
        return mv;
    }
}

