package com.example.HR_Management_Frontend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Maps the _links block that Spring Data REST adds to every HAL response.
 *
 * We only need the links that our controller actually uses:
 *   self    → used by EmployeeDTO.getSelfId() to extract the numeric ID
 *   manager → used by EmployeeController.buildManagerChain() to walk upward
 *
 * All other links (department, job, subordinates, …) are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HalLinksDTO {

    private HalLink self;
    private HalLink manager;

    public HalLink getSelf()                    { return self; }
    public void setSelf(HalLink self)           { this.self = self; }

    public HalLink getManager()                 { return manager; }
    public void setManager(HalLink manager)     { this.manager = manager; }

    // ── Inner class ──────────────────────────────────────────────────────────

    /**
     * Represents a single HAL link object: { "href": "...", "templated": true }
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HalLink {
        private String href;
        private boolean templated;

        public String getHref()                 { return href; }
        public void setHref(String href)        { this.href = href; }

        public boolean isTemplated()            { return templated; }
        public void setTemplated(boolean v)     { this.templated = v; }
    }
}
