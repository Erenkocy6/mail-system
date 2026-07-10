import { inject, Injectable } from '@angular/core';
import { AuthService } from './auth-service';
import { CanActivate } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  private authService = inject(AuthService);

  public async canActivate(): Promise<boolean> {
    await this.authService.initialize();
    if (this.authService.isAuthenticated()) {
      return true;
    }

    this.authService.login();
    return false;
  }
}
