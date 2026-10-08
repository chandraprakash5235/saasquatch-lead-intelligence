import React from 'react';
import { Badge, Tooltip, Tag } from 'antd';
import { ApiOutlined, CheckCircleOutlined, DisconnectOutlined, ReloadOutlined } from '@ant-design/icons';
import { HealthResponse } from '../types/lead';

interface HeaderProps {
  health: HealthResponse | null;
  isOnline: boolean;
  loading: boolean;
  error: string | null;
  onRefresh: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  health,
  isOnline,
  loading,
  error,
  onRefresh,
}) => {
  return (
    <header className="app-header">
      <div className="header-container">
        <div>
          <div className="brand-badge">
            <ApiOutlined /> Caprae Capital AI Platform
          </div>
          <h1 className="header-title">SaaSquatch Lead Intelligence</h1>
          <p className="header-subtitle">Find the leads worth your attention — and understand why.</p>
        </div>

        <div className="backend-status-card">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
            <span style={{ fontSize: '0.75rem', color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase' }}>
              Backend Status
            </span>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span className={`status-indicator ${isOnline ? 'online' : 'offline'}`} />
              <span style={{ color: '#ffffff', fontWeight: 700, fontSize: '0.9rem' }}>
                {isOnline ? 'Connected' : 'Offline'}
              </span>
              {isOnline && health && (
                <Tag color="success" style={{ borderRadius: '12px', margin: 0 }}>
                  {health.service}
                </Tag>
              )}
            </div>
          </div>

          <Tooltip title={isOnline ? 'Backend healthy on port 8080' : error || 'Click to reconnect'}>
            <button
              onClick={onRefresh}
              style={{
                background: 'rgba(255,255,255,0.1)',
                border: '1px solid rgba(255,255,255,0.2)',
                borderRadius: '8px',
                color: '#fff',
                padding: '6px 10px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
                transition: 'all 0.2s',
              }}
            >
              <ReloadOutlined spin={loading} />
            </button>
          </Tooltip>
        </div>
      </div>
    </header>
  );
};
