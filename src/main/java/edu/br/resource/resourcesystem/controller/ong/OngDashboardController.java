package edu.br.resource.resourcesystem.controller.ong;

import edu.br.resource.resourcesystem.service.ong.OngDashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OngDashboardController {

    private final OngDashboardService dashboardService;

    public OngDashboardController(OngDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/ong/dashboard")
    public String dashboard(Model model) {
        model.addAllAttributes(dashboardService.dashboardData());
        return "ong/dashboard";
    }
}
