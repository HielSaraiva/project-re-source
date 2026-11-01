package edu.br.resource.resourcesystem.controller.shared;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LegacyPageRedirectController {
    @GetMapping("/sobre")
    public RedirectView about() {
        return redirect("/about");
    }

    @GetMapping("/sustentabilidade")
    public RedirectView sustainability() {
        return redirect("/sustainability");
    }

    @GetMapping("/politica-de-privacidade")
    public RedirectView privacy() {
        return redirect("/privacy-policy");
    }

    @GetMapping("/ong/dashboard")
    public RedirectView institutionDashboard() {
        return redirect("/institution/dashboard");
    }

    @GetMapping("/ong/donations")
    public RedirectView institutionDonations() {
        return redirect("/institution/donations");
    }

    @GetMapping("/ong/donations/{protocol:INT-[0-9]{4}-[0-9]+}")
    public RedirectView institutionDonation(@PathVariable String protocol) {
        return redirect("/institution/donations/" + protocol);
    }

    private RedirectView redirect(String path) {
        var view = new RedirectView(path, true);
        view.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
        view.setPropagateQueryParams(true);
        return view;
    }
}
