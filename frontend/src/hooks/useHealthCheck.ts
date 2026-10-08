import { useState, useEffect, useCallback } from 'react';
import { checkBackendHealth } from '../services/api';
import { HealthResponse } from '../types/lead';

export interface UseHealthCheckResult {
  health: HealthResponse | null;
  loading: boolean;
  isOnline: boolean;
  error: string | null;
  refetch: () => void;
}

export function useHealthCheck(pollIntervalMs = 15000): UseHealthCheckResult {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isOnline, setIsOnline] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchHealth = useCallback(async () => {
    setLoading(true);
    try {
      const data = await checkBackendHealth();
      setHealth(data);
      setIsOnline(data.status === 'UP');
      setError(null);
    } catch (err) {
      setIsOnline(false);
      setHealth(null);
      setError(err instanceof Error ? err.message : 'Backend unreachable');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHealth();

    const interval = setInterval(() => {
      fetchHealth();
    }, pollIntervalMs);

    return () => clearInterval(interval);
  }, [fetchHealth, pollIntervalMs]);

  return { health, loading, isOnline, error, refetch: fetchHealth };
}
