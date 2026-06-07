import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="info-page contact-page">
      <div class="info-hero">
        <p class="eyebrow">Contact</p>
        <h1>Parlons de ton prochain evenement.</h1>
        <p>
          Une question, une idee ou besoin d'aide pour organiser un event ? Envoie un message et l'equipe EventFlow
          te recontacte.
        </p>
      </div>

      <div class="contact-layout">
        <form class="panel contact-form" (ngSubmit)="submitContact()">
          <label>
            Nom complet
            <input name="name" [(ngModel)]="form.name" required>
          </label>
          <label>
            Email
            <input name="email" type="email" [(ngModel)]="form.email" required>
          </label>
          <label>
            Message
            <textarea name="message" rows="6" [(ngModel)]="form.message" required></textarea>
          </label>
          <button class="button primary full" type="submit">Envoyer</button>
          @if (sent) {
            <p class="message">Message prepare. Tu peux connecter ce formulaire a ton backend plus tard.</p>
          }
        </form>

        <aside class="panel contact-card">
          <h2>EventFlow</h2>
          <p>Gestion des events, inscriptions et dashboards organisateur.</p>
          <a class="button secondary full" routerLink="/events">Voir les events</a>
        </aside>
      </div>
    </section>
  `
})
export class ContactComponent {
  sent = false;

  form = {
    name: '',
    email: '',
    message: ''
  };

  submitContact() {
    this.sent = true;
  }
}
