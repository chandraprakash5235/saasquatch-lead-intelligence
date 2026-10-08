import { HealthResponse, Lead, KPIMetrics } from '../types/lead';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

/**
 * Fetch backend health status
 */
export async function checkBackendHealth(): Promise<HealthResponse> {
  try {
    const response = await fetch(`${API_BASE_URL}/health`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error(`HTTP error status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Failed to fetch health check:', error);
    throw error;
  }
}

/**
 * Mock leads data matching the sample CSV dataset for Phase 1 UI
 */
export const MOCK_LEADS: Lead[] = [
  {
    id: '1',
    companyName: 'ABC Manufacturing',
    industry: 'Manufacturing',
    revenue: 8200000,
    employees: 72,
    location: 'Texas',
    growthSignal: 'Hiring',
    decisionMaker: 'John Smith',
    leadScore: 85,
    priority: 'High',
    completeness: 100,
  },
  {
    id: '2',
    companyName: 'XYZ Services',
    industry: 'Services',
    revenue: 5100000,
    employees: 48,
    location: 'Florida',
    growthSignal: 'Expansion',
    decisionMaker: 'Sarah Jones',
    leadScore: 72,
    priority: 'Medium',
    completeness: 90,
  },
  {
    id: '3',
    companyName: 'Delta Health',
    industry: 'Healthcare',
    revenue: 3400000,
    employees: 31,
    location: 'Ohio',
    growthSignal: 'None',
    decisionMaker: 'Mark Brown',
    leadScore: 45,
    priority: 'Low',
    completeness: 85,
  },
  {
    id: '4',
    companyName: 'Nova Technology',
    industry: 'Technology',
    revenue: 12500000,
    employees: 95,
    location: 'California',
    growthSignal: 'Hiring',
    decisionMaker: 'Emily Davis',
    leadScore: 94,
    priority: 'High',
    completeness: 100,
  },
  {
    id: '5',
    companyName: 'Prime Logistics',
    industry: 'Logistics',
    revenue: 6800000,
    employees: 64,
    location: 'Illinois',
    growthSignal: 'New Location',
    decisionMaker: 'Michael Wilson',
    leadScore: 78,
    priority: 'Medium',
    completeness: 95,
  },
];

export const MOCK_KPI_METRICS: KPIMetrics = {
  totalLeads: 5,
  highPriority: 2,
  growthSignals: 4,
  dataCompleteness: 94,
};
