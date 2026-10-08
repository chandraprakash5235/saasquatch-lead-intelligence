import React from 'react';
import {
  TeamOutlined,
  FireOutlined,
  RiseOutlined,
  BarChartOutlined,
} from '@ant-design/icons';
import { KPIMetrics } from '../types/lead';

interface KPICardsProps {
  metrics: KPIMetrics;
  loading?: boolean;
}

export const KPICards: React.FC<KPICardsProps> = ({ metrics, loading }) => {
  return (
    <div className="kpi-grid">
      {/* 1. Total Leads */}
      <div className="kpi-card">
        <div className="kpi-header">
          <span className="kpi-title">Total Leads</span>
          <div className="kpi-icon-wrapper" style={{ background: '#eef2ff', color: '#4f46e5' }}>
            <TeamOutlined />
          </div>
        </div>
        <div className="kpi-value">{loading ? '—' : metrics.totalLeads}</div>
        <div className="kpi-footer">
          <span>Active sales pipeline prospects</span>
        </div>
      </div>

      {/* 2. High Priority */}
      <div className="kpi-card">
        <div className="kpi-header">
          <span className="kpi-title">High Priority</span>
          <div className="kpi-icon-wrapper" style={{ background: '#fef2f2', color: '#dc2626' }}>
            <FireOutlined />
          </div>
        </div>
        <div className="kpi-value" style={{ color: '#dc2626' }}>
          {loading ? '—' : metrics.highPriority}
        </div>
        <div className="kpi-footer" style={{ color: '#dc2626' }}>
          <span>Score ≥ 80 — requires immediate outreach</span>
        </div>
      </div>

      {/* 3. Growth Signals */}
      <div className="kpi-card">
        <div className="kpi-header">
          <span className="kpi-title">Growth Signals</span>
          <div className="kpi-icon-wrapper" style={{ background: '#f0fdf4', color: '#16a34a' }}>
            <RiseOutlined />
          </div>
        </div>
        <div className="kpi-value">{loading ? '—' : metrics.growthSignals}</div>
        <div className="kpi-footer" style={{ color: '#16a34a' }}>
          <span>Hiring, expansion & new locations</span>
        </div>
      </div>

      {/* 4. Avg Data Completeness */}
      <div className="kpi-card">
        <div className="kpi-header">
          <span className="kpi-title">Avg. Data Completeness</span>
          <div className="kpi-icon-wrapper" style={{ background: '#faf5ff', color: '#9333ea' }}>
            <BarChartOutlined />
          </div>
        </div>
        <div className="kpi-value">{loading ? '—' : `${metrics.dataCompleteness}%`}</div>
        <div className="kpi-footer">
          <span>Avg profile metadata score</span>
        </div>
      </div>
    </div>
  );
};
