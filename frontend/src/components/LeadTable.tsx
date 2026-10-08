import React, { useState } from 'react';
import { Table, Tag, Button, Select, Space, Progress, Tooltip } from 'antd';
import {
  EyeOutlined,
  UploadOutlined,
  ReloadOutlined,
  RiseOutlined,
  FireOutlined,
  MinusCircleOutlined,
} from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { Lead, PriorityLevel } from '../types/lead';

interface LeadTableProps {
  leads: Lead[];
  loading: boolean;
  totalElements: number;
  currentPage: number;
  pageSize: number;
  selectedIndustry?: string;
  selectedGrowthSignal?: string;
  selectedPriority?: PriorityLevel;
  onPageChange: (page: number, pageSize: number) => void;
  onFilterChange: (industry?: string, growthSignal?: string, priority?: PriorityLevel) => void;
  onSelectLead: (lead: Lead) => void;
  onOpenImportModal: () => void;
  onRefresh: () => void;
}

const PRIORITY_COLOR: Record<PriorityLevel, string> = {
  HIGH: 'error',
  MEDIUM: 'warning',
  LOW: 'default',
};

const PRIORITY_ICON: Record<PriorityLevel, React.ReactNode> = {
  HIGH: <FireOutlined />,
  MEDIUM: <RiseOutlined />,
  LOW: <MinusCircleOutlined />,
};

export const LeadTable: React.FC<LeadTableProps> = ({
  leads,
  loading,
  totalElements,
  currentPage,
  pageSize,
  selectedIndustry,
  selectedGrowthSignal,
  selectedPriority,
  onPageChange,
  onFilterChange,
  onSelectLead,
  onOpenImportModal,
  onRefresh,
}) => {
  const columns: ColumnsType<Lead> = [
    {
      title: 'Company',
      dataIndex: 'companyName',
      key: 'companyName',
      render: (text: string, record: Lead) => (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span style={{ fontWeight: 700, color: '#0f172a', fontSize: '0.95rem' }}>{text}</span>
          <span style={{ fontSize: '0.78rem', color: '#64748b' }}>
            {record.location ? `📍 ${record.location}` : 'No location'}
            {record.decisionMakerName ? ` • ${record.decisionMakerName}` : ''}
          </span>
        </div>
      ),
    },
    {
      title: 'Industry',
      dataIndex: 'industry',
      key: 'industry',
      render: (industry?: string) => (
        <Tag color="blue" style={{ borderRadius: '6px', fontWeight: 600 }}>
          {industry || 'Unspecified'}
        </Tag>
      ),
    },
    {
      title: 'Revenue',
      dataIndex: 'revenue',
      key: 'revenue',
      render: (rev?: number) => {
        if (!rev && rev !== 0) return <span style={{ color: '#94a3b8' }}>N/A</span>;
        const formatted = rev >= 1000000
          ? `$${(rev / 1000000).toFixed(1)}M`
          : `$${(rev / 1000).toFixed(0)}K`;
        return <span style={{ fontWeight: 700, color: '#0f172a' }}>{formatted}</span>;
      },
    },
    {
      title: 'Employees',
      dataIndex: 'employeeCount',
      key: 'employeeCount',
      render: (emp?: number) => emp ?? <span style={{ color: '#94a3b8' }}>N/A</span>,
    },
    {
      title: 'Growth Signal',
      dataIndex: 'growthSignal',
      key: 'growthSignal',
      render: (signal?: string) => {
        if (!signal || signal === 'None') {
          return <Tag style={{ color: '#94a3b8', borderRadius: '6px' }}>None</Tag>;
        }
        return (
          <Tag color="green" icon={<RiseOutlined />} style={{ fontWeight: 600, borderRadius: '6px' }}>
            {signal}
          </Tag>
        );
      },
    },
    {
      title: 'Lead Score',
      dataIndex: 'score',
      key: 'score',
      sorter: true,
      render: (score?: number) => {
        if (score === undefined || score === null) {
          return <span style={{ color: '#94a3b8', fontSize: '0.85rem' }}>Unscored</span>;
        }
        return (
          <div style={{ width: 110 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 2 }}>
              <span style={{ fontWeight: 800, fontSize: '0.9rem', color: score >= 80 ? '#16a34a' : score >= 60 ? '#d97706' : '#64748b' }}>
                {score}
              </span>
              <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>/100</span>
            </div>
            <Progress
              percent={score}
              showInfo={false}
              strokeColor={score >= 80 ? '#10b981' : score >= 60 ? '#f59e0b' : '#94a3b8'}
              size="small"
            />
          </div>
        );
      },
    },
    {
      title: 'Priority',
      dataIndex: 'priority',
      key: 'priority',
      render: (priority?: PriorityLevel) => {
        if (!priority) return <Tag style={{ borderRadius: '6px' }}>Unknown</Tag>;
        return (
          <Tag
            color={PRIORITY_COLOR[priority]}
            icon={PRIORITY_ICON[priority]}
            style={{ fontWeight: 700, borderRadius: '6px' }}
          >
            {priority}
          </Tag>
        );
      },
    },
    {
      title: 'Action',
      key: 'action',
      render: (_, record: Lead) => (
        <Button
          type="primary"
          ghost
          size="small"
          icon={<EyeOutlined />}
          onClick={() => onSelectLead(record)}
          style={{ borderRadius: '6px', fontWeight: 600 }}
        >
          Why this lead?
        </Button>
      ),
    },
  ];

  const handleTableChange = (pagination: TablePaginationConfig) => {
    if (pagination.current && pagination.pageSize) {
      onPageChange(pagination.current - 1, pagination.pageSize);
    }
  };

  return (
    <div className="table-card">
      <div className="table-header-section">
        <div>
          <div className="table-title">
            Lead Intelligence Data
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', margin: '4px 0 6px', fontSize: '0.78rem', fontWeight: 700, color: '#4338ca' }}>
            <span style={{ background: '#e0e7ff', padding: '2px 8px', borderRadius: '4px' }}>1. Find</span>
            <span>→</span>
            <span style={{ background: '#e0e7ff', padding: '2px 8px', borderRadius: '4px' }}>2. Prioritize</span>
            <span>→</span>
            <span style={{ background: '#e0e7ff', padding: '2px 8px', borderRadius: '4px' }}>3. Understand</span>
            <span>→</span>
            <span style={{ background: '#e0e7ff', padding: '2px 8px', borderRadius: '4px' }}>4. Act</span>
          </div>
          <p className="table-subtitle">
            Sorted by lead score (highest value first) — click <strong>"Why this lead?"</strong> to inspect explainable scoring & AI briefs
          </p>
        </div>

        <Space wrap>
          <Select
            placeholder="Filter by Industry"
            allowClear
            value={selectedIndustry}
            onChange={(val) => onFilterChange(val, selectedGrowthSignal, selectedPriority)}
            style={{ width: 180 }}
          >
            <Select.Option value="Manufacturing">Manufacturing</Select.Option>
            <Select.Option value="Services">Services</Select.Option>
            <Select.Option value="Healthcare">Healthcare</Select.Option>
            <Select.Option value="Technology">Technology</Select.Option>
            <Select.Option value="Logistics">Logistics</Select.Option>
          </Select>

          <Select
            placeholder="Priority"
            allowClear
            value={selectedPriority}
            onChange={(val) => onFilterChange(selectedIndustry, selectedGrowthSignal, val)}
            style={{ width: 130 }}
          >
            <Select.Option value="HIGH">🔴 HIGH</Select.Option>
            <Select.Option value="MEDIUM">🟡 MEDIUM</Select.Option>
            <Select.Option value="LOW">⚪ LOW</Select.Option>
          </Select>

          <Select
            placeholder="Growth Signal"
            allowClear
            value={selectedGrowthSignal}
            onChange={(val) => onFilterChange(selectedIndustry, val, selectedPriority)}
            style={{ width: 160 }}
          >
            <Select.Option value="Hiring">Hiring</Select.Option>
            <Select.Option value="Expansion">Expansion</Select.Option>
            <Select.Option value="New Location">New Location</Select.Option>
            <Select.Option value="Funding">Funding</Select.Option>
            <Select.Option value="None">None</Select.Option>
          </Select>

          <Tooltip title="Refresh">
            <Button icon={<ReloadOutlined />} onClick={onRefresh} loading={loading} />
          </Tooltip>

          <Button
            type="primary"
            icon={<UploadOutlined />}
            onClick={onOpenImportModal}
            style={{ borderRadius: '8px', fontWeight: 600 }}
          >
            Import Leads
          </Button>
        </Space>
      </div>

      <Table
        columns={columns}
        dataSource={leads}
        rowKey="id"
        loading={loading}
        pagination={{
          current: currentPage + 1,
          pageSize: pageSize,
          total: totalElements,
          showSizeChanger: false,
          showTotal: (total) => `${total} leads total`,
        }}
        onChange={handleTableChange}
        rowClassName={(record) =>
          record.priority === 'HIGH' ? 'high-priority-row' : ''
        }
        locale={{
          emptyText:
            'No leads found. Click "Import Leads" to upload sample-data/leads.csv to begin.',
        }}
      />
    </div>
  );
};
