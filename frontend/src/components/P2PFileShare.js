// src/components/P2PFileShare.js
import React, { useState, useRef } from 'react';
import { Upload, Download, FileText, Copy, Check, AlertCircle, Trash2 } from 'lucide-react';

const P2PFileShare = () => {
  const [uploadFile, setUploadFile] = useState(null);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [uploadCode, setUploadCode] = useState('');
  const [downloadCode, setDownloadCode] = useState('');
  const [downloadProgress, setDownloadProgress] = useState(0);
  const [fileInfo, setFileInfo] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [copied, setCopied] = useState(false);
  const [activeTab, setActiveTab] = useState('upload');
  
  const fileInputRef = useRef(null);

  const showMessage = (msg, type) => {
    setMessage(msg);
    setMessageType(type);
    setTimeout(() => {
      setMessage('');
      setMessageType('');
    }, 5000);
  };

  const simulateProgress = (setProgress, duration = 2000) => {
    return new Promise((resolve) => {
      let progress = 0;
      const interval = setInterval(() => {
        progress += Math.random() * 30;
        if (progress >= 100) {
          setProgress(100);
          clearInterval(interval);
          resolve();
        } else {
          setProgress(Math.min(progress, 95));
        }
      }, duration / 10);
    });
  };

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (file) {
      setUploadFile(file);
      setUploadCode('');
      setUploadProgress(0);
    }
  };

  const handleUpload = async () => {
    if (!uploadFile) {
      showMessage('Please select a file first', 'error');
      return;
    }

    setIsUploading(true);
    setUploadProgress(0);
    
    const formData = new FormData();
    formData.append('file', uploadFile);

    try {
      // Simulate upload progress
      const progressPromise = simulateProgress(setUploadProgress);
      
      const response = await fetch('http://localhost:8080/api/upload', {
        method: 'POST',
        body: formData,
      });

      await progressPromise;
      const result = await response.json();
      
      if (result.success) {
        setUploadCode(result.code);
        showMessage('File uploaded successfully!', 'success');
        setUploadProgress(100);
      } else {
        showMessage(result.message || 'Upload failed', 'error');
        setUploadProgress(0);
      }
    } catch (error) {
      showMessage('Upload failed: ' + error.message, 'error');
      setUploadProgress(0);
    } finally {
      setIsUploading(false);
    }
  };

  const handleDownloadCodeChange = async (code) => {
    setDownloadCode(code);
    
    if (code.length === 8) {
      try {
        const response = await fetch(`http://localhost:8080/api/file/${code}/info`);
        const result = await response.json();
        
        if (result.success) {
          setFileInfo(result);
        } else {
          setFileInfo(null);
          showMessage('File not found or expired', 'error');
        }
      } catch (error) {
        setFileInfo(null);
        showMessage('Error checking file', 'error');
      }
    } else {
      setFileInfo(null);
    }
  };

  const handleDownload = async () => {
    if (!downloadCode || downloadCode.length !== 8) {
      showMessage('Please enter a valid 8-character code', 'error');
      return;
    }

    setIsDownloading(true);
    setDownloadProgress(0);

    try {
      // Simulate download progress
      const progressPromise = simulateProgress(setDownloadProgress, 1500);
      
      const response = await fetch(`http://localhost:8080/api/download/${downloadCode}`);
      
      await progressPromise;
      
      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileInfo?.fileName || 'download';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
        showMessage('File downloaded successfully!', 'success');
        setDownloadProgress(100);
      } else {
        showMessage('Download failed - file not found or expired', 'error');
        setDownloadProgress(0);
      }
    } catch (error) {
      showMessage('Download failed: ' + error.message, 'error');
      setDownloadProgress(0);
    } finally {
      setIsDownloading(false);
    }
  };

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(uploadCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      showMessage('Failed to copy code', 'error');
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">P2P File Share</h1>
          <p className="text-gray-600">Share files securely with unique codes</p>
        </div>

        {/* Message Display */}
        {message && (
          <div className={`mb-6 p-4 rounded-lg flex items-center gap-2 ${
            messageType === 'success' ? 'bg-green-100 text-green-800 border border-green-200' :
            messageType === 'error' ? 'bg-red-100 text-red-800 border border-red-200' :
            'bg-blue-100 text-blue-800 border border-blue-200'
          }`}>
            <AlertCircle className="w-5 h-5" />
            {message}
          </div>
        )}

        {/* Tab Navigation */}
        <div className="mb-6">
          <div className="flex space-x-1 bg-gray-200 p-1 rounded-lg">
            <button
              onClick={() => setActiveTab('upload')}
              className={`flex-1 py-2 px-4 rounded-md font-medium transition-colors ${
                activeTab === 'upload'
                  ? 'bg-white text-blue-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-800'
              }`}
            >
              <Upload className="w-4 h-4 inline mr-2" />
              Upload File
            </button>
            <button
              onClick={() => setActiveTab('download')}
              className={`flex-1 py-2 px-4 rounded-md font-medium transition-colors ${
                activeTab === 'download'
                  ? 'bg-white text-blue-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-800'
              }`}
            >
              <Download className="w-4 h-4 inline mr-2" />
              Download File
            </button>
          </div>
        </div>

        {/* Upload Tab */}
        {activeTab === 'upload' && (
          <div className="bg-white rounded-xl shadow-lg p-6 mb-6">
            <h2 className="text-2xl font-semibold text-gray-800 mb-4">Upload File</h2>
            
            <div className="space-y-4">
              <div>
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleFileSelect}
                  className="hidden"
                />
                <button
                  onClick={() => fileInputRef.current?.click()}
                  disabled={isUploading}
                  className="w-full p-8 border-2 border-dashed border-gray-300 rounded-lg hover:border-blue-400 transition-colors text-center disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  <FileText className="w-12 h-12 mx-auto mb-4 text-gray-400" />
                  <p className="text-gray-600">
                    {uploadFile ? uploadFile.name : 'Click to select a file'}
                  </p>
                  {uploadFile && (
                    <p className="text-sm text-gray-500 mt-2">
                      Size: {formatFileSize(uploadFile.size)}
                    </p>
                  )}
                </button>
              </div>

              {uploadFile && (
                <button
                  onClick={handleUpload}
                  disabled={isUploading}
                  className="w-full bg-blue-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isUploading ? 'Uploading...' : 'Upload File'}
                </button>
              )}

              {isUploading && (
                <div className="space-y-2">
                  <div className="flex justify-between text-sm text-gray-600">
                    <span>Upload Progress</span>
                    <span>{Math.round(uploadProgress)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${uploadProgress}%` }}
                    />
                  </div>
                </div>
              )}

              {uploadCode && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                  <h3 className="font-semibold text-green-800 mb-2">Upload Successful!</h3>
                  <p className="text-green-700 mb-3">Share this code to allow downloads:</p>
                  <div className="flex items-center gap-2">
                    <code className="bg-white px-4 py-2 rounded border flex-1 text-lg font-mono tracking-widest text-center">
                      {uploadCode}
                    </code>
                    <button
                      onClick={copyToClipboard}
                      className="bg-green-600 text-white p-2 rounded hover:bg-green-700 transition-colors"
                      title="Copy to clipboard"
                    >
                      {copied ? <Check className="w-5 h-5" /> : <Copy className="w-5 h-5" />}
                    </button>
                  </div>
                  <p className="text-sm text-green-600 mt-2">
                    * This code will expire in 24 hours
                  </p>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Download Tab */}
        {activeTab === 'download' && (
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-2xl font-semibold text-gray-800 mb-4">Download File</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Enter File Code
                </label>
                <input
                  type="text"
                  value={downloadCode}
                  onChange={(e) => handleDownloadCodeChange(e.target.value.toUpperCase())}
                  placeholder="Enter 8-character code"
                  maxLength={8}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg font-mono text-lg tracking-widest text-center uppercase focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              {fileInfo && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <h3 className="font-semibold text-blue-800 mb-2">File Found</h3>
                  <div className="text-blue-700 space-y-1">
                    <p><span className="font-medium">Name:</span> {fileInfo.fileName}</p>
                    <p><span className="font-medium">Size:</span> {formatFileSize(fileInfo.fileSize)}</p>
                    <p><span className="font-medium">Type:</span> {fileInfo.contentType}</p>
                    <p><span className="font-medium">Uploaded:</span> {new Date(fileInfo.uploadTime).toLocaleString()}</p>
                  </div>
                </div>
              )}

              {downloadCode.length === 8 && (
                <button
                  onClick={handleDownload}
                  disabled={isDownloading || !fileInfo}
                  className="w-full bg-green-600 text-white py-3 px-6 rounded-lg font-medium hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isDownloading ? 'Downloading...' : 'Download File'}
                </button>
              )}

              {isDownloading && (
                <div className="space-y-2">
                  <div className="flex justify-between text-sm text-gray-600">
                    <span>Download Progress</span>
                    <span>{Math.round(downloadProgress)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-green-600 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${downloadProgress}%` }}
                    />
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Footer */}
        <div className="text-center mt-8 text-gray-500 text-sm">
          <p>Files are automatically deleted after 24 hours</p>
          <p className="mt-1">Maximum file size: 100MB</p>
        </div>
      </div>
    </div>
  );
};

export default P2PFileShare;