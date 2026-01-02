package cc.sars.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class CambiarGrupoController {

    @PostMapping("/cambiarGrupo")
    public String selectGroup(@RequestParam("nombreGrupo") String groupName,
                              HttpSession session) {
        session.setAttribute("currentActiveGroup", groupName);
        return "redirect:/";
    }
}
