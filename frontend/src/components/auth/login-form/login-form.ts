import { Component, inject } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { AuthService } from '../../../services/auth/auth-service';

@Component({
  selector: 'app-login-form',
  imports: [
    ButtonModule,
  ],
  templateUrl: './login-form.html',
})
export class LoginForm {
  private authService = inject(AuthService);

  protected login() {
    this.authService.login();
  }

  protected isAuthenticated(): boolean {
    return this.authService.isAuthenticated();
  }

  protected displayName(): string {
    return this.authService.getDisplayName();
  }
}
