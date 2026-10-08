import React from 'react';
import { ConfigProvider } from 'antd';
import { Header } from '../components/Header';
import { DashboardPage } from '../pages/DashboardPage';
import { useHealthCheck } from '../hooks/useHealthCheck';

export const MainLayout: React.FC = () => {
  const { health, loading, isOnline, error, refetch } = useHealthCheck();

  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#4f46e5',
          fontFamily: "'Plus Jakarta Sans', sans-serif",
          borderRadius: 8,
        },
      }}
    >
      <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: '#f8fafc' }}>
        <Header
          health={health}
          isOnline={isOnline}
          loading={loading}
          error={error}
          onRefresh={refetch}
        />
        <main style={{ flex: 1 }}>
          <DashboardPage />
        </main>
        <footer
          style={{
            textAlign: 'center',
            padding: '1.5rem',
            color: '#64748b',
            fontSize: '0.85rem',
            borderTop: '1px solid #e2e8f0',
            background: '#ffffff',
          }}
        >
          SaaSquatch Lead Intelligence &copy; {new Date().getFullYear()} Caprae Capital AI-Readiness Challenge. All rights reserved.
        </footer>
      </div>
    </ConfigProvider>
  );
};
