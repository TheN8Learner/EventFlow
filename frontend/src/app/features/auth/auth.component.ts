import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

type Flow = 'login' | 'register';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="auth-layout auth-page">
      <div class="auth-copy">
        <p class="eyebrow">Welcome back</p>
        <h1>{{ flow === 'login' ? 'Sign in to EventFlow' : 'Create your EventFlow account' }}</h1>
        <p class="muted">Access your events, registrations, organizer tools and profile from a clean SaaS workspace.</p>
        <div class="auth-proof">
          <span><strong>JWT</strong> secure sessions</span>
          <span><strong>Events</strong> join and create</span>
          <span><strong>Admin</strong> role-based access</span>
        </div>
      </div>

      <form class="panel auth-card" (ngSubmit)="submit()">
        <div class="segmented">
          <button type="button" [class.active]="flow === 'login'" (click)="flow = 'login'">Login</button>
          <button type="button" [class.active]="flow === 'register'" (click)="flow = 'register'">Register</button>
        </div>

        @if (flow === 'register') {
          <label>
            First name
            <input name="firstName" [(ngModel)]="form.firstName" required>
          </label>
          <label>
            Last name
            <input name="lastName" [(ngModel)]="form.lastName" required>
          </label>
        }

        <label>
          Email
          <input name="email" type="email" [(ngModel)]="form.email" required>
        </label>
        <label>
          Password
          <input name="password" type="password" [(ngModel)]="form.password" required>
        </label>

        @if (message) {
          <p class="message" [class.error]="hasError">{{ message }}</p>
        }

        <button class="button primary full" type="submit" [disabled]="submitting">
          {{ submitting ? 'Processing...' : (flow === 'login' ? 'Sign in' : 'Create account') }}
        </button>
      </form>
    </section>
  `
})
export class AuthComponent {
  flow: Flow = 'login';
  submitting = false;
  hasError = false;
  message = '';

  form = {
    firstName: '',
    lastName: '',
    email: '',
    password: ''
  };

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  submit() {
    this.submitting = true;
    this.message = '';
    this.hasError = false;

    const request = this.flow === 'login'
      ? this.auth.login({ email: this.form.email, password: this.form.password })
      : this.auth.register(this.form);

    request.subscribe({
      next: () => {
        this.auth.loadProfile().subscribe(() => {
          this.submitting = false;
          this.router.navigateByUrl('/events');
        });
      },
      error: () => {
        this.submitting = false;
        this.hasError = true;
        this.message = 'Unable to authenticate with these credentials.';
      }
    });
  }
}
