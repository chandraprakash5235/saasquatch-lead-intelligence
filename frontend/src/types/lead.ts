export type PriorityLevel = 'HIGH' | 'MEDIUM' | 'LOW';

export interface Lead {
  id: number;
  companyName: string;
  website?: string;
  industry?: string;
  revenue?: number;
  employeeCount?: number;
  location?: string;
  growthSignal?: string;
  decisionMakerName?: string;
  decisionMakerTitle?: string;
  decisionMakerLinkedIn?: string;
  dataCompleteness: number;
  score?: number;
  priority?: PriorityLevel;
  createdAt?: string;
  updatedAt?: string;
}

export interface HealthResponse {
  status: string;
  service: string;
}

export interface KPIMetrics {
  totalLeads: number;
  highPriority: number | string;
  growthSignals: number;
  dataCompleteness: number;
}

export interface ScoreBreakdown {
  icpFit: number;
  revenueFit: number;
  employeeFit: number;
  growthSignal: number;
  decisionMaker: number;
  dataCompleteness: number;
}

export interface LeadScoreResult {
  leadId: number;
  companyName: string;
  totalScore: number;
  icpFitScore: number;
  revenueFitScore: number;
  employeeFitScore: number;
  growthSignalScore: number;
  decisionMakerScore: number;
  dataCompletenessScore: number;
  priority: PriorityLevel;
  reasons: string[];
  breakdown: ScoreBreakdown;
}

export interface OutreachBrief {
  leadId: number;
  companyName: string;
  summary: string;
  talkingPoints: string[];
  recommendedChannel: string;
  personalizationAngle: string;
  suggestedEmailOpening: string;
  riskNotes: string;
}
