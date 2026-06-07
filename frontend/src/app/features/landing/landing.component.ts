import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="hero landing-hero">
      <div class="hero-copy">
        <p class="eyebrow">Modern event management</p>
        <h1>Create, manage and discover events easily.</h1>
        <p>
          EventFlow transforme ton API Spring Boot en plateforme SaaS claire:
          decouverte d'evenements, inscriptions, creation, suivi et administration.
        </p>
        <div class="hero-actions">
          <a class="button primary large" routerLink="/events">Explore events</a>
          <a class="button secondary large" routerLink="/auth">Create account</a>
        </div>
      </div>

      <div class="hero-panel app-preview" aria-label="Apercu de l'application">
        <div class="preview-toolbar">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <div class="event-preview-card featured-preview">
          <div class="preview-image"></div>
          <div>
            <span class="status published">PUBLISHED</span>
            <h2>Product Design Summit</h2>
            <p>120 seats, design systems, product strategy and networking.</p>
          </div>
        </div>
        <div class="preview-list">
          <article>
            <span class="preview-dot cyan"></span>
            <div>
              <strong>Angular Workshop</strong>
              <small>WAITLISTED - 48 seats</small>
            </div>
          </article>
          <article>
            <span class="preview-dot violet"></span>
            <div>
              <strong>Startup Night</strong>
              <small>CONFIRMED - 200 seats</small>
            </div>
          </article>
        </div>
        <div class="stats-grid landing-stats">
          <span><strong>JWT</strong> Secure access</span>
          <span><strong>Events</strong> Publish fast</span>
          <span><strong>Waitlist</strong> Smart capacity</span>
          <span><strong>Admin</strong> Global control</span>
        </div>
      </div>
    </section>

    <section class="feature-band">
      <article>
        <span class="feature-icon">01</span>
        <h2>Discover Events</h2>
        <p>Explore upcoming events with clean cards, categories and clear capacity information.</p>
      </article>
      <article>
        <span class="feature-icon">02</span>
        <h2>Join Events</h2>
        <p>Register in one click and see confirmed, waitlisted or cancelled statuses instantly.</p>
      </article>
      <article>
        <span class="feature-icon">03</span>
        <h2>Create Events</h2>
        <p>Publish professional event pages with flyer, date, capacity and category badges.</p>
      </article>
      <article>
        <span class="feature-icon">04</span>
        <h2>Manage Participants</h2>
        <p>Keep a simple operational view of created events, users and registrations.</p>
      </article>
    </section>

  `
})
export class LandingComponent {}
