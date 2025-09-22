package com.example.starter_project.systems.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoleViewController {

    @GetMapping("/roles")
    public String viewRoles(Model model) {
        model.addAttribute("title", "Roles");
        model.addAttribute("content", "roles");//systems/
        model.addAttribute("js", "/js/systems/role.js");

        return "layout";
//        model.addAttribute("name", "John Doe");
//        return "roles"; // Loads templates/roles.html
    }
}
