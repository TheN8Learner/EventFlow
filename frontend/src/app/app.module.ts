import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { EventsComponent } from './events/events.component';
import { EventDetailsComponent } from './event-details/event-details.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { MyEventsComponent } from './my-events/my-events.component';
import { CreateEventComponent } from './create-event/create-event.component';
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'events', component: EventsComponent },
  { path: 'events/:id', component: EventDetailsComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'my-events', component: MyEventsComponent },
  { path: 'organiser/events/create', component: CreateEventComponent },
  { path: 'admin/dashboard', component: AdminDashboardComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    EventsComponent,
    EventDetailsComponent,
    LoginComponent,
    RegisterComponent,
    MyEventsComponent,
    CreateEventComponent,
    AdminDashboardComponent
  ],
  imports: [BrowserModule, FormsModule, RouterModule.forRoot(routes)],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
