import React, { useState, useEffect } from 'react';
import {
  Drawer, Tag, Progress, Descriptions, Divider, Typography,
  Button, Spin, Space, Alert, Card, message,
} from 'antd';
import {
  GlobalOutlined,
  CalendarOutlined,
  InfoCircleOutlined,
  FireOutlined,
  RiseOutlined,
  MinusCircleOutlined,
  ThunderboltOutlined,
  CheckCircleOutlined,
  RobotOutlined,
  CopyOutlined,
  WarningOutlined,
  SendOutlined,
} from '@ant-design/icons';
import { Lead, LeadScoreResult, PriorityLevel } from '../types/lead';
import { getLeadScore, generateOutreachBrief, OutreachBrief } from '../services/leadService';

const { Text, Title, Paragraph } = Typography;

interface LeadDetailDrawerProps {
  lead: Lead | null;
  open: boolean;
  onClose: () => void;
}

const PRIORITY_COLOR: Record<PriorityLevel, string> = {
  HIGH: '#dc2626',
  MEDIUM: '#d97706',
  LOW: '#64748b',
};

const PRIORITY_BG: Record<PriorityLevel, string> = {
  HIGH: '#fef2f2',
  MEDIUM: '#fffbeb',
  LOW: '#f8fafc',
};

const PRIORITY_ICON: Record<PriorityLevel, React.ReactNode> = {
  HIGH: <FireOutlined />,
  MEDIUM: <RiseOutlined />,
  LOW: <MinusCircleOutlined />,
};

interface ScoreRowProps {
  label: string;
  score: number;
  maxScore: number;
}

const ScoreRow: React.FC<ScoreRowProps> = ({ label, score, maxScore }) => (
  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
    <div style={{ width: 150, fontSize: '0.82rem', color: '#475569', fontWeight: 600 }}>{label}</div>
    <Progress
      percent={(score / maxScore) * 100}
      showInfo={false}
      strokeColor={score === maxScore ? '#10b981' : score > 0 ? '#4f46e5' : '#e2e8f0'}
      style={{ flex: 1, margin: 0 }}
      size="small"
    />
    <div style={{ width: 60, textAlign: 'right', fontSize: '0.82rem', fontWeight: 700, color: '#0f172a' }}>
      {score} / {maxScore}
    </div>
  </div>
);

export const LeadDetailDrawer: React.FC<LeadDetailDrawerProps> = ({ lead, open, onClose }) => {
  const [scoreResult, setScoreResult] = useState<LeadScoreResult | null>(null);
  const [scoreLoading, setScoreLoading] = useState(false);

  // Phase 4 AI Outreach Brief states
  const [brief, setBrief] = useState<OutreachBrief | null>(null);
  const [briefLoading, setBriefLoading] = useState(false);
  const [briefError, setBriefError] = useState<string | null>(null);

  useEffect(() => {
    if (open && lead?.id) {
      setScoreResult(null);
      setBrief(null);
      setBriefError(null);
      setScoreLoading(true);

      getLeadScore(lead.id)
        .then((result) => setScoreResult(result))
        .catch((err) => console.error('Failed to fetch score:', err))
        .finally(() => setScoreLoading(false));
    }
  }, [open, lead?.id]);

  const handleGenerateBrief = async () => {
    if (!lead?.id) return;
    setBriefLoading(true);
    setBriefError(null);

    try {
      const data = await generateOutreachBrief(lead.id);
      setBrief(data);
      message.success('AI Outreach Brief generated!');
    } catch (err: any) {
      const rawMsg = err.response?.data?.message || err.message || '';
      let userFriendlyMsg = 'The AI outreach service is temporarily unavailable.';
      if (err.response?.status === 503 || rawMsg.includes('not configured')) {
        userFriendlyMsg = 'AI outreach is not configured. Set OPENAI_API_KEY to enable live briefs.';
      } else if (rawMsg.toLowerCase().includes('timeout') || err.code === 'ECONNABORTED') {
        userFriendlyMsg = 'The request took too long. Please try again.';
      }
      setBriefError(userFriendlyMsg);
    } finally {
      setBriefLoading(false);
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    message.success('Message copied to clipboard');
  };

  if (!lead) return null;

  const priority = scoreResult?.priority ?? lead.priority;
  const totalScore = scoreResult?.totalScore ?? lead.score;
  const formattedRevenue = lead.revenue ? `$${Number(lead.revenue).toLocaleString()}` : 'N/A';
  const formattedDate = lead.createdAt ? new Date(lead.createdAt).toLocaleString() : 'N/A';

  return (
    <Drawer
      title={
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>

          <div>
            <div style={{ fontWeight: 800, fontSize: '1.05rem', color: '#0f172a' }}>
              {lead.companyName}
            </div>
            {lead.industry && (
              <Tag color="blue" style={{ borderRadius: '6px', marginTop: 2 }}>
                {lead.industry}
              </Tag>
            )}
          </div>
        </div>
      }
      placement="right"
      width={520}
      onClose={() => {
        onClose();
        setScoreResult(null);
        setBrief(null);
        setBriefError(null);
      }}
      open={open}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>

        {/* ── Score Hero Card ── */}
        {priority && totalScore !== undefined && (
          <div
            style={{
              background: `linear-gradient(135deg, ${PRIORITY_BG[priority]} 0%, #f8fafc 100%)`,
              border: `1px solid ${PRIORITY_COLOR[priority]}30`,
              borderRadius: '14px',
              padding: '16px 20px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <Text type="secondary" style={{ fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 700 }}>
                  Deterministic Lead Score
                </Text>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: '6px', marginTop: '4px' }}>
                  <span style={{ fontSize: '2.5rem', fontWeight: 900, color: PRIORITY_COLOR[priority] }}>
                    {totalScore}
                  </span>
                  <span style={{ color: '#94a3b8', fontSize: '1rem' }}>/100</span>
                </div>
                <Tag
                  icon={PRIORITY_ICON[priority]}
                  style={{
                    background: PRIORITY_COLOR[priority],
                    color: '#ffffff',
                    border: 'none',
                    fontWeight: 700,
                    borderRadius: '20px',
                    padding: '0 12px',
                    marginTop: '4px',
                  }}
                >
                  {priority} PRIORITY
                </Tag>
              </div>
              <Progress
                type="circle"
                percent={totalScore}
                width={80}
                strokeColor={PRIORITY_COLOR[priority]}
                format={(pct) => (
                  <span style={{ fontWeight: 800, color: PRIORITY_COLOR[priority], fontSize: '1rem' }}>
                    {pct}
                  </span>
                )}
              />
            </div>
          </div>
        )}

        {/* ── Why This Lead? ── */}
        <div
          style={{
            background: '#f8fafc',
            borderRadius: '12px',
            border: '1px solid #e2e8f0',
            padding: '16px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
            <ThunderboltOutlined style={{ color: '#4f46e5', fontSize: '1rem' }} />
            <Title level={5} style={{ margin: 0, color: '#0f172a' }}>
              Why this lead?
            </Title>
          </div>

          {scoreLoading ? (
            <div style={{ textAlign: 'center', padding: '20px' }}>
              <Spin size="small" />
              <div style={{ color: '#94a3b8', marginTop: 8, fontSize: '0.85rem' }}>
                Calculating score breakdown...
              </div>
            </div>
          ) : scoreResult ? (
            <>
              <ScoreRow label="ICP Fit" score={scoreResult.icpFitScore} maxScore={30} />
              <ScoreRow label="Revenue Fit" score={scoreResult.revenueFitScore} maxScore={20} />
              <ScoreRow label="Employee Fit" score={scoreResult.employeeFitScore} maxScore={15} />
              <ScoreRow label="Growth Signal" score={scoreResult.growthSignalScore} maxScore={15} />
              <ScoreRow label="Decision Maker" score={scoreResult.decisionMakerScore} maxScore={10} />
              <ScoreRow label="Data Completeness" score={scoreResult.dataCompletenessScore} maxScore={10} />

              <Divider style={{ margin: '14px 0 10px' }} />

              <div>
                <Text
                  style={{
                    fontSize: '0.75rem',
                    textTransform: 'uppercase',
                    fontWeight: 700,
                    color: '#475569',
                    display: 'block',
                    marginBottom: '8px',
                  }}
                >
                  Score Reasons
                </Text>
                {scoreResult.reasons.map((reason, i) => (
                  <div
                    key={i}
                    style={{
                      display: 'flex',
                      alignItems: 'flex-start',
                      gap: '8px',
                      marginBottom: '6px',
                      fontSize: '0.85rem',
                      color: '#1e293b',
                    }}
                  >
                    <CheckCircleOutlined style={{ color: '#10b981', marginTop: 2, flexShrink: 0 }} />
                    <span>{reason}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <Text type="secondary" style={{ fontSize: '0.85rem' }}>
              Score breakdown unavailable.
            </Text>
          )}
        </div>

        {/* ── Phase 4: AI Outreach Brief Section ── */}
        <div
          style={{
            background: 'linear-gradient(180deg, #f5f3ff 0%, #ffffff 100%)',
            borderRadius: '12px',
            border: '1px solid #c7d2fe',
            padding: '16px',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <RobotOutlined style={{ color: '#4f46e5', fontSize: '1.2rem' }} />
              <Title level={5} style={{ margin: 0, color: '#1e1b4b' }}>
                AI Outreach Brief
              </Title>
            </div>
            <Tag color="purple" style={{ borderRadius: '12px', fontWeight: 600 }}>
              Grounded in recorded lead data
            </Tag>
          </div>

          {!brief && !briefLoading && (
            <div style={{ textAlign: 'center', padding: '12px 0' }}>
              <Text type="secondary" style={{ display: 'block', marginBottom: '12px', fontSize: '0.85rem' }}>
                Generate a personalized outreach strategy based on recorded lead facts.
              </Text>
              <Button
                type="primary"
                icon={<RobotOutlined />}
                loading={briefLoading}
                onClick={handleGenerateBrief}
                style={{
                  background: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
                  border: 'none',
                  borderRadius: '8px',
                  fontWeight: 600,
                }}
              >
                Generate AI Outreach Brief
              </Button>
            </div>
          )}

          {briefLoading && (
            <div style={{ textAlign: 'center', padding: '24px 0' }}>
              <Spin size="large" />
              <div style={{ color: '#4f46e5', fontWeight: 600, marginTop: '12px', fontSize: '0.9rem' }}>
                Generating grounded sales brief...
              </div>
              <Text type="secondary" style={{ fontSize: '0.8rem' }}>
                Analyzing growth signals, decision maker title & ICP alignment
              </Text>
            </div>
          )}

          {briefError && (
            <div style={{ marginTop: '8px' }}>
              <Alert
                message="AI Brief Unavailable"
                description={briefError}
                type="warning"
                showIcon
                action={
                  <Button size="small" type="primary" onClick={handleGenerateBrief}>
                    Retry
                  </Button>
                }
              />
            </div>
          )}

          {brief && !briefLoading && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '8px' }}>

              {/* Executive Summary */}
              <Card size="small" style={{ background: '#ffffff', borderRadius: '8px' }}>
                <Text style={{ fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 700, color: '#6366f1' }}>
                  Executive Summary
                </Text>
                <Paragraph style={{ margin: '4px 0 0', fontSize: '0.85rem', color: '#1e293b' }}>
                  {brief.summary}
                </Paragraph>
              </Card>

              {/* Recommended Channel & Personalization Angle */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                <Card size="small" style={{ background: '#ffffff', borderRadius: '8px' }}>
                  <Text style={{ fontSize: '0.72rem', textTransform: 'uppercase', fontWeight: 700, color: '#475569' }}>
                    Recommended Channel
                  </Text>
                  <div style={{ marginTop: 4 }}>
                    <Tag color="blue" icon={<SendOutlined />}>
                      {brief.recommendedChannel}
                    </Tag>
                  </div>
                </Card>

                <Card size="small" style={{ background: '#ffffff', borderRadius: '8px' }}>
                  <Text style={{ fontSize: '0.72rem', textTransform: 'uppercase', fontWeight: 700, color: '#475569' }}>
                    Hook Angle
                  </Text>
                  <div style={{ marginTop: 4, fontSize: '0.8rem', color: '#0f172a', fontWeight: 600 }}>
                    {brief.personalizationAngle}
                  </div>
                </Card>
              </div>

              {/* Key Talking Points */}
              <Card size="small" style={{ background: '#ffffff', borderRadius: '8px' }}>
                <Text style={{ fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 700, color: '#6366f1' }}>
                  Key Talking Points
                </Text>
                <ul style={{ paddingLeft: '18px', margin: '6px 0 0', fontSize: '0.85rem', color: '#334155' }}>
                  {brief.talkingPoints?.map((point, idx) => (
                    <li key={idx} style={{ marginBottom: '4px' }}>{point}</li>
                  ))}
                </ul>
              </Card>

              {/* Email Opening Line */}
              <Card size="small" style={{ background: '#f8fafc', borderRadius: '8px', borderLeft: '4px solid #6366f1' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                  <Text style={{ fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 700, color: '#4338ca' }}>
                    Suggested Email Opening
                  </Text>
                  <Button
                    type="text"
                    size="small"
                    icon={<CopyOutlined />}
                    onClick={() => copyToClipboard(brief.suggestedEmailOpening)}
                  >
                    Copy
                  </Button>
                </div>
                <Text italic style={{ fontSize: '0.85rem', color: '#1e293b' }}>
                  "{brief.suggestedEmailOpening}"
                </Text>
              </Card>

              {/* Risk & Caution Notes */}
              {brief.riskNotes && (
                <Alert
                  message="Risk & Data Gap Notes"
                  description={brief.riskNotes}
                  type="info"
                  showIcon
                  icon={<WarningOutlined />}
                  style={{ borderRadius: '8px', fontSize: '0.8rem' }}
                />
              )}

              <div style={{ textAlign: 'right', marginTop: '4px' }}>
                <Button size="small" icon={<RobotOutlined />} onClick={handleGenerateBrief}>
                  Regenerate Brief
                </Button>
              </div>
            </div>
          )}
        </div>

        <Divider style={{ margin: '4px 0' }} />

        {/* ── Company Intelligence ── */}
        <Descriptions title="Company Intelligence" column={1} bordered size="small">
          <Descriptions.Item label="Company">{lead.companyName}</Descriptions.Item>
          <Descriptions.Item label="Industry">{lead.industry || 'N/A'}</Descriptions.Item>
          <Descriptions.Item label="Annual Revenue">{formattedRevenue}</Descriptions.Item>
          <Descriptions.Item label="Employees">{lead.employeeCount ?? 'N/A'}</Descriptions.Item>
          <Descriptions.Item label="Location">{lead.location || 'N/A'}</Descriptions.Item>
          <Descriptions.Item label="Growth Signal">
            <Tag color={lead.growthSignal && lead.growthSignal !== 'None' ? 'green' : 'default'}>
              {lead.growthSignal || 'None'}
            </Tag>
          </Descriptions.Item>
        </Descriptions>

        <Descriptions title="Decision Maker" column={1} bordered size="small">
          <Descriptions.Item label="Name">{lead.decisionMakerName || 'N/A'}</Descriptions.Item>
          <Descriptions.Item label="Title">{lead.decisionMakerTitle || 'N/A'}</Descriptions.Item>
          <Descriptions.Item label="LinkedIn">
            {lead.decisionMakerLinkedIn ? (
              <a href={lead.decisionMakerLinkedIn} target="_blank" rel="noreferrer">
                <GlobalOutlined /> View Profile
              </a>
            ) : (
              'N/A'
            )}
          </Descriptions.Item>
        </Descriptions>

        <Descriptions title="Record Metadata" column={1} bordered size="small">
          <Descriptions.Item label="Created At">
            <CalendarOutlined style={{ marginRight: 6 }} />
            {formattedDate}
          </Descriptions.Item>
        </Descriptions>
      </div>
    </Drawer>
  );
};
