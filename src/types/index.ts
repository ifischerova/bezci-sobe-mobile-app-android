// User types
export interface User {
  id: string;
  username: string;
  email: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  phone?: string;
  city?: string;
  language?: string;
  driversLicenseValidUntil?: string;
  driversLicenseId?: string;
  isBlocked?: boolean;
  note?: string;
  roles: Role[];
}

// Matches the backend UserResponse DTO returned by /auth/me and PUT /auth/me.
export interface UserResponse {
  id: string;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  city?: string;
  language?: string;
  roles: string[];
}

export enum Role {
  USER = 'ROLE_USER',
  ADMIN = 'ROLE_ADMIN',
}

// Race types
export interface Race {
  id: string;
  name: string;
  place: string;
  date: string;
  startTime: string;
  web?: string;
  trackLength: TrackLength;
  trackType: TrackType;
  certifications: Certification[];
  raceCalendarId: string;
  isPast: boolean;
}

export interface TrackLength {
  id: string;
  name: string; // e.g., "5K", "10K", "Half Marathon", "Marathon"
}

export interface TrackType {
  id: string;
  name: string; // e.g., "Road", "Trail", "Track"
}

export interface Certification {
  id: string;
  name: string; // e.g., "IAAF", "AIMS"
}

export interface RaceCalendar {
  id: string;
  year: number;
  isActive: boolean;
}

// Ride sharing types
export interface Ride {
  id: string;
  raceId: string;
  racePast: boolean;
  userId: string;
  userUsername: string;
  userFirstName?: string;
  userLastName?: string;
  type: RideType;
  from: string;
  to?: string;
  car?: string;
  availableSeats: number;
  occupiedSeats: number;
  passengers: string[]; // Array of user IDs
  notes?: string;
  createdAt: string;
}

export enum RideType {
  OFFER = 'OFFER',
  REQUEST = 'REQUEST',
}

// Authentication types
export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
  confirmPassword?: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  username: string;
  roles: string[];
}

// Form validation
export interface ValidationError {
  field: string;
  message: string;
}

export interface FormState<T> {
  data: T;
  errors: ValidationError[];
  isValid: boolean;
  isSubmitting: boolean;
}
