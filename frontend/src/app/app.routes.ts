import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { AboutComponent } from './features/about/about.component';
import { AdminComponent } from './features/admin/admin.component';
import { AuthComponent } from './features/auth/auth.component';
import { ContactComponent } from './features/contact/contact.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { LandingComponent } from './features/landing/landing.component';

export const routes: Routes = [
  { path: '', component: LandingComponent },
  { path: 'about', component: AboutComponent },
  { path: 'contact', component: ContactComponent },
  { path: 'auth', component: AuthComponent },
  { path: 'events', component: DashboardComponent, canActivate: [authGuard], data: { view: 'events' } },
  { path: 'joined-events', component: DashboardComponent, canActivate: [authGuard], data: { view: 'joined' } },
  { path: 'created-events', component: DashboardComponent, canActivate: [authGuard], data: { view: 'created' } },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard], data: { view: 'organizer' } },
  { path: 'admin', component: AdminComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
