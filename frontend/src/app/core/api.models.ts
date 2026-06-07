export type Role = 'USER' | 'ADMIN';
export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'FINISHED' | 'COMPLETED';
export type RegistrationStatus = 'CONFIRMED' | 'WAITLISTED' | 'CANCELLED';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RegisterRequest extends LoginRequest {
  firstName: string;
  lastName: string;
}

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
}

export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface Category {
  id: number;
  name: string;
}

export interface CategoryRequest {
  name: string;
}

export interface EventRequest {
  title: string;
  description: string;
  flyer: string;
  date: string;
  capacityMax: number;
  categoryIds: number[];
}

export interface EventItem {
  id: number;
  title: string;
  description: string;
  flyer: string;
  date: string;
  capacityMax: number;
  status: EventStatus;
  creatorId: number;
  creatorName: string;
  creatorEmail: string;
  categories: Category[];
  registeredCount: number | null;
  availableSeats: number | null;
}

export interface Registration {
  id: number;
  status: RegistrationStatus;
  eventId: number;
  userId: number;
}
