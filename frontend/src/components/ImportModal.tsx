import React, { useState } from 'react';
import { Modal, Upload, Button, message, Alert, List, Typography, Space } from 'antd';
import { UploadOutlined, FileTextOutlined, CheckCircleOutlined, WarningOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd/es/upload/interface';
import { importLeadsCsv, LeadImportResponse } from '../services/leadService';

const { Text, Title } = Typography;

interface ImportModalProps {
  open: boolean;
  onClose: () => void;
  onImportSuccess: () => void;
}

export const ImportModal: React.FC<ImportModalProps> = ({
  open,
  onClose,
  onImportSuccess,
}) => {
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [uploading, setUploading] = useState(false);
  const [result, setResult] = useState<LeadImportResponse | null>(null);

  const handleUpload = async () => {
    if (fileList.length === 0) {
      message.warning('Please select a .csv file to import.');
      return;
    }

    const file = fileList[0].originFileObj as File;
    setUploading(true);
    setResult(null);

    try {
      const res = await importLeadsCsv(file);
      setResult(res);

      if (res.successfulRows > 0) {
        message.success(`Successfully imported ${res.successfulRows} lead(s)!`);
        onImportSuccess();
      } else {
        message.warning('Import completed with warnings or no valid rows.');
      }
    } catch (err: any) {
      console.error('Import failed:', err);
      const errorMsg = err.response?.data?.message || err.message || 'CSV Import failed.';
      message.error(errorMsg);
    } finally {
      setUploading(false);
    }
  };

  const handleModalClose = () => {
    setFileList([]);
    setResult(null);
    onClose();
  };

  return (
    <Modal
      title={
        <Space>
          <UploadOutlined style={{ color: '#4f46e5' }} />
          <span>Import Leads from CSV</span>
        </Space>
      }
      open={open}
      onCancel={handleModalClose}
      footer={[
        <Button key="close" onClick={handleModalClose}>
          Cancel
        </Button>,
        <Button
          key="upload"
          type="primary"
          onClick={handleUpload}
          loading={uploading}
          disabled={fileList.length === 0}
          icon={<UploadOutlined />}
        >
          Import File
        </Button>,
      ]}
    >
      <div style={{ padding: '12px 0' }}>
        <p style={{ color: '#64748b', marginBottom: '16px', fontSize: '0.9rem' }}>
          Select a <code>.csv</code> file containing lead records (e.g. <code>sample-data/leads.csv</code>). Required columns: <code>company_name</code>.
        </p>

        <Upload
          accept=".csv"
          maxCount={1}
          beforeUpload={(file) => {
            const isCsv = file.name.endsWith('.csv');
            if (!isCsv) {
              message.error('You can only upload .csv files!');
              return Upload.LIST_IGNORE;
            }
            setFileList([file]);
            setResult(null);
            return false; // Prevent automatic upload
          }}
          onRemove={() => {
            setFileList([]);
            setResult(null);
          }}
          fileList={fileList}
        >
          <Button icon={<FileTextOutlined />}>Select CSV File</Button>
        </Upload>

        {result && (
          <div style={{ marginTop: '20px' }}>
            <Alert
              message={`Import Summary: ${result.successfulRows} of ${result.totalRows} rows imported successfully`}
              type={result.failedRows === 0 ? 'success' : 'warning'}
              showIcon
              icon={result.failedRows === 0 ? <CheckCircleOutlined /> : <WarningOutlined />}
              style={{ marginBottom: '12px' }}
            />

            {result.errors.length > 0 && (
              <div>
                <Text type="danger" strong style={{ fontSize: '0.85rem' }}>
                  Warnings / Errors ({result.errors.length}):
                </Text>
                <List
                  size="small"
                  bordered
                  dataSource={result.errors}
                  renderItem={(item) => (
                    <List.Item style={{ fontSize: '0.8rem', color: '#dc2626' }}>
                      {item}
                    </List.Item>
                  )}
                  style={{ maxHeight: '140px', overflowY: 'auto', marginTop: '8px' }}
                />
              </div>
            )}
          </div>
        )}
      </div>
    </Modal>
  );
};
