import { describe, it, expect, beforeEach, vi } from 'vitest';
import { apiService } from '../services/apiService';
import { RideType } from '../types';

const mockFetch = vi.fn();
vi.stubGlobal('fetch', mockFetch);

function jsonResponse(data: unknown, status = 200) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(data),
  } as Response);
}

describe('API Service', () => {
  beforeEach(() => {
    localStorage.clear();
    mockFetch.mockReset();
  });

  describe('Authentication', () => {
    it('should login with correct credentials', async () => {
      const authResponse = { token: 'jwt-token', userId: '1', username: 'admin', roles: ['ROLE_USER'] };
      mockFetch.mockReturnValueOnce(jsonResponse(authResponse));

      const result = await apiService.login('admin', 'admin123');
      expect(result.token).toBe('jwt-token');
      expect(result.username).toBe('admin');
      expect(localStorage.getItem('bezci_sobe_token')).toBe('jwt-token');
    });

    it('should throw error with incorrect credentials', async () => {
      mockFetch.mockReturnValueOnce(jsonResponse({ message: 'Neplatné přihlašovací údaje' }, 401));

      await expect(apiService.login('admin', 'wrong')).rejects.toThrow('Neplatné přihlašovací údaje');
    });

    it('should register new user', async () => {
      const user = { id: 'new-id', username: 'newuser', email: 'new@example.com', roles: ['ROLE_USER'] };
      mockFetch.mockReturnValueOnce(jsonResponse(user, 201));

      const result = await apiService.register('newuser', 'new@example.com', 'password123');
      expect(result.username).toBe('newuser');
      expect(result.email).toBe('new@example.com');
    });

    it('should logout user', () => {
      localStorage.setItem('bezci_sobe_token', 'test-token');
      apiService.logout();
      expect(localStorage.getItem('bezci_sobe_token')).toBeNull();
    });
  });

  describe('Races', () => {
    it('should get all races', async () => {
      const races = [{ id: '1', name: 'Test Race', place: 'Praha' }];
      mockFetch.mockReturnValueOnce(jsonResponse(races));

      const result = await apiService.getRaces();
      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('Test Race');
    });

    it('should get race by id', async () => {
      const race = { id: '1', name: 'Pražský maraton', place: 'Praha' };
      mockFetch.mockReturnValueOnce(jsonResponse(race));

      const result = await apiService.getRaceById('1');
      expect(result).not.toBeNull();
      expect(result?.name).toBe('Pražský maraton');
    });
  });

  describe('Rides', () => {
    it('should create new ride', async () => {
      const ride = { id: 'new-ride', raceId: '1', from: 'Brno', to: 'Praha' };
      mockFetch.mockReturnValueOnce(jsonResponse(ride, 201));

      const result = await apiService.createRide({
        raceId: '1',
        type: RideType.OFFER,
        from: 'Brno',
        to: 'Praha',
        car: 'Škoda Octavia',
        availableSeats: 3,
      });

      expect(result).toHaveProperty('id');
      expect(result.from).toBe('Brno');
    });

    it('should get rides by race', async () => {
      const rides = [{ id: '1', raceId: '1', from: 'Praha' }];
      mockFetch.mockReturnValueOnce(jsonResponse(rides));

      const result = await apiService.getRidesByRace('1');
      expect(result).toHaveLength(1);
    });

    it('should delete ride', async () => {
      mockFetch.mockReturnValueOnce(Promise.resolve({ ok: true, status: 204 } as Response));

      await expect(apiService.deleteRide('1')).resolves.toBeUndefined();
    });
  });

  describe('Reference Data', () => {
    it('should get track lengths', async () => {
      const lengths = [{ id: '1', name: '5K' }, { id: '2', name: '10K' }];
      mockFetch.mockReturnValueOnce(jsonResponse(lengths));

      const result = await apiService.getTrackLengths();
      expect(result).toHaveLength(2);
    });

    it('should get track types', async () => {
      const types = [{ id: '1', name: 'Silnice' }];
      mockFetch.mockReturnValueOnce(jsonResponse(types));

      const result = await apiService.getTrackTypes();
      expect(result).toHaveLength(1);
    });

    it('should get race calendars', async () => {
      const calendars = [{ id: '1', year: 2026, isActive: true }];
      mockFetch.mockReturnValueOnce(jsonResponse(calendars));

      const result = await apiService.getRaceCalendars();
      expect(result).toHaveLength(1);
    });
  });
});
