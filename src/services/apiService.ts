import { User, Race, Ride, RaceCalendar, TrackLength, TrackType, AuthResponse, RideType, UserResponse } from '../types';

const API_BASE = 'http://localhost:8080/api';
const TOKEN_KEY = 'bezci_sobe_token';

function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

function authHeaders(): Record<string, string> {
  const token = getToken();
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
    this.name = 'ApiError';
  }
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => ({ message: 'Chyba serveru' }));
    throw new ApiError(body.message || `HTTP ${res.status}`, res.status);
  }
  return res.json();
}

export const apiService = {
  init: () => {
    // No-op: data is now in the database
  },

  // Auth
  login: async (username: string, password: string): Promise<AuthResponse> => {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });
    const data = await handleResponse<AuthResponse>(res);
    setToken(data.token);
    return data;
  },

  register: async (username: string, email: string, password: string, language?: string): Promise<User> => {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password, language }),
    });
    return handleResponse<User>(res);
  },

  verifyEmail: async (token: string): Promise<void> => {
    const res = await fetch(`${API_BASE}/auth/verify-email?token=${encodeURIComponent(token)}`, {
      method: 'GET',
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: 'Chyba serveru' }));
      throw new ApiError(body.message || `HTTP ${res.status}`, res.status);
    }
  },

  resendVerification: async (email: string): Promise<void> => {
    const res = await fetch(`${API_BASE}/auth/resend-verification`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: 'Chyba serveru' }));
      throw new ApiError(body.message || `HTTP ${res.status}`, res.status);
    }
  },

  forgotPassword: async (email: string): Promise<void> => {
    const res = await fetch(`${API_BASE}/auth/forgot-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email }),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: 'Chyba serveru' }));
      throw new ApiError(body.message || `HTTP ${res.status}`, res.status);
    }
  },

  resetPassword: async (token: string, password: string): Promise<void> => {
    const res = await fetch(`${API_BASE}/auth/reset-password`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, password }),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: 'Chyba serveru' }));
      throw new ApiError(body.message || `HTTP ${res.status}`, res.status);
    }
  },

  logout: (): void => {
    removeToken();
  },

  getCurrentUser: async (): Promise<User | null> => {
    const token = getToken();
    if (!token) return null;
    try {
      const res = await fetch(`${API_BASE}/auth/me`, {
        headers: authHeaders(),
      });
      if (!res.ok) {
        removeToken();
        return null;
      }
      return await res.json();
    } catch {
      return null;
    }
  },

  changePassword: async (currentPassword: string, newPassword: string): Promise<void> => {
    const res = await fetch(`${API_BASE}/auth/change-password`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ currentPassword, newPassword }),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: 'Chyba serveru' }));
      throw new ApiError(body.message || `HTTP ${res.status}`, res.status);
    }
  },

  deleteAccount: async (password: string): Promise<void> => {
    const res = await fetch(`${API_BASE}/auth/delete-account`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ password }),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: 'Chyba serveru' }));
      throw new ApiError(body.message || `HTTP ${res.status}`, res.status);
    }
  },

  updateProfile: async (data: { firstName?: string; lastName?: string; city?: string; language?: string }): Promise<UserResponse> => {
    const res = await fetch(`${API_BASE}/auth/me`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(data),
    });
    return handleResponse<UserResponse>(res);
  },

  // Races
  getRaces: async (): Promise<Race[]> => {
    const res = await fetch(`${API_BASE}/races`);
    return handleResponse<Race[]>(res);
  },

  getRaceById: async (id: string): Promise<Race | null> => {
    try {
      const res = await fetch(`${API_BASE}/races/${id}`);
      return await handleResponse<Race>(res);
    } catch {
      return null;
    }
  },

  // Rides
  getRidesByRace: async (raceId: string): Promise<Ride[]> => {
    const res = await fetch(`${API_BASE}/rides?raceId=${raceId}`);
    return handleResponse<Ride[]>(res);
  },

  createRide: async (ride: {
    raceId: string;
    type: RideType;
    from: string;
    to?: string;
    car?: string;
    availableSeats: number;
    notes?: string;
  }): Promise<Ride> => {
    const res = await fetch(`${API_BASE}/rides`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({
        raceId: Number(ride.raceId),
        type: ride.type,
        from: ride.from,
        to: ride.to || null,
        car: ride.car || null,
        availableSeats: ride.availableSeats,
        notes: ride.notes || null,
      }),
    });
    return handleResponse<Ride>(res);
  },

  deleteRide: async (id: string): Promise<void> => {
    const res = await fetch(`${API_BASE}/rides/${id}`, {
      method: 'DELETE',
      headers: authHeaders(),
    });
    if (!res.ok) {
      const body = await res.json().catch(() => ({ message: 'Chyba při mazání jízdy' }));
      throw new Error(body.message);
    }
  },

  acceptRide: async (rideId: string): Promise<Ride> => {
    const res = await fetch(`${API_BASE}/rides/${rideId}/accept`, {
      method: 'POST',
      headers: authHeaders(),
    });
    return handleResponse<Ride>(res);
  },

  cancelRideAcceptance: async (rideId: string): Promise<Ride> => {
    const res = await fetch(`${API_BASE}/rides/${rideId}/cancel`, {
      method: 'POST',
      headers: authHeaders(),
    });
    return handleResponse<Ride>(res);
  },

  // Reference data
  getTrackLengths: async (): Promise<TrackLength[]> => {
    const res = await fetch(`${API_BASE}/reference/track-lengths`);
    return handleResponse<TrackLength[]>(res);
  },

  getTrackTypes: async (): Promise<TrackType[]> => {
    const res = await fetch(`${API_BASE}/reference/track-types`);
    return handleResponse<TrackType[]>(res);
  },

  getRaceCalendars: async (): Promise<RaceCalendar[]> => {
    const res = await fetch(`${API_BASE}/reference/race-calendars`);
    return handleResponse<RaceCalendar[]>(res);
  },
};
