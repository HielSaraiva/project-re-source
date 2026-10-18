
package edu.br.resource.resourcesystem.controller.donor;

import edu.br.resource.resourcesystem.security.CurrentActor;
import edu.br.resource.resourcesystem.service.DonationHistoryService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.br.resource.resourcesystem.dto.response.DonationHistoryResponse;

@Controller
@RequestMapping("/donor/donations")
public class DonorDonationHistoryController {

    private final DonationHistoryService donationHistoryService;
    private final CurrentActor actors;

    public DonorDonationHistoryController(
            DonationHistoryService donationHistoryService,
            CurrentActor actors) {

        this.donationHistoryService = donationHistoryService;
        this.actors = actors;
    }

    @GetMapping
    public String history(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Integer donorId = actors.donor(authentication).getId();

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                10,
                Sort.by("createdAt").descending()
                        .and(Sort.by("id").descending())
        );

        Page<DonationHistoryResponse> donations =
                donationHistoryService.findDonationsByDonor(
                        donorId,
                        pageable
                );

        model.addAttribute("donations", donations);
        model.addAttribute("currentPage", donations.getNumber());
        model.addAttribute("totalPages", donations.getTotalPages());

        return "donor/donations/history";
    }
}
