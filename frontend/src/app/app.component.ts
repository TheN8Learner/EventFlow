import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ApiService } from './core/api.service';
import { User } from './core/api.models';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="topbar">
      <a class="brand" routerLink="/">
        <span class="brand-mark">EF</span>
        <span>EventFlow</span>
      </a>

      <nav>
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Home</a>
        <a routerLink="/events" routerLinkActive="active">Events</a>
        <a routerLink="/about" routerLinkActive="active">A propos</a>
        <a routerLink="/contact" routerLinkActive="active">Contact</a>
      </nav>

      <div class="topbar-actions">
        @if (auth.currentUser(); as user) {
          <div class="profile-dropdown">
            <button class="profile-trigger" type="button" (click)="toggleProfile(user)" aria-label="Ouvrir le profil">
              <span class="profile-icon" aria-hidden="true"></span>
              <span class="profile-name">{{ user.firstName }}</span>
            </button>

            @if (profileOpen) {
              <section class="profile-menu">
                <div class="profile-summary">
                  <span class="profile-icon large" aria-hidden="true"></span>
                  <div>
                    <strong>{{ user.firstName }} {{ user.lastName }}</strong>
                    <span>{{ user.email }}</span>
                    <small>{{ user.role }}</small>
                  </div>
                </div>

                @if (profileMessage) {
                  <p class="profile-message" [class.error]="profileError">{{ profileMessage }}</p>
                }

                @if (!editingProfile && !changingPassword) {
                  <div class="profile-actions">
                    <a class="button secondary full" routerLink="/dashboard" (click)="profileOpen = false">My dashboard</a>
                    <button class="button secondary full" type="button" (click)="startEdit(user)">Edit profile</button>
                    <button class="button secondary full" type="button" (click)="startPasswordChange()">Change password</button>
                    <button class="button danger full" type="button" (click)="logout()">Logout</button>
                  </div>
                }

                @if (editingProfile) {
                  <form class="profile-form" (ngSubmit)="saveProfile()">
                    <label>
                      First name
                      <input name="menuFirstName" [(ngModel)]="profileForm.firstName" required>
                    </label>
                    <label>
                      Last name
                      <input name="menuLastName" [(ngModel)]="profileForm.lastName" required>
                    </label>
                    <label>
                      Email
                      <input name="menuEmail" type="email" [(ngModel)]="profileForm.email" required>
                    </label>
                    <div class="profile-form-actions">
                      <button class="button secondary" type="button" (click)="cancelForms()">Cancel</button>
                      <button class="button primary" type="submit" [disabled]="savingProfile">
                        {{ savingProfile ? 'Saving...' : 'Save' }}
                      </button>
                    </div>
                  </form>
                }

                @if (changingPassword) {
                  <form class="profile-form" (ngSubmit)="savePassword()">
                    <label>
                      Current password
                      <input name="menuCurrentPassword" type="password" [(ngModel)]="passwordForm.currentPassword" required>
                    </label>
                    <label>
                      New password
                      <input name="menuNewPassword" type="password" [(ngModel)]="passwordForm.newPassword" required>
                    </label>
                    <div class="profile-form-actions">
                      <button class="button secondary" type="button" (click)="cancelForms()">Cancel</button>
                      <button class="button primary" type="submit" [disabled]="savingPassword">
                        {{ savingPassword ? 'Saving...' : 'Change' }}
                      </button>
                    </div>
                  </form>
                }
              </section>
            }
          </div>
        } @else {
          <a class="button primary" routerLink="/auth">Register</a>
        }
      </div>
    </header>

    <main>
      <router-outlet></router-outlet>
    </main>

    <footer class="site-footer">
      <div>
        <a class="brand footer-brand" routerLink="/">
          <span class="brand-mark">EF</span>
          <span>EventFlow</span>
        </a>
        <p>Une plateforme simple pour decouvrir, creer et gerer des evenements.</p>
      </div>

      <nav aria-label="Footer navigation">
        <a routerLink="/">Home</a>
        <a routerLink="/events">Events</a>
        <a routerLink="/about">A propos</a>
        <a routerLink="/contact">Contact</a>
      </nav>

      <small>© 2026 EventFlow. All rights reserved.</small>
    </footer>
  `
})
export class AppComponent implements OnInit {
  profileOpen = false;
  editingProfile = false;
  changingPassword = false;
  savingProfile = false;
  savingPassword = false;
  profileMessage = '';
  profileError = false;

  profileForm = {
    firstName: '',
    lastName: '',
    email: ''
  };

  passwordForm = {
    currentPassword: '',
    newPassword: ''
  };

  constructor(
    readonly auth: AuthService,
    private readonly api: ApiService
  ) {}

  ngOnInit() {
    this.auth.loadProfile().subscribe((user) => {
      if (user) {
        this.patchProfile(user);
      }
    });
  }

  toggleProfile(user: User) {
    this.profileOpen = !this.profileOpen;
    this.profileMessage = '';
    if (this.profileOpen) {
      this.patchProfile(user);
      this.cancelForms();
    }
  }

  startEdit(user: User) {
    this.patchProfile(user);
    this.editingProfile = true;
    this.changingPassword = false;
    this.profileMessage = '';
  }

  startPasswordChange() {
    this.passwordForm = { currentPassword: '', newPassword: '' };
    this.changingPassword = true;
    this.editingProfile = false;
    this.profileMessage = '';
  }

  cancelForms() {
    this.editingProfile = false;
    this.changingPassword = false;
    this.savingProfile = false;
    this.savingPassword = false;
  }

  saveProfile() {
    this.savingProfile = true;
    this.profileMessage = '';
    this.api.updateMe(this.profileForm).subscribe({
      next: (user) => {
        this.auth.currentUser.set(user);
        this.patchProfile(user);
        this.savingProfile = false;
        this.editingProfile = false;
        this.flashProfile('Profile updated.');
      },
      error: () => {
        this.savingProfile = false;
        this.flashProfile('Unable to update profile.', true);
      }
    });
  }

  savePassword() {
    this.savingPassword = true;
    this.profileMessage = '';
    this.api.changePassword(this.passwordForm).subscribe({
      next: () => {
        this.passwordForm = { currentPassword: '', newPassword: '' };
        this.savingPassword = false;
        this.changingPassword = false;
        this.flashProfile('Password changed.');
      },
      error: () => {
        this.savingPassword = false;
        this.flashProfile('Unable to change password.', true);
      }
    });
  }

  logout() {
    this.profileOpen = false;
    this.auth.logout();
  }

  private patchProfile(user: User) {
    this.profileForm = {
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email
    };
  }

  private flashProfile(message: string, error = false) {
    this.profileMessage = message;
    this.profileError = error;
  }
}
