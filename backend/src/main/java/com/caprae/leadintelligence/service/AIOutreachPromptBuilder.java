package com.caprae.leadintelligence.service;

import com.caprae.leadintelligence.entity.Lead;
import org.springframework.stereotype.Component;

/**
 * Builds the Ollama prompt. Only used to generate the three creative language fields:
 * talkingPoints, personalizationAngle, suggestedEmailOpening.
 *
 * summary, recommendedChannel, riskNotes are generated deterministically in Java
 * by AIOutreachService and are never produced by the LLM.
 */
@Component
public class AIOutreachPromptBuilder {

    public String buildSystemPrompt() {
        return """
            You are a B2B sales assistant writing a cold outreach message from a verified lead data record.

            === WHAT YOU ARE GENERATING ===
            You produce ONLY these three JSON fields:
              - talkingPoints  : array of exactly 3 strings
              - personalizationAngle : exactly 1 string
              - suggestedEmailOpening : exactly 1-2 sentence string

            === THE ONLY SOURCE OF FACTS ===
            The FACT SHEET in the user message is the sole and complete source of facts.
            Every factual statement you write MUST trace directly to a field in that FACT SHEET.
            If a field says "Unknown" or "None Recorded", do NOT invent a value for it.

            === ABSOLUTE PROHIBITIONS — NEVER WRITE ANY OF THESE ===
            You MUST NOT mention, imply, or allude to any of the following:

            PRIOR CONTACT
              - previous conversation, prior conversation, as discussed, following up on,
                last time we spoke, our earlier call, per our last meeting

            AI / TECHNOLOGY ASSUMPTIONS
              - AI adoption, AI interest, AI initiatives, AI technologies, AI integration,
                AI solutions, leveraging AI, AI-powered, potential for AI, use of AI,
                technology adoption, technology solutions, technology problems,
                technology challenges, technology stack, digital transformation,
                tool adoption, software adoption, automation interest

            INTERNAL PROCESS / COMMUNICATION PROBLEMS
              - communication problems, communication challenges, internal communication,
                communication gap, process problems, workflow issues, operational challenges,
                integration problems, department challenges, team coordination, collaboration issues,
                productivity problems, efficiency issues, operational bottlenecks

            BUSINESS ASSUMPTIONS
              - business challenges, business problems, business needs, business goals,
                buying intent, budget, purchase intent, product interest, solution fit,
                pain points, recent investments, future plans, growth needs, growth strategy,
                scaling challenges, company priorities, company objectives, strategic initiative

            HIRING LANGUAGE
              - actively hiring (say only: "The recorded growth signal is [value]")
              - engineering expansion, team expansion, headcount growth

            UNVERIFIED CLAIMS ABOUT PEOPLE
              - decision-maker interests, goals, preferences, motivations, focus areas,
                key decision maker, champion, stakeholder priorities

            EXTERNAL DATA
              - market trends, industry benchmarks, competitor analysis, external statistics,
                funding rounds, recent investments, partnerships, acquisitions

            === QUESTION RULE ===
            If you want to reference something that is NOT explicitly in the FACT SHEET,
            you MUST frame it as a neutral open question that does NOT imply a problem exists.
            CORRECT: "Would it be useful to explore whether [offering] fits your current priorities?"
            WRONG:   "Given your communication challenges, our product could help."
            WRONG:   "As you scale your engineering team, you might need..."

            === OUTPUT FORMAT ===
            Output ONLY a raw valid JSON object. No markdown. No preamble. No explanation.
            Example structure:
            {
              "talkingPoints": ["...", "...", "..."],
              "personalizationAngle": "...",
              "suggestedEmailOpening": "..."
            }
            """;
    }

    public String buildFactSheet(Lead lead) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== GROUNDED LEAD FACT SHEET ===\n");
        sb.append("Company Name: ").append(lead.getCompanyName()).append("\n");
        sb.append("Website: ").append(lead.getWebsite() != null ? lead.getWebsite() : "N/A").append("\n");
        sb.append("Industry: ").append(lead.getIndustry() != null ? lead.getIndustry() : "N/A").append("\n");
        sb.append("Revenue: ").append(lead.getRevenue() != null
                ? "$" + String.format("%,.0f", lead.getRevenue()) : "Unknown / Not Disclosed").append("\n");
        sb.append("Employee Count: ").append(lead.getEmployeeCount() != null ? lead.getEmployeeCount() : "Unknown").append("\n");
        sb.append("Location: ").append(lead.getLocation() != null ? lead.getLocation() : "N/A").append("\n");
        sb.append("Growth Signal: ").append(lead.getGrowthSignal() != null ? lead.getGrowthSignal() : "None Recorded").append("\n");
        sb.append("Decision Maker Name: ").append(lead.getDecisionMakerName() != null ? lead.getDecisionMakerName() : "Unknown").append("\n");
        sb.append("Decision Maker Title: ").append(lead.getDecisionMakerTitle() != null ? lead.getDecisionMakerTitle() : "Unknown").append("\n");
        sb.append("Decision Maker LinkedIn: ").append(AIOutreachValidator.hasValidLinkedIn(lead)
                ? lead.getDecisionMakerLinkedIn() : "Unavailable").append("\n");
        sb.append("Deterministic Lead Score: ").append(lead.getScore() != null ? lead.getScore() : "Unscored").append("/100\n");
        sb.append("Deterministic Priority Level: ").append(lead.getPriority() != null ? lead.getPriority() : "N/A").append("\n");
        sb.append("=== END FACT SHEET ===");
        return sb.toString();
    }

    public String buildUserPrompt(Lead lead) {
        StringBuilder sb = new StringBuilder();
        sb.append("TASK: Generate a grounded outreach brief using ONLY the facts below.\n\n");
        sb.append(buildFactSheet(lead)).append("\n\n");
        sb.append("""
            REMINDER — PROHIBITED (do not use any of these regardless of how plausible they sound):
            previous conversation | AI adoption | AI interest | AI initiatives | AI technologies |
            communication problems | process problems | workflow issues | operational challenges |
            technology adoption | technology problems | integration problems | business challenges |
            business needs | buying intent | budget | pain points | growth needs | growth strategy |
            actively hiring | engineering expansion | team expansion | key decision maker |
            recent investments | future plans | product interest | stakeholder priorities

            Return ONLY a raw JSON object with exactly these three keys — no markdown, no extra text:
            {
              "talkingPoints": [
                "<Fact directly from FACT SHEET, or neutral open question>",
                "<Fact directly from FACT SHEET, or neutral open question>",
                "<Fact directly from FACT SHEET, or neutral open question>"
              ],
              "personalizationAngle": "<Neutral open question or conditional suggestion referencing only FACT SHEET data>",
              "suggestedEmailOpening": "<1-2 sentences. Cold introduction only. Zero references to prior contact or prior conversation.>"
            }
            """);
        return sb.toString();
    }
}
