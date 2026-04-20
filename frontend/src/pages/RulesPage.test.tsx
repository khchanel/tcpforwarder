import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import RulesPage from './RulesPage';
import client from '../api/client';

// Mock the API client
jest.mock('../api/client');
const mockedClient = client as jest.Mocked<typeof client>;

const mockRules = [
  { id: '1', name: 'Rule 1', listenPort: 9001, targetHost: 'localhost', targetPort: 8080, enabled: true },
  { id: '2', name: 'Rule 2', listenPort: 9002, targetHost: 'localhost', targetPort: 8081, enabled: false }
];

const mockStats = {
  '1': { connectionErrors: 0, lastError: null, lastErrorAt: null, totalBytesIn: 0, totalBytesOut: 0, totalConnections: 0 }
};

describe('RulesPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockedClient.get.mockImplementation((url: string) => {
      if (url === '/rules') return Promise.resolve({ data: mockRules });
      if (url.includes('/stats')) return Promise.resolve({ data: mockStats });
      return Promise.reject(new Error('Not found'));
    });
  });

  test('renders rules list correctly', async () => {
    render(<RulesPage />);

    await waitFor(() => {
      expect(screen.getByText('Rule 1')).toBeInTheDocument();
    });
    expect(screen.getByText('Rule 2')).toBeInTheDocument();
  });

  test('opens add form when + Add Rule is clicked', async () => {
    render(<RulesPage />);

    const addBtn = await screen.findByText('+ Add Rule');
    fireEvent.click(addBtn);

    expect(screen.getByText('New Rule')).toBeInTheDocument();
    expect(screen.getByLabelText('Name')).toBeInTheDocument();
  });

  test('handles error during rule loading', async () => {
    mockedClient.get.mockImplementation(() => Promise.reject({ response: { data: { error: 'Failed to load rules' } } }));
    render(<RulesPage />);

    await waitFor(() => {
        expect(screen.queryByText('Rule 1')).not.toBeInTheDocument();
    });
  });
});
