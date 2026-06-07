import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email = '';
  password = '';
  constructor(private router: Router) {}
  submit() {
    // implement login with API
    this.router.navigate(['/events']);
  }
}
