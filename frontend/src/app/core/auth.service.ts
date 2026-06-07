import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, of, tap } from 'rxjs';
import { AuthTokens, LoginRequest, RegisterRequest, User } from './api.models';
import { ApiService } from './api.service';

const TOKEN_KEY = 'eventflow_token';
const ACCESS_TOKEN_KEY = 'eventflow_access_token';
const REFRESH_TOKEN_KEY = 'eventflow_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<User | null>(null);
  readonly loadingProfile = signal(false);

  constructor(
    private readonly api: ApiService,
    private readonly router: Router
  ) {}

  get token() {
    return localStorage.getItem(ACCESS_TOKEN_KEY) || localStorage.getItem(TOKEN_KEY);
  }

  get refreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  get isAuthenticated() {
    return Boolean(this.token);
  }

  login(payload: LoginRequest, admin = false) {
    const request = admin ? this.api.loginAdmin(payload) : this.api.login(payload);
    return request.pipe(tap((tokens) => this.setTokens(tokens)));
  }

  register(payload: RegisterRequest, admin = false) {
    const request = admin ? this.api.registerAdmin(payload) : this.api.register(payload);
    return request.pipe(tap((tokens) => this.setTokens(tokens)));
  }

  refreshSession() {
    const refreshToken = this.refreshToken;
    if (!refreshToken) {
      return of(null);
    }

    return this.api.refreshToken({ refreshToken }).pipe(
      tap((tokens) => this.setTokens(tokens)),
      catchError(() => {
        this.logout(false);
        return of(null);
      })
    );
  }

  loadProfile() {
    if (!this.token) {
      this.currentUser.set(null);
      return of(null);
    }

    this.loadingProfile.set(true);
    return this.api.me().pipe(
      tap((user) => this.currentUser.set(user)),
      catchError(() => {
        this.logout(false);
        return of(null);
      }),
      tap(() => this.loadingProfile.set(false))
    );
  }

  setTokens(tokens: AuthTokens) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  }

  logout(navigate = true) {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.currentUser.set(null);
    if (navigate) {
      this.router.navigateByUrl('/');
    }
  }
}
