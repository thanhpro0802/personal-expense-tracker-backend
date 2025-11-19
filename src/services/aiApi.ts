import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081';

export interface ChatRequest {
  question: string;
  walletId: string;
}

export interface ChatResponse {
  success: boolean;
  data: string;
}

export const sendChatMessage = async (request: ChatRequest): Promise<string> => {
  try {
    const token = localStorage.getItem('token');
    
    const response = await axios.post<ChatResponse>(
      `${API_BASE_URL}/api/ai/chat`,
      request,
      {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
      }
    );

    if (response.data.success) {
      return response.data.data;
    } else {
      throw new Error('Failed to get response from AI');
    }
  } catch (error: any) {
    if (error.response?.data?.data) {
      throw new Error(error.response.data.data);
    }
    throw new Error('Error communicating with AI assistant');
  }
};
