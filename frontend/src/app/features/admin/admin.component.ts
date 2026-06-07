import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Category, EventItem, Registration, Role, User } from '../../core/api.models';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <section class="admin-shell">
      <aside class="admin-sidebar">
        <a class="admin-brand" routerLink="/">
          <span class="brand-mark">EF</span>
          <span>Admin</span>
        </a>
        <nav>
          <a href="#admin-dashboard">Dashboard</a>
          <a href="#admin-events">Events</a>
          <a href="#admin-categories">Categories</a>
          <a href="#admin-users">Users</a>
          <a href="#admin-registrations">Registrations</a>
        </nav>
      </aside>

      <main class="admin-main">
        <div class="workspace-header admin-header" id="admin-dashboard">
          <div>
            <p class="eyebrow">Administration</p>
            <h1>Control center</h1>
            <p class="muted">Manage users, events, categories and registrations from one clean dashboard.</p>
          </div>
          <button class="button secondary" type="button" (click)="refresh()">Refresh</button>
        </div>

        @if (auth.currentUser()?.role !== 'ADMIN') {
          <section class="panel access-panel">
            <h2>Admin access required</h2>
            <p class="muted">Log in with an ADMIN account to use this console.</p>
            <a class="button primary" routerLink="/auth">Go to login</a>
          </section>
        } @else {
          @if (message) {
            <p class="message" [class.error]="hasError">{{ message }}</p>
          }

          <div class="metric-grid admin-metrics">
            <article>
              <span>Total events</span>
              <strong>{{ events.length }}</strong>
            </article>
            <article>
              <span>Published</span>
              <strong>{{ publishedEvents }}</strong>
            </article>
            <article>
              <span>Total users</span>
              <strong>{{ users.length }}</strong>
            </article>
            <article>
              <span>Categories</span>
              <strong>{{ categories.length }}</strong>
            </article>
            <article>
              <span>Registrations</span>
              <strong>{{ registrations.length }}</strong>
            </article>
          </div>

          <section class="panel admin-panel" id="admin-categories">
            <div class="section-title">
              <div>
                <p class="eyebrow">Categories</p>
                <h2>Create category</h2>
              </div>
            </div>
            <form class="inline-form" (ngSubmit)="createCategory()">
              <input name="categoryName" [(ngModel)]="categoryName" placeholder="Category name" required>
              <button class="button primary" type="submit">Add category</button>
            </form>
            <div class="chips category-list">
              @for (category of categories; track category.id) {
                <span>{{ category.name }}</span>
              }
            </div>
          </section>

          <section class="panel admin-panel" id="admin-users">
            <div class="section-title">
              <div>
                <p class="eyebrow">Users</p>
                <h2>User management</h2>
              </div>
              <span>{{ users.length }}</span>
            </div>
            <div class="table">
              <div class="table-head users-row">
                <span>ID</span>
                <span>Name</span>
                <span>Email</span>
                <span>Role</span>
                <span></span>
              </div>
              @for (user of users; track user.id) {
                <div class="users-row">
                  <span>#{{ user.id }}</span>
                  <span>{{ user.firstName }} {{ user.lastName }}</span>
                  <span>{{ user.email }}</span>
                  <select [name]="'role-' + user.id" [ngModel]="user.role" (ngModelChange)="changeRole(user.id, $event)">
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                  <button class="button danger compact" type="button" (click)="deleteUser(user.id)">Delete</button>
                </div>
              }
            </div>
          </section>

          <section class="panel admin-panel" id="admin-events">
            <div class="section-title">
              <div>
                <p class="eyebrow">Events</p>
                <h2>All events</h2>
              </div>
              <span>{{ events.length }}</span>
            </div>
            <div class="table">
              <div class="table-head events-row">
                <span>ID</span>
                <span>Title</span>
                <span>Status</span>
                <span>Organizer</span>
                <span>Date</span>
              </div>
              @for (event of events; track event.id) {
                <div class="events-row">
                  <span>#{{ event.id }}</span>
                  <span>{{ event.title }}</span>
                  <span class="status">{{ event.status }}</span>
                  <span>{{ event.creatorEmail }}</span>
                  <span>{{ event.date | date:'dd/MM/yyyy HH:mm' }}</span>
                </div>
              }
            </div>
          </section>

          <section class="panel admin-panel" id="admin-registrations">
            <div class="section-title">
              <div>
                <p class="eyebrow">Registrations</p>
                <h2>All registrations</h2>
              </div>
              <span>{{ registrations.length }}</span>
            </div>
            <div class="table">
              <div class="table-head regs-row">
                <span>ID</span>
                <span>User</span>
                <span>Event</span>
                <span>Status</span>
              </div>
              @for (registration of registrations; track registration.id) {
                <div class="regs-row">
                  <span>#{{ registration.id }}</span>
                  <span>#{{ registration.userId }}</span>
                  <span>#{{ registration.eventId }}</span>
                  <span class="status">{{ registration.status }}</span>
                </div>
              }
            </div>
          </section>
        }
      </main>
    </section>
  `
})
export class AdminComponent implements OnInit {
  users: User[] = [];
  events: EventItem[] = [];
  registrations: Registration[] = [];
  categories: Category[] = [];
  categoryName = '';
  message = '';
  hasError = false;

  constructor(
    readonly auth: AuthService,
    private readonly api: ApiService
  ) {}

  get publishedEvents() {
    return this.events.filter((event) => event.status === 'PUBLISHED').length;
  }

  ngOnInit() {
    this.auth.loadProfile().subscribe(() => this.refresh());
  }

  refresh() {
    if (this.auth.currentUser()?.role !== 'ADMIN') {
      return;
    }

    this.api.adminUsers().subscribe({
      next: (page) => this.users = page.content,
      error: () => this.flash('Unable to load users.', true)
    });

    this.api.adminEvents().subscribe({
      next: (page) => this.events = page.content,
      error: () => this.flash('Unable to load admin events.', true)
    });

    this.api.adminRegistrations().subscribe({
      next: (page) => this.registrations = page.content,
      error: () => this.flash('Unable to load admin registrations.', true)
    });

    this.api.categories().subscribe({
      next: (page) => this.categories = page.content,
      error: () => this.categories = []
    });
  }

  changeRole(id: number, role: Role) {
    this.api.changeUserRole(id, role).subscribe({
      next: () => {
        this.flash('User role updated.');
        this.refresh();
      },
      error: () => this.flash('Unable to update user role.', true)
    });
  }

  deleteUser(id: number) {
    this.api.deleteUser(id).subscribe({
      next: () => {
        this.flash('User deleted.');
        this.refresh();
      },
      error: () => this.flash('Unable to delete user.', true)
    });
  }

  createCategory() {
    this.api.createCategory({ name: this.categoryName }).subscribe({
      next: () => {
        this.categoryName = '';
        this.flash('Category created.');
        this.refresh();
      },
      error: () => this.flash('Unable to create category.', true)
    });
  }

  private flash(message: string, error = false) {
    this.message = message;
    this.hasError = error;
  }
}
