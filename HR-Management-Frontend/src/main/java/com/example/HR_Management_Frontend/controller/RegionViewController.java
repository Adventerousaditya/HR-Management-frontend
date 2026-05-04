package com.example.HR_Management_Frontend.controller;

import com.example.HR_Management_Frontend.dto.RegionDetailDto;
import com.example.HR_Management_Frontend.model.Region;
import com.example.HR_Management_Frontend.service.RegionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/regions")
public class RegionViewController {

    private final RegionService regionService;

    public RegionViewController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping
    public String listRegions(@RequestParam(required = false) String q, Model model) {
        String keyword = q == null ? "" : q.trim();
        List<Region> regions = regionService.getAllRegions();

        if (!keyword.isBlank()) {
            String normalizedKeyword = keyword.toLowerCase();
            regions = regions.stream()
                    .filter(region -> region.getRegionName() != null
                            && region.getRegionName().toLowerCase().contains(normalizedKeyword))
                    .toList();
        }

        model.addAttribute("regions", regions);
        model.addAttribute("q", keyword);
        return "region/list";
    }

    @GetMapping("/{regionId}")
    public String regionDetail(@PathVariable Long regionId, Model model) {
        RegionDetailDto detail = regionService.getRegionDetail(regionId);
        model.addAttribute("region", detail);
        model.addAttribute("pageTitle", detail.getRegionName());
        return "region/detail";
    }
}
