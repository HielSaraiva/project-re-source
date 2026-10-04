package edu.br.resource.resourcesystem.controller.donor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import edu.br.resource.resourcesystem.service.donor.DonorDashboardService;

@Controller
public class DonorDashboardController {

    private final DonorDashboardService dashboardService;

    public DonorDashboardController(DonorDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/", "/donor/dashboard"})
    public String dashboard(Model model) {
        model.addAllAttributes(dashboardService.dashboardData());
        return "donor/dashboard";
    }
}
