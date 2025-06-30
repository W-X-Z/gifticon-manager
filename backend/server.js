const express = require('express');
const cors = require('cors');
const app = express();
const port = 3000;

// CORS 설정
app.use(cors());
app.use(express.json());

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    success: true,
    data: {
      status: 'healthy',
      timestamp: new Date().toISOString(),
      version: '1.0.0',
      environment: 'development'
    },
    timestamp: new Date().toISOString()
  });
});

// Analyze endpoint (dummy response)
app.post('/api/analyze', (req, res) => {
  res.json({
    success: true,
    data: {
      brandName: '스타벅스',
      productName: '아메리카노',
      expiryDate: '2024-12-31',
      amount: 5000,
      balance: 5000,
      barcodeNumber: '1234567890',
      category: 'CAFE',
      purchaseDate: '2024-01-01'
    },
    error: null,
    confidence: 0.95
  });
});

app.listen(port, () => {
  console.log(`Server running at http://localhost:${port}`);
}); 