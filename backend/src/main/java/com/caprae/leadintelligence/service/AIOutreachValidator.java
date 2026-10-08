package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.dto.OutreachBriefResponse;
import com.caprae.leadintelligence.entity.Lead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides:
 * 1. hasValidLinkedIn() - deterministic LinkedIn check
 * 2. buildDeterministicSummary() - Java-generated summary from Lead fields
 * 3. buildDeterministicChannel() - Java-generated channel recommendation
 * 4. buildDeterministicRiskNotes() - Java-generated risk notes from missing fields
 * 5. buildDeterministicTalkingPoints() - safe fallback talking points
 * 6. validateAiFields() - keyword check on the 3 AI-generated fields only
 */
public class AIOutreachValidator {

    private static final Logger log = LoggerFactory.getLogger(AIOutreachValidator.class);

    /** Prohibited patterns checked only against AI-generated fields */
    private static final List<String> PROHIBITED_PATTERNS = List.of(
            // ── Prior contact ─────────────────────────────────────────────────
            "previous conversation",
            "prior conversation",
            "as discussed",
            "following up on",
            "last time we spoke",
            "our earlier call",
            "per our last meeting",

            // ── AI / technology assumptions ───────────────────────────────────
            "interested in ai",
            "keen on ai",
            "wants to use ai",
            "leveraging ai",
            "ai initiatives",
            "ai integration",
            "ai technologies",
            "ai solutions",
            "ai-powered",
            "potential for ai",
            "use of ai",
            "ai adoption",
            "technology adoption",
            "technology solutions",
            "technology problems",
            "technology challenges",
            "technology stack",
            "digital transformation",
            "tool adoption",
            "software adoption",
            "automation interest",

            // ── Communication / process / operational problems ─────────────────
            "communication problems",
            "communication challenges",
            "internal communication",
            "communication gap",
            "process problems",
            "workflow issues",
            "operational challenges",
            "integration problems",
            "department challenges",
            "team coordination",
            "collaboration issues",
            "productivity problems",
            "efficiency issues",
            "operational bottlenecks",

            // ── Business assumptions ───────────────────────────────────────────
            "business challenges",
            "business problems",
            "business needs",
            "business goals",
            "buying intent",
            "purchase intent",
            "product interest",
            "solution fit",
            "pain points",
            "recent investments",
            "future plans",
            "growth needs",
            "growth strategy",
            "scaling challenges",
            "company priorities",
            "company objectives",
            "strategic initiative",
            "budget",

            // ── Hiring / headcount language ───────────────────────────────────
            "actively hiring",
            "engineering expansion",
            "engineering team expansion",
            "team expansion",
            "headcount growth",

            // ── Unverified decision-maker claims ──────────────────────────────
            "key decision maker",
            "stakeholder priorities",
            "decision-maker interests",
            "decision-maker goals",
            "decision-maker preferences",

            // ── External data ──────────────────────────────────────────────────
            "market trends",
            "industry benchmarks",
            "funding rounds",
            "partnerships",
            "acquisitions"
    );

    // -------------------------------------------------------------------------
    // Deterministic field builders (called from AIOutreachService, never LLM)
    // -------------------------------------------------------------------------

    public static boolean hasValidLinkedIn(Lead lead) {
        String li = lead.getDecisionMakerLinkedIn();
        return li != null
                && !li.isBlank()
                && !li.equalsIgnoreCase("unavailable")
                && !li.equalsIgnoreCase("n/a")
                && !li.equalsIgnoreCase("null");
    }

    /** Fully deterministic summary built from Lead fields only. */
    public static String buildDeterministicSummary(Lead lead) {
        StringBuilder sb = new StringBuilder();
        sb.append(lead.getCompanyName()).append(" is a ");
        sb.append(lead.getIndustry() != null ? lead.getIndustry() : "N/A");
        sb.append(" company");
        if (lead.getLocation() != null) {
            sb.append(" in ").append(lead.getLocation());
        }
        if (lead.getRevenue() != null) {
            sb.append(" with recorded revenue of $")
              .append(String.format("%,.0f", lead.getRevenue()));
        } else {
            sb.append(" with revenue not disclosed");
        }
        if (lead.getEmployeeCount() != null) {
            sb.append(" and ").append(lead.getEmployeeCount()).append(" employees");
        }
        sb.append(".");

        if (lead.getGrowthSignal() != null && !lead.getGrowthSignal().equalsIgnoreCase("None")) {
            sb.append(" The recorded growth signal is ").append(lead.getGrowthSignal()).append(".");
        }

        String dmName  = lead.getDecisionMakerName()  != null ? lead.getDecisionMakerName()  : null;
        String dmTitle = lead.getDecisionMakerTitle() != null ? lead.getDecisionMakerTitle() : null;
        if (dmName != null && dmTitle != null) {
            sb.append(" The recorded decision maker is ").append(dmName)
              .append(", ").append(dmTitle).append(".");
        } else if (dmName != null) {
            sb.append(" The recorded decision maker is ").append(dmName).append(".");
        }

        return sb.toString();
    }

    /** Deterministic channel based solely on LinkedIn URL presence. */
    public static String buildDeterministicChannel(Lead lead) {
        return hasValidLinkedIn(lead) ? "LinkedIn InMail" : "Email";
    }

    /** Deterministic risk notes from missing Lead fields. */
    public static String buildDeterministicRiskNotes(Lead lead) {
        List<String> gaps = new ArrayList<>();
        if (lead.getRevenue() == null) gaps.add("revenue is not disclosed");
        if (lead.getDecisionMakerName() == null || lead.getDecisionMakerName().equalsIgnoreCase("Unknown"))
            gaps.add("decision maker name is unknown");
        if (lead.getDecisionMakerTitle() == null || lead.getDecisionMakerTitle().isBlank())
            gaps.add("decision maker title is unknown");
        if (!hasValidLinkedIn(lead))
            gaps.add("LinkedIn profile is unavailable");
        if (lead.getIndustry() == null) gaps.add("industry is not recorded");
        if (lead.getGrowthSignal() == null) gaps.add("no growth signal recorded");

        if (gaps.isEmpty()) {
            return "All primary contact and company fields are populated.";
        }
        return "Data gaps noted: " + String.join("; ", gaps) + ".";
    }

    /** Safe, deterministic talking points as fallback when AI fields fail validation. */
    public static List<String> buildDeterministicTalkingPoints(Lead lead) {
        List<String> points = new ArrayList<>();

        if (lead.getGrowthSignal() != null && !lead.getGrowthSignal().equalsIgnoreCase("None")) {
            points.add("The recorded growth signal is " + lead.getGrowthSignal() + ".");
        } else {
            points.add("No specific growth signal is recorded for this company.");
        }

        String industry = lead.getIndustry() != null ? lead.getIndustry() : "this sector";
        points.add("The company operates in the " + industry + " industry.");

        String dmName  = lead.getDecisionMakerName()  != null ? lead.getDecisionMakerName()  : null;
        String dmTitle = lead.getDecisionMakerTitle() != null ? lead.getDecisionMakerTitle() : null;
        if (dmName != null && dmTitle != null) {
            points.add("The recorded decision maker is " + dmName + ", " + dmTitle + ".");
        } else if (dmName != null) {
            points.add("The recorded decision maker is " + dmName + ".");
        } else {
            points.add("No decision maker is recorded for this lead.");
        }

        return points;
    }

    // -------------------------------------------------------------------------
    // Validation of AI-generated fields only
    // -------------------------------------------------------------------------

    /**
     * Validates the three AI-generated fields (talkingPoints, personalizationAngle,
     * suggestedEmailOpening) against the prohibited patterns list.
     * summary, recommendedChannel, riskNotes are deterministic and never validated here.
     */
    public static boolean validateAiFields(List<String> talkingPoints,
                                           String personalizationAngle,
                                           String suggestedEmailOpening,
                                           Long leadId) {
        String combined = String.join(" ",
                talkingPoints != null ? String.join(" ", talkingPoints) : "",
                nullToEmpty(personalizationAngle),
                nullToEmpty(suggestedEmailOpening)
        ).toLowerCase(Locale.ROOT);

        for (String pattern : PROHIBITED_PATTERNS) {
            if (combined.contains(pattern)) {
                log.warn("Grounding validation failed for lead ID {}: AI field contained prohibited phrase '{}'",
                        leadId, pattern);
                return false;
            }
        }
        return true;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
