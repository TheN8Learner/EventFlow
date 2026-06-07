import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import {
  Category,
  CategoryRequest,
  ChangePasswordRequest,
  AuthTokens,
  EventItem,
  EventRequest,
  LoginRequest,
  Page,
  RefreshTokenRequest,
  RegisterRequest,
  Registration,
  UpdateUserRequest,
  User
} from './api.models';

interface CloudinaryUploadResponse {
  secure_url: string;
  public_id: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = environment.apiBaseUrl.replace(/\/$/, '');

  constructor(private readonly http: HttpClient) {}

  register(payload: RegisterRequest) {
    return this.http.post<AuthTokens>(`${this.baseUrl}/api/v1/register`, payload);
  }

  login(payload: LoginRequest) {
    return this.http.post<AuthTokens>(`${this.baseUrl}/api/v1/login`, payload);
  }

  registerAdmin(payload: RegisterRequest) {
    return this.http.post<AuthTokens>(`${this.baseUrl}/api/v1/admin/register`, payload);
  }

  loginAdmin(payload: LoginRequest) {
    return this.http.post<AuthTokens>(`${this.baseUrl}/api/v1/admin/login`, payload);
  }

  refreshToken(payload: RefreshTokenRequest) {
    return this.http.post<AuthTokens>(`${this.baseUrl}/api/v1/refresh-token`, payload);
  }

  me() {
    return this.http.get<User>(`${this.baseUrl}/api/v1/me`);
  }

  updateMe(payload: UpdateUserRequest) {
    return this.http.put<User>(`${this.baseUrl}/api/v1/me`, payload);
  }

  changePassword(payload: ChangePasswordRequest) {
    return this.http.post<void>(`${this.baseUrl}/api/v1/me/change-password`, payload);
  }

  events(page = 0, size = 100) {
    return this.http.get<Page<EventItem>>(`${this.baseUrl}/api/v1/events`, { params: this.pageParams(page, size) });
  }

  eventDetails(id: number) {
    return this.http.get<EventItem>(`${this.baseUrl}/api/v1/events/${id}`);
  }

  myCreatedEvents(page = 0, size = 100) {
    return this.http.get<Page<EventItem>>(`${this.baseUrl}/api/v1/events/my-created`, { params: this.pageParams(page, size) });
  }

  myJoinedEvents(page = 0, size = 100) {
    return this.http.get<Page<EventItem>>(`${this.baseUrl}/api/v1/events/my-joined`, { params: this.pageParams(page, size) });
  }

  createEvent(payload: EventRequest) {
    return this.http.post<EventItem>(`${this.baseUrl}/api/v1/events`, payload);
  }

  hasCloudinaryConfig() {
    return Boolean(environment.cloudinary.cloudName && environment.cloudinary.uploadPreset);
  }

  uploadFlyer(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', environment.cloudinary.uploadPreset);

    return this.http.post<CloudinaryUploadResponse>(
      `https://api.cloudinary.com/v1_1/${environment.cloudinary.cloudName}/image/upload`,
      formData
    );
  }

  draftEvent(id: number) {
    return this.http.post<EventItem>(`${this.baseUrl}/api/v1/events/${id}/draft`, {});
  }

  publishEvent(id: number) {
    return this.http.post<EventItem>(`${this.baseUrl}/api/v1/events/${id}/publish`, {});
  }

  cancelEvent(id: number) {
    return this.http.post<EventItem>(`${this.baseUrl}/api/v1/events/${id}/cancel`, {});
  }

  categories(page = 0, size = 100) {
    return this.http.get<Page<Category>>(`${this.baseUrl}/category`, { params: this.pageParams(page, size) });
  }

  createCategory(payload: CategoryRequest) {
    return this.http.post<Category>(`${this.baseUrl}/category`, payload);
  }

  registrations(page = 0, size = 100) {
    return this.http.get<Page<Registration>>(`${this.baseUrl}/api/v1/registrations/my`, { params: this.pageParams(page, size) });
  }

  registrationsForMyCreatedEvents(page = 0, size = 100) {
    return this.http.get<Page<Registration>>(`${this.baseUrl}/api/v1/registrations/my-created-events`, { params: this.pageParams(page, size) });
  }

  registerToEvent(eventId: number) {
    return this.http.post<Registration>(`${this.baseUrl}/api/v1/registrations`, { eventId });
  }

  cancelRegistration(id: number) {
    return this.http.post<Registration>(`${this.baseUrl}/api/v1/registrations/${id}/cancel`, {});
  }

  adminUsers(page = 0, size = 20) {
    return this.http.get<Page<User>>(`${this.baseUrl}/api/v1/admin/users`, { params: this.pageParams(page, size) });
  }

  adminEvents(page = 0, size = 20) {
    return this.http.get<Page<EventItem>>(`${this.baseUrl}/api/v1/admin/events`, { params: this.pageParams(page, size) });
  }

  adminRegistrations(page = 0, size = 20) {
    return this.http.get<Page<Registration>>(`${this.baseUrl}/api/v1/admin/registrations`, { params: this.pageParams(page, size) });
  }

  changeUserRole(id: number, role: 'USER' | 'ADMIN') {
    return this.http.put<User>(`${this.baseUrl}/api/v1/admin/users/${id}/role`, { role });
  }

  deleteUser(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/api/v1/admin/users/${id}`);
  }

  private pageParams(page: number, size: number) {
    return new HttpParams().set('page', page).set('size', size);
  }
}
