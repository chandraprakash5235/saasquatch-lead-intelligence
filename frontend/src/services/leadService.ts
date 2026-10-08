import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

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

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface LeadImportResponse {
  totalRows: number;
  successfulRows: number;
  failedRows: number;
  errors: string[];
}

export interface GetLeadsParams {
  industry?: string;
  location?: string;
  growthSignal?: string;
  minScore?: number;
  priority?: PriorityLevel;
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: string;
}

/**
 * Fetch paginated list of leads with optional filtering and sorting.
 */
export async function getLeads(params: GetLeadsParams = {}): Promise<PagedResponse<Lead>> {
  const response = await apiClient.get<PagedResponse<Lead>>('/leads', { params });
  return response.data;
}

/**
 * Fetch single lead details by ID.
 */
export async function getLeadById(id: number): Promise<Lead> {
  const response = await apiClient.get<Lead>(`/leads/${id}`);
  return response.data;
}

/**
 * Fetch detailed explainable score result for a lead.
 */
export async function getLeadScore(id: number): Promise<LeadScoreResult> {
  const response = await apiClient.get<LeadScoreResult>(`/leads/${id}/score`);
  return response.data;
}

/**
 * Recalculate scores for all database leads.
 */
export async function recalculateScores(): Promise<{ recalculatedCount: number; status: string }> {
  const response = await apiClient.post<{ recalculatedCount: number; status: string }>('/leads/recalculate-scores');
  return response.data;
}

export async function importLeadsCsv(file: File): Promise<LeadImportResponse> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post<LeadImportResponse>('/leads/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
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

/**
 * Generate AI Outreach Brief for a given lead ID.
 */
export async function generateOutreachBrief(id: number): Promise<OutreachBrief> {
  const response = await apiClient.post<OutreachBrief>(`/leads/${id}/outreach`);
  return response.data;
}
