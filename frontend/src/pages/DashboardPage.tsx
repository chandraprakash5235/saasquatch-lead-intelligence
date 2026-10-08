import React, { useState, useEffect, useCallback } from 'react';
import { message } from 'antd';
import { KPICards } from '../components/KPICards';
import { LeadTable } from '../components/LeadTable';
import { LeadDetailDrawer } from '../components/LeadDetailDrawer';
import { ImportModal } from '../components/ImportModal';
import { getLeads, Lead, PriorityLevel } from '../services/leadService';
import { KPIMetrics } from '../types/lead';

export const DashboardPage: React.FC = () => {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [pageSize] = useState<number>(20);

  // All-leads metrics (unpaged, for KPI computation)
  const [allLeads, setAllLeads] = useState<Lead[]>([]);

  const [industryFilter, setIndustryFilter] = useState<string | undefined>(undefined);
  const [growthSignalFilter, setGrowthSignalFilter] = useState<string | undefined>(undefined);
  const [priorityFilter, setPriorityFilter] = useState<PriorityLevel | undefined>(undefined);

  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);
  const [isImportModalOpen, setIsImportModalOpen] = useState<boolean>(false);

  // Fetch KPI summary from full dataset (no filters, large page size)
  const fetchAllLeadsForKPI = useCallback(async () => {
    try {
      const data = await getLeads({ page: 0, size: 1000, sortBy: 'score', direction: 'desc' });
      setAllLeads(data.content);
    } catch {
      // non-critical
    }
  }, []);

  const fetchLeads = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getLeads({
        page: currentPage,
        size: pageSize,
        industry: industryFilter,
        growthSignal: growthSignalFilter,
        priority: priorityFilter,
        sortBy: 'score',
        direction: 'desc',
      });
      setLeads(data.content);
      setTotalElements(data.totalElements);
    } catch (err: any) {
      console.error('Failed to fetch leads:', err);
      message.error('Failed to load leads from backend API');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, industryFilter, growthSignalFilter, priorityFilter]);

  useEffect(() => {
    fetchLeads();
  }, [fetchLeads]);

  useEffect(() => {
    fetchAllLeadsForKPI();
  }, [fetchAllLeadsForKPI]);

  // Compute real KPI metrics from the full unfiltered dataset
  const highPriorityCount = allLeads.filter((l) => l.priority === 'HIGH').length;
  const growthSignalCount = allLeads.filter(
    (l) => l.growthSignal && l.growthSignal !== 'None'
  ).length;
  const avgCompleteness =
    allLeads.length > 0
      ? Math.round(
          allLeads.reduce((acc, curr) => acc + (curr.dataCompleteness || 0), 0) / allLeads.length
        )
      : 0;

  const metrics: KPIMetrics = {
    totalLeads: allLeads.length > 0 ? allLeads.length : totalElements,
    highPriority: highPriorityCount,
    growthSignals: growthSignalCount,
    dataCompleteness: avgCompleteness,
  };

  const handleFilterChange = (
    industry?: string,
    growthSignal?: string,
    priority?: PriorityLevel
  ) => {
    setIndustryFilter(industry);
    setGrowthSignalFilter(growthSignal);
    setPriorityFilter(priority);
    setCurrentPage(0);
  };

  const handleImportSuccess = () => {
    fetchLeads();
    fetchAllLeadsForKPI();
  };

  return (
    <div className="dashboard-container">
      <KPICards metrics={metrics} loading={loading && allLeads.length === 0} />

      <LeadTable
        leads={leads}
        loading={loading}
        totalElements={totalElements}
        currentPage={currentPage}
        pageSize={pageSize}
        selectedIndustry={industryFilter}
        selectedGrowthSignal={growthSignalFilter}
        selectedPriority={priorityFilter}
        onPageChange={(page) => setCurrentPage(page)}
        onFilterChange={handleFilterChange}
        onSelectLead={(lead) => setSelectedLead(lead)}
        onOpenImportModal={() => setIsImportModalOpen(true)}
        onRefresh={handleImportSuccess}
      />

      <LeadDetailDrawer
        lead={selectedLead}
        open={!!selectedLead}
        onClose={() => setSelectedLead(null)}
      />

      <ImportModal
        open={isImportModalOpen}
        onClose={() => setIsImportModalOpen(false)}
        onImportSuccess={handleImportSuccess}
      />
    </div>
  );
};
