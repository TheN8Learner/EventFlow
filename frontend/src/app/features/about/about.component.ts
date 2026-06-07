import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="info-page">
      <div class="info-hero">
        <p class="eyebrow">A propos</p>
        <h1>EventFlow simplifie la gestion des evenements.</h1>
        <p>
          EventFlow est une application web Angular connectee a une API Spring Boot pour publier des evenements,
          gerer les inscriptions, suivre les places disponibles et organiser les participants depuis un espace clair.
        </p>
        <a class="button primary large" routerLink="/events">Explorer les events</a>
      </div>

      <div class="info-grid">
        <article>
          <span class="feature-icon">01</span>
          <h2>Pour les participants</h2>
          <p>Decouvrir les events, voir les details, rejoindre ou annuler une inscription facilement.</p>
        </article>
        <article>
          <span class="feature-icon">02</span>
          <h2>Pour les organisateurs</h2>
          <p>Creer des events, ajouter un flyer, suivre les inscriptions et gerer les statuts draft ou cancel.</p>
        </article>
        <article>
          <span class="feature-icon">03</span>
          <h2>Pour les admins</h2>
          <p>Garder une vue globale sur les utilisateurs, events, categories et registrations.</p>
        </article>
      </div>
    </section>
  `
})
export class AboutComponent {}
