import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink, RouterLinkActive } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Category, EventItem, Registration, RegistrationStatus } from '../../core/api.models';

type DashboardView = 'events' | 'joined' | 'created' | 'organizer';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  template: `
    <section class="workspace dashboard-shell">
      <aside class="events-rail" aria-label="Events navigation">
        <a routerLink="/events" routerLinkActive="active">Events</a>
        <a routerLink="/joined-events" routerLinkActive="active">Joined events</a>
        <a routerLink="/created-events" routerLinkActive="active">Created events</a>
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
      </aside>

      <div class="events-content">
        <div class="workspace-header dashboard-hero">
          <div>
            <p class="eyebrow">{{ viewEyebrow }}</p>
            <h1>{{ viewTitle }}</h1>
            <p class="muted">{{ viewSubtitle }}</p>
          </div>
          <button class="button secondary" type="button" (click)="refresh()" [disabled]="loading">
            {{ loading ? 'Loading...' : 'Refresh' }}
          </button>
        </div>

        <div class="metric-grid">
        @if (viewMode === 'events') {
          <article>
            <span>Available events</span>
            <strong>{{ events.length }}</strong>
          </article>
          <article>
            <span>Already joined</span>
            <strong>{{ syncedJoinedEvents.length }}</strong>
          </article>
        } @else if (viewMode === 'joined') {
          <article>
            <span>Joined events</span>
            <strong>{{ syncedJoinedEvents.length }}</strong>
          </article>
          <article>
            <span>Active registrations</span>
            <strong>{{ activeRegistrations.length }}</strong>
          </article>
        } @else if (viewMode === 'created') {
          <article>
            <span>Created events</span>
            <strong>{{ createdEvents.length }}</strong>
          </article>
          <article>
            <span>Published events</span>
            <strong>{{ publishedCreatedEvents }}</strong>
          </article>
        } @else {
          <article>
            <span>Events created</span>
            <strong>{{ createdEvents.length }}</strong>
          </article>
          <article>
            <span>Published events</span>
            <strong>{{ publishedCreatedEvents }}</strong>
          </article>
          <article class="metric-action" (click)="openRegistrationPanel('CONFIRMED')" tabindex="0" role="button" (keydown.enter)="openRegistrationPanel('CONFIRMED')">
            <span>Visible registrations</span>
            <strong>{{ activeOrganizerRegistrations.length }}</strong>
          </article>
          <article class="metric-action" (click)="openRegistrationPanel('WAITLISTED')" tabindex="0" role="button" (keydown.enter)="openRegistrationPanel('WAITLISTED')">
            <span>Waitlisted users</span>
            <strong>{{ waitlistedOrganizerRegistrations }}</strong>
          </article>
        }
        </div>

        @if (message) {
          <p class="message" [class.error]="hasError">{{ message }}</p>
        }

        @if (showDiscover) {
        <section class="panel events-panel discover-panel">
          <div class="section-title discover-title">
            <div>
              <p class="eyebrow">Discover</p>
              <h2>Events</h2>
            </div>
            <label class="search-field">
              <span>Search</span>
              <input name="eventSearch" [(ngModel)]="searchTerm" placeholder="Search title, category or organizer">
            </label>
          </div>

          @if (filteredEvents.length === 0) {
            <p class="empty">No event matches your search yet.</p>
          } @else {
            <div class="event-grid">
              @for (event of filteredEvents; track event.id) {
                <article class="event-card clickable-card" [class.joined]="registrationForEvent(event.id)" [class.owned]="isMyCreatedEvent(event.id)" (click)="openEvent(event)">
                  <img [src]="event.flyer" [alt]="event.title" (error)="useFallbackImage($event)">
                  <div class="event-card-body">
                    <div class="row between">
                      <span class="status" [class.cancelled]="event.status === 'CANCELLED'" [class.completed]="event.status === 'COMPLETED'" [class.draft]="event.status === 'DRAFT'">{{ event.status }}</span>
                      <span class="muted">{{ event.date | date:'dd/MM/yyyy HH:mm' }}</span>
                    </div>
                    <h3>{{ event.title }}</h3>
                    <p>{{ event.description }}</p>
                    <div class="event-meta">
                      <span>{{ seatsLabel(event) }}</span>
                      <span>{{ event.registeredCount || 0 }} registered</span>
                      <span>{{ event.creatorName || 'Organizer' }}</span>
                    </div>
                    <div class="chips">
                      @for (category of event.categories; track category.id) {
                        <span>{{ category.name }}</span>
                      }
                    </div>
                    <div class="row between event-action-row">
                      @if (registrationForEvent(event.id); as registration) {
                        <button class="button success-toggle compact" type="button" (click)="toggleRegistration(event.id, $event)" title="Click to cancel registration">
                          {{ registeredButtonText(registration) }}
                        </button>
                      } @else if (isMyCreatedEvent(event.id)) {
                        <button class="button secondary compact" type="button" disabled>
                          Votre event
                        </button>
                      } @else if (canJoinWaitlist(event)) {
                        <button class="button secondary compact" type="button" (click)="toggleRegistration(event.id, $event)">Join waitlist</button>
                      } @else if (!canJoin(event)) {
                        <button class="button blocked compact" type="button" disabled>
                          {{ joinDisabledText(event) }}
                        </button>
                      } @else {
                        <button class="button primary compact" type="button" (click)="toggleRegistration(event.id, $event)">Join event</button>
                      }
                    </div>
                  </div>
                </article>
              }
            </div>
          }
        </section>
        }

        @if (showMyEvents || showOrganizer) {
        <div class="layout-grid dashboard-grid">
          <section class="main-stack">
            @if (showMyEvents) {
              <section class="panel my-events-panel">
                <div class="section-title">
                  <div>
                    <p class="eyebrow">My events</p>
                    <h2>{{ viewMode === 'joined' ? 'Joined events' : 'Created events' }}</h2>
                  </div>
                </div>

                @if (viewMode === 'joined') {
                  @if (syncedJoinedEvents.length === 0) {
                    <p class="empty">You have not joined any event yet.</p>
                  } @else {
                    <div class="mini-event-list">
                      @for (event of syncedJoinedEvents; track event.id) {
                        <article class="clickable-card" (click)="openEvent(event)">
                          <img [src]="event.flyer" [alt]="event.title" (error)="useFallbackImage($event)">
                          <div>
                            <strong>{{ event.title }}</strong>
                            <span>{{ event.date | date:'dd/MM/yyyy HH:mm' }}</span>
                          </div>
                          @if (registrationForEvent(event.id); as registration) {
                            <button class="button success-toggle compact" type="button" (click)="toggleRegistration(event.id, $event)" title="Click to cancel registration">
                              {{ registeredButtonText(registration) }}
                            </button>
                          }
                        </article>
                      }
                    </div>
                  }
                } @else {
                  @if (createdEvents.length === 0) {
                    <p class="empty">Created events will appear here after publishing.</p>
                  } @else {
                    <div class="mini-event-list">
                      @for (event of createdEvents; track event.id) {
                        <article class="clickable-card" (click)="openEvent(event)">
                          <img [src]="event.flyer" [alt]="event.title" (error)="useFallbackImage($event)">
                          <div>
                            <strong>{{ event.title }}</strong>
                          <span>{{ seatsLabel(event) }} - {{ event.date | date:'dd/MM/yyyy' }}</span>
                          </div>
                          <span class="status" [class.cancelled]="event.status === 'CANCELLED'" [class.completed]="event.status === 'COMPLETED'" [class.draft]="event.status === 'DRAFT'">{{ event.status }}</span>
                          <div class="event-management-actions" (click)="$event.stopPropagation()">
                            @if (canDraftEvent(event)) {
                              <button class="button ghost compact" type="button" (click)="draftEvent(event.id)">Draft</button>
                            }
                            @if (canPublishEvent(event)) {
                              <button class="button primary compact" type="button" (click)="publishEvent(event.id)">Publish</button>
                            }
                            @if (canCancelEvent(event)) {
                              <button class="button danger compact" type="button" (click)="cancelEvent(event.id)">Cancel</button>
                            }
                          </div>
                        </article>
                      }
                    </div>
                  }
                }
              </section>
            }

            @if (showOrganizer) {
              <section class="panel organizer-panel">
                <div class="section-title">
                  <div>
                    <p class="eyebrow">Organizer</p>
                    <h2>Your event activity</h2>
                  </div>
                </div>
                <div class="organizer-stats">
                  <span><strong>{{ createdEvents.length }}</strong> Events created</span>
                  <span><strong>{{ publishedCreatedEvents }}</strong> Published events</span>
                  <button type="button" (click)="openRegistrationPanel('CONFIRMED')"><strong>{{ activeOrganizerRegistrations.length }}</strong> Visible registrations</button>
                  <button type="button" (click)="openRegistrationPanel('WAITLISTED')"><strong>{{ waitlistedOrganizerRegistrations }}</strong> Waitlisted users</button>
                </div>
              </section>

              @if (registrationPanelOpen) {
                <section class="panel registration-panel">
                  <div class="section-title">
                    <div>
                      <p class="eyebrow">Participants</p>
                      <h2>{{ selectedRegistrationStatus === 'WAITLISTED' ? 'Waitlisted users' : 'Visible registrations' }}</h2>
                    </div>
                    <button class="button secondary compact" type="button" (click)="closeRegistrationPanel()">Close</button>
                  </div>

                  @if (createdEvents.length === 0) {
                    <p class="empty">Create an event first to see registrations.</p>
                  } @else {
                    <div class="registration-browser">
                      <div class="registration-events-box">
                        <h3>Choose an event</h3>
                        <div class="registration-event-list">
                          @for (event of createdEvents; track event.id) {
                            <button
                              type="button"
                              [class.active]="selectedRegistrationEventId === event.id"
                              (click)="selectRegistrationEvent(event.id)"
                            >
                              <strong>{{ event.title }}</strong>
                              <span>
                                {{ registrationsForEvent(event.id, selectedRegistrationStatus).length }}
                                {{ selectedRegistrationStatus === 'WAITLISTED' ? 'waitlisted' : 'registered' }}
                              </span>
                            </button>
                          }
                        </div>
                      </div>

                      <div class="registration-results">
                        <div class="registration-results-title">
                          <div>
                            <h3>{{ selectedRegistrationEventTitle || 'Participants for selected event' }}</h3>
                            <span>{{ selectedRegistrationStatus === 'WAITLISTED' ? 'Waitlist' : 'Confirmed registrations' }}</span>
                          </div>
                        </div>

                        @if (!selectedRegistrationEventId) {
                          <p class="empty">Select an event above to see its participants.</p>
                        } @else {
                        <div class="registration-tools">
                          <label class="search-field">
                            <span>Search participant</span>
                            <input name="registrationSearch" [(ngModel)]="registrationSearch" placeholder="Name or email">
                          </label>
                          <span class="registration-count">
                            {{ filteredSelectedEventRegistrations.length }} / {{ selectedEventRegistrations.length }}
                          </span>
                        </div>

                        <div class="registration-user-list">
                          @if (selectedEventRegistrationsLoading) {
                            <p class="empty">Loading registrations...</p>
                          } @else if (selectedEventRegistrations.length === 0) {
                            <p class="empty">No {{ selectedRegistrationStatus === 'WAITLISTED' ? 'waitlisted users' : 'visible registrations' }} for this event.</p>
                          } @else if (filteredSelectedEventRegistrations.length === 0) {
                            <p class="empty">No participant matches this search.</p>
                          } @else {
                            @for (registration of filteredSelectedEventRegistrations; track registration.id) {
                              <article>
                                <div>
                                  <strong>{{ registration.userName || ('User #' + registration.userId) }}</strong>
                                  <span>{{ registration.userEmail || 'Email unavailable' }}</span>
                                </div>
                                <span class="status" [class.success]="registration.status === 'CONFIRMED'">
                                  {{ registration.status }}
                                </span>
                              </article>
                            }
                          }
                        </div>
                        }
                      </div>
                    </div>
                  }
                </section>
              }

              <section class="panel my-events-panel">
                <div class="section-title">
                  <div>
                    <p class="eyebrow">Created events</p>
                    <h2>Organizer list</h2>
                  </div>
                </div>
                @if (createdEvents.length === 0) {
                  <p class="empty">Create your first event from the form on the right.</p>
                } @else {
                  <div class="mini-event-list">
                    @for (event of createdEvents; track event.id) {
                      <article class="clickable-card" (click)="openEvent(event)">
                        <img [src]="event.flyer" [alt]="event.title" (error)="useFallbackImage($event)">
                        <div>
                          <strong>{{ event.title }}</strong>
                          <span>{{ seatsLabel(event) }} - {{ event.date | date:'dd/MM/yyyy HH:mm' }}</span>
                        </div>
                        <span class="status" [class.cancelled]="event.status === 'CANCELLED'" [class.completed]="event.status === 'COMPLETED'" [class.draft]="event.status === 'DRAFT'">{{ event.status }}</span>
                        <div class="event-management-actions" (click)="$event.stopPropagation()">
                          @if (canDraftEvent(event)) {
                            <button class="button ghost compact" type="button" (click)="draftEvent(event.id)">Draft</button>
                          }
                          @if (canPublishEvent(event)) {
                            <button class="button primary compact" type="button" (click)="publishEvent(event.id)">Publish</button>
                          }
                          @if (canCancelEvent(event)) {
                            <button class="button danger compact" type="button" (click)="cancelEvent(event.id)">Cancel</button>
                          }
                        </div>
                      </article>
                    }
                  </div>
                }
              </section>
            }
          </section>

          @if (showOrganizer) {
            <aside class="side-stack">
              <section class="panel create-panel">
                <div class="section-title">
                  <div>
                    <p class="eyebrow">Create</p>
                    <h2>Publish event</h2>
                  </div>
                </div>
                <form class="stack" (ngSubmit)="createEvent()">
                  <label>
                    Title
                    <input name="title" [(ngModel)]="eventForm.title" required>
                  </label>
                  <label>
                    Flyer image
                    <input type="file" accept="image/*" (change)="uploadFlyer($event)" [disabled]="uploadingFlyer">
                  </label>
                  @if (uploadingFlyer) {
                    <p class="upload-note">Uploading flyer to Cloudinary...</p>
                  }
                  @if (eventForm.flyer) {
                    <p class="upload-note success">Flyer ready. You can also replace it with another image.</p>
                  }
                  <label>
                    Flyer URL
                    <input name="flyer" [(ngModel)]="eventForm.flyer" placeholder="Cloudinary URL or https://...">
                  </label>
                  <div class="form-grid">
                    <label>
                      Date and time
                      <input name="date" type="datetime-local" [(ngModel)]="eventForm.date" required>
                    </label>
                    <label>
                      Capacity
                      <input name="capacityMax" type="number" min="1" [(ngModel)]="eventForm.capacityMax" required>
                    </label>
                  </div>
                  <label>
                    Description
                    <textarea name="description" rows="4" [(ngModel)]="eventForm.description" required></textarea>
                  </label>

                  <div>
                    <p class="label-text">Categories</p>
                    <div class="checkbox-list">
                      @if (categories.length === 0) {
                        <p class="empty">No category available.</p>
                      }
                      @for (category of categories; track category.id) {
                        <label class="checkbox-row">
                          <input
                            type="checkbox"
                            [checked]="selectedCategoryIds.includes(category.id)"
                            (change)="toggleCategory(category.id)"
                          >
                          {{ category.name }}
                        </label>
                      }
                    </div>
                  </div>

                  <button class="button primary full" type="submit" [disabled]="uploadingFlyer">
                    {{ uploadingFlyer ? 'Uploading flyer...' : 'Publish Event' }}
                  </button>
                </form>

                <div class="event-card form-preview">
                  <img [src]="eventForm.flyer || fallbackFlyer" alt="Event preview" (error)="useFallbackImage($event)">
                  <div class="event-card-body">
                    <span class="status published">PREVIEW</span>
                    <h3>{{ eventForm.title || 'Your event title' }}</h3>
                    <p>{{ eventForm.description || 'A short event description will appear here.' }}</p>
                  </div>
                </div>
              </section>

              <section class="panel profile-hint-panel">
                <h2>Profile</h2>
                <p class="muted">Open the user icon in the top-right corner to view your account, edit details or log out.</p>
              </section>
            </aside>
          }
        </div>
        }
      </div>

      @if (selectedEvent) {
        <div class="modal-backdrop" (click)="closeEvent()">
          <article class="event-detail-modal" (click)="$event.stopPropagation()">
            <button class="modal-close" type="button" (click)="closeEvent()" aria-label="Close event details">×</button>
            <img class="detail-flyer" [src]="selectedEvent.flyer" [alt]="selectedEvent.title" (error)="useFallbackImage($event)">
            <div class="detail-content">
              <div class="row between detail-heading">
                <span class="status" [class.cancelled]="selectedEvent.status === 'CANCELLED'" [class.completed]="selectedEvent.status === 'COMPLETED'" [class.draft]="selectedEvent.status === 'DRAFT'">{{ selectedEvent.status }}</span>
                <span class="muted">{{ selectedEvent.date | date:'dd/MM/yyyy HH:mm' }}</span>
              </div>
              <h2>{{ selectedEvent.title }}</h2>
              <p>{{ selectedEvent.description }}</p>

              <div class="detail-grid">
                <span><strong>Creator</strong>{{ selectedEvent.creatorName || 'Organizer' }}</span>
                <span><strong>Email</strong>{{ selectedEvent.creatorEmail }}</span>
                <span><strong>Capacity</strong>{{ selectedEvent.capacityMax }} seats</span>
                <span><strong>Available</strong>{{ seatsLabel(selectedEvent) }}</span>
                <span><strong>Registered</strong>{{ selectedEvent.registeredCount || 0 }}</span>
                <span><strong>Date</strong>{{ selectedEvent.date | date:'fullDate' }}</span>
              </div>

              <div class="chips">
                @for (category of selectedEvent.categories; track category.id) {
                  <span>{{ category.name }}</span>
                }
              </div>

              <div class="detail-actions">
                @if (registrationForEvent(selectedEvent.id); as registration) {
                  <button class="button success-toggle" type="button" (click)="toggleRegistration(selectedEvent.id, $event)">
                    {{ registeredButtonText(registration) }} - cancel registration
                  </button>
                } @else if (isMyCreatedEvent(selectedEvent.id)) {
                  <div class="event-management-actions">
                    <button class="button secondary" type="button" disabled>Votre event</button>
                    @if (canDraftEvent(selectedEvent)) {
                      <button class="button ghost" type="button" (click)="draftEvent(selectedEvent.id)">Draft</button>
                    }
                    @if (canPublishEvent(selectedEvent)) {
                      <button class="button primary" type="button" (click)="publishEvent(selectedEvent.id)">Publish</button>
                    }
                    @if (canCancelEvent(selectedEvent)) {
                      <button class="button danger" type="button" (click)="cancelEvent(selectedEvent.id)">Cancel</button>
                    }
                  </div>
                } @else if (!canJoin(selectedEvent)) {
                  @if (canJoinWaitlist(selectedEvent)) {
                    <button class="button secondary" type="button" (click)="toggleRegistration(selectedEvent.id, $event)">Join waitlist</button>
                  } @else {
                    <button class="button blocked" type="button" disabled>{{ joinDisabledText(selectedEvent) }}</button>
                  }
                } @else {
                  <button class="button primary" type="button" (click)="toggleRegistration(selectedEvent.id, $event)">Join event</button>
                }
              </div>
            </div>
          </article>
        </div>
      }
    </section>
  `
})
export class DashboardComponent implements OnInit {
  events: EventItem[] = [];
  createdEvents: EventItem[] = [];
  joinedEvents: EventItem[] = [];
  categories: Category[] = [];
  registrations: Registration[] = [];
  organizerRegistrations: Registration[] = [];
  viewMode: DashboardView = 'events';
  searchTerm = '';
  loading = false;
  uploadingFlyer = false;
  message = '';
  hasError = false;
  selectedEvent: EventItem | null = null;
  registrationPanelOpen = false;
  selectedRegistrationStatus: Extract<RegistrationStatus, 'CONFIRMED' | 'WAITLISTED'> = 'CONFIRMED';
  selectedRegistrationEventId: number | null = null;
  selectedEventRegistrations: Registration[] = [];
  selectedEventRegistrationsLoading = false;
  registrationSearch = '';
  readonly fallbackFlyer = 'https://images.unsplash.com/photo-1505373877841-8d25f7d46678?auto=format&fit=crop&w=900&q=80';

  eventForm = {
    title: '',
    description: '',
    flyer: '',
    date: '',
    capacityMax: 50
  };

  selectedCategoryIds: number[] = [];

  constructor(
    private readonly api: ApiService,
    private readonly route: ActivatedRoute
  ) {}

  get activeRegistrations() {
    const createdEventIds = new Set(this.createdEvents.map((event) => event.id));
    return this.registrations.filter((registration) => registration.status !== 'CANCELLED' && !createdEventIds.has(registration.eventId));
  }

  get activeOrganizerRegistrations() {
    return this.organizerRegistrations.filter((registration) => registration.status === 'CONFIRMED');
  }

  get syncedJoinedEvents() {
    const createdEventIds = new Set(this.createdEvents.map((event) => event.id));
    const activeEventIds = new Set(
      this.activeRegistrations
        .filter((registration) => !createdEventIds.has(registration.eventId))
        .map((registration) => registration.eventId)
    );
    const byId = new Map<number, EventItem>();

    for (const event of this.events) {
      if (activeEventIds.has(event.id)) {
        byId.set(event.id, event);
      }
    }

    for (const event of this.joinedEvents) {
      if (activeEventIds.has(event.id)) {
        byId.set(event.id, event);
      }
    }

    return Array.from(byId.values());
  }

  get showDiscover() {
    return this.viewMode === 'events';
  }

  get showMyEvents() {
    return this.viewMode === 'joined' || this.viewMode === 'created';
  }

  get showOrganizer() {
    return this.viewMode === 'organizer';
  }

  get viewEyebrow() {
    return this.viewMode === 'organizer' ? 'Organizer dashboard' : 'Events workspace';
  }

  get viewTitle() {
    if (this.viewMode === 'joined') {
      return 'Joined events';
    }
    if (this.viewMode === 'created') {
      return 'Created events';
    }
    if (this.viewMode === 'organizer') {
      return 'Manage your created events.';
    }
    return 'Discover and join events.';
  }

  get viewSubtitle() {
    if (this.viewMode === 'joined') {
      return 'All events where your registration is active appear here.';
    }
    if (this.viewMode === 'created') {
      return 'Review the events you created and their publication status.';
    }
    if (this.viewMode === 'organizer') {
      return 'Create events, track your organizer activity and manage your own event list.';
    }
    return 'Browse available events, see details, join or cancel your registration in one click.';
  }

  get filteredEvents() {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) {
      return this.events.filter((event) => event.status !== 'DRAFT');
    }

    return this.events.filter((event) => {
      if (event.status === 'DRAFT') {
        return false;
      }
      const categories = event.categories.map((category) => category.name).join(' ');
      return `${event.title} ${event.description} ${event.creatorName} ${categories}`.toLowerCase().includes(term);
    });
  }

  get publishedCreatedEvents() {
    return this.createdEvents.filter((event) => event.status === 'PUBLISHED').length;
  }

  get waitlistedRegistrations() {
    return this.registrations.filter((registration) => registration.status === 'WAITLISTED').length;
  }

  get waitlistedOrganizerRegistrations() {
    return this.organizerRegistrations.filter((registration) => registration.status === 'WAITLISTED').length;
  }

  registrationsForEvent(eventId: number, status: RegistrationStatus) {
    return this.organizerRegistrations.filter((registration) => registration.eventId === eventId && registration.status === status);
  }

  get filteredSelectedEventRegistrations() {
    const term = this.registrationSearch.trim().toLowerCase();
    if (!term) {
      return this.selectedEventRegistrations;
    }

    return this.selectedEventRegistrations.filter((registration) =>
      `${registration.userName || ''} ${registration.userEmail || ''} ${registration.userId}`.toLowerCase().includes(term)
    );
  }

  get selectedRegistrationEventTitle() {
    return this.createdEvents.find((event) => event.id === this.selectedRegistrationEventId)?.title ?? '';
  }

  ngOnInit() {
    this.route.data.subscribe((data) => {
      this.viewMode = (data['view'] as DashboardView | undefined) ?? 'events';
      this.refresh();
    });
  }

  refresh() {
    this.loading = true;
    this.message = '';
    let pending = 6;
    const done = () => {
      pending -= 1;
      this.loading = pending > 0;
    };

    this.api.events().subscribe({
      next: (page) => this.events = page.content,
      error: () => {
        this.flash('Unable to load events.', true);
        done();
      },
      complete: done
    });

    this.api.myCreatedEvents().subscribe({
      next: (page) => this.createdEvents = page.content,
      error: () => {
        this.createdEvents = [];
        done();
      },
      complete: done
    });

    this.api.myJoinedEvents().subscribe({
      next: (page) => this.joinedEvents = page.content,
      error: () => {
        this.joinedEvents = [];
        done();
      },
      complete: done
    });

    this.api.categories().subscribe({
      next: (page) => this.categories = page.content,
      error: () => {
        this.flash('Unable to load categories.', true);
        done();
      },
      complete: done
    });

    this.api.registrations().subscribe({
      next: (page) => this.registrations = page.content,
      error: () => {
        this.registrations = [];
        done();
      },
      complete: done
    });

    this.api.registrationsForMyCreatedEvents(0, 1000).subscribe({
      next: (page) => {
        this.organizerRegistrations = page.content;
        if (this.registrationPanelOpen && this.selectedRegistrationEventId) {
          this.loadSelectedEventRegistrations();
        }
      },
      error: () => {
        this.organizerRegistrations = [];
        done();
      },
      complete: done
    });
  }

  registrationForEvent(eventId: number) {
    if (this.isMyCreatedEvent(eventId)) {
      return undefined;
    }

    return this.activeRegistrations.find((registration) => registration.eventId === eventId);
  }

  registeredButtonText(registration: Registration) {
    return registration.status === 'WAITLISTED' ? 'En attente' : 'Inscrit';
  }

  seatsLabel(event: EventItem) {
    if (event.availableSeats === null || event.availableSeats === undefined) {
      return `${event.capacityMax} seats`;
    }
    return `${event.availableSeats} seats left`;
  }

  isParticipant(event: EventItem) {
    return Boolean(this.registrationForEvent(event.id));
  }

  isMyCreatedEvent(eventId: number) {
    return this.createdEvents.some((event) => event.id === eventId);
  }

  isEventFull(event: EventItem) {
    return event.status === 'COMPLETED' || (event.availableSeats !== null && event.availableSeats !== undefined && event.availableSeats <= 0);
  }

  canJoin(event: EventItem) {
    return event.status === 'PUBLISHED' && !this.isEventFull(event) && !this.isMyCreatedEvent(event.id);
  }

  canJoinWaitlist(event: EventItem) {
    return (event.status === 'PUBLISHED' || event.status === 'COMPLETED') && this.isEventFull(event) && !this.isMyCreatedEvent(event.id);
  }

  joinDisabledText(event: EventItem) {
    if (event.status === 'COMPLETED' || this.isEventFull(event)) {
      return 'Completed';
    }
    if (event.status === 'CANCELLED') {
      return 'Cancelled';
    }
    if (event.status === 'DRAFT') {
      return 'Draft';
    }
    if (event.status === 'FINISHED') {
      return 'Finished';
    }
    return 'Unavailable';
  }

  canDraftEvent(event: EventItem) {
    return this.isMyCreatedEvent(event.id) && event.status !== 'DRAFT' && event.status !== 'CANCELLED';
  }

  canPublishEvent(event: EventItem) {
    return this.isMyCreatedEvent(event.id) && event.status === 'DRAFT';
  }

  canCancelEvent(event: EventItem) {
    return this.isMyCreatedEvent(event.id) && event.status !== 'CANCELLED';
  }

  openRegistrationPanel(status: Extract<RegistrationStatus, 'CONFIRMED' | 'WAITLISTED'>) {
    this.registrationPanelOpen = true;
    this.selectedRegistrationStatus = status;
    this.selectedRegistrationEventId = null;
    this.selectedEventRegistrations = [];
    this.registrationSearch = '';
  }

  closeRegistrationPanel() {
    this.registrationPanelOpen = false;
    this.selectedEventRegistrations = [];
    this.registrationSearch = '';
  }

  selectRegistrationEvent(eventId: number) {
    this.selectedRegistrationEventId = eventId;
    this.registrationSearch = '';
    this.loadSelectedEventRegistrations();
  }

  toggleCategory(id: number) {
    this.selectedCategoryIds = this.selectedCategoryIds.includes(id)
      ? this.selectedCategoryIds.filter((categoryId) => categoryId !== id)
      : [...this.selectedCategoryIds, id];
  }

  createEvent() {
    const date = this.eventForm.date.length === 16 ? `${this.eventForm.date}:00` : this.eventForm.date;

    this.api.createEvent({
      title: this.eventForm.title,
      description: this.eventForm.description,
      flyer: this.eventForm.flyer || this.fallbackFlyer,
      date,
      capacityMax: Number(this.eventForm.capacityMax),
      categoryIds: this.selectedCategoryIds
    }).subscribe({
      next: () => {
        this.eventForm = { title: '', description: '', flyer: '', date: '', capacityMax: 50 };
        this.selectedCategoryIds = [];
        this.flash('Event published successfully.');
        this.refresh();
      },
      error: () => this.flash('Unable to create the event. Check fields and categories.', true)
    });
  }

  uploadFlyer(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.flash('Please choose an image file for the flyer.', true);
      input.value = '';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.flash('Flyer is too large. Please choose an image under 5 MB.', true);
      input.value = '';
      return;
    }

    if (!this.api.hasCloudinaryConfig()) {
      this.flash('Cloudinary is not configured yet. Add cloudName and uploadPreset in src/environments.', true);
      input.value = '';
      return;
    }

    this.uploadingFlyer = true;
    this.api.uploadFlyer(file).subscribe({
      next: (response) => {
        this.eventForm.flyer = response.secure_url;
        this.flash('Flyer uploaded successfully.');
      },
      error: (error) => {
        const cloudinaryMessage = error?.error?.error?.message || error?.error?.message;
        const statusMessage = error?.status ? `HTTP ${error.status}` : 'Network error';
        console.error('Cloudinary upload failed', error);
        this.flash(cloudinaryMessage ? `Cloudinary upload failed: ${cloudinaryMessage}` : `Unable to upload flyer to Cloudinary (${statusMessage}).`, true);
        this.uploadingFlyer = false;
        input.value = '';
      },
      complete: () => {
        this.uploadingFlyer = false;
        input.value = '';
      }
    });
  }

  openEvent(event: EventItem) {
    this.selectedEvent = event;
    this.api.eventDetails(event.id).subscribe({
      next: (details) => this.selectedEvent = details,
      error: () => this.selectedEvent = event
    });
  }

  closeEvent() {
    this.selectedEvent = null;
  }

  toggleRegistration(eventId: number, domEvent?: Event) {
    domEvent?.stopPropagation();
    const registration = this.registrationForEvent(eventId);
    if (registration) {
      this.unregister(registration.id);
      return;
    }

    const event = this.findEvent(eventId);
    if (!event || (!this.canJoin(event) && !this.canJoinWaitlist(event))) {
      this.flash(event ? `This event is ${this.joinDisabledText(event).toLowerCase()}.` : 'Event unavailable.', true);
      return;
    }

    this.api.registerToEvent(eventId).subscribe({
      next: (registration) => {
        this.flash(`Registration ${registration.status.toLowerCase()} for event ${eventId}.`);
        this.refresh();
        this.refreshSelectedEvent(eventId);
      },
      error: () => {
        this.flash('Registration state refreshed. Try the button again if needed.');
        this.refresh();
        this.refreshSelectedEvent(eventId);
      }
    });
  }

  draftEvent(eventId: number) {
    this.api.draftEvent(eventId).subscribe({
      next: (event) => this.afterEventStatusChange(event, 'Event moved to draft.'),
      error: () => this.flash('Unable to move this event to draft.', true)
    });
  }

  publishEvent(eventId: number) {
    this.api.publishEvent(eventId).subscribe({
      next: (event) => this.afterEventStatusChange(event, event.status === 'COMPLETED' ? 'Event is full and remains completed.' : 'Event published.'),
      error: () => this.flash('Unable to publish this event.', true)
    });
  }

  cancelEvent(eventId: number) {
    this.api.cancelEvent(eventId).subscribe({
      next: (event) => this.afterEventStatusChange(event, 'Event cancelled.'),
      error: () => this.flash('Unable to cancel this event.', true)
    });
  }

  unregister(registrationId: number) {
    this.api.cancelRegistration(registrationId).subscribe({
      next: () => {
        this.flash('Registration cancelled.');
        this.refresh();
        if (this.selectedEvent) {
          this.refreshSelectedEvent(this.selectedEvent.id);
        }
      },
      error: () => this.flash('Unable to leave this event.', true)
    });
  }

  private refreshSelectedEvent(eventId: number) {
    if (!this.selectedEvent || this.selectedEvent.id !== eventId) {
      return;
    }
    this.api.eventDetails(eventId).subscribe({
      next: (details) => this.selectedEvent = details
    });
  }

  private loadSelectedEventRegistrations() {
    if (!this.registrationPanelOpen || !this.selectedRegistrationEventId) {
      this.selectedEventRegistrations = [];
      return;
    }

    this.selectedEventRegistrationsLoading = true;
    this.api.registrationsForMyCreatedEvent(this.selectedRegistrationEventId, this.selectedRegistrationStatus, 0, 500).subscribe({
      next: (page) => this.selectedEventRegistrations = page.content,
      error: () => {
        this.selectedEventRegistrations = this.registrationsForEvent(this.selectedRegistrationEventId!, this.selectedRegistrationStatus);
        this.flash('Unable to load live registrations for this event.', true);
      },
      complete: () => this.selectedEventRegistrationsLoading = false
    });
  }

  private afterEventStatusChange(event: EventItem, message: string) {
    this.upsertEvent(event);
    this.flash(message);
    this.refresh();
    this.refreshSelectedEvent(event.id);
  }

  private upsertEvent(event: EventItem) {
    this.events = this.replaceEvent(this.events, event);
    this.createdEvents = this.replaceEvent(this.createdEvents, event);
    this.joinedEvents = this.replaceEvent(this.joinedEvents, event);
    if (this.selectedEvent?.id === event.id) {
      this.selectedEvent = event;
    }
  }

  private replaceEvent(events: EventItem[], updated: EventItem) {
    return events.some((event) => event.id === updated.id)
      ? events.map((event) => event.id === updated.id ? updated : event)
      : events;
  }

  private findEvent(eventId: number) {
    return [...this.events, ...this.createdEvents, ...this.joinedEvents].find((event) => event.id === eventId);
  }

  useFallbackImage(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = this.fallbackFlyer;
  }

  private flash(message: string, error = false) {
    this.message = message;
    this.hasError = error;
  }
}
