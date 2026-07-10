import { inject, Injectable } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { authConfig } from '../../app/auth.config';
import { IdentityClaims } from '../../types/auth';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private oauthService = inject(OAuthService);
  private initialization: Promise<void>;

  constructor() {
    this.oauthService.configure(authConfig);
    this.oauthService.setupAutomaticSilentRefresh();
    this.initialization = this.oauthService
      .loadDiscoveryDocumentAndTryLogin()
      .then(() => undefined)
      .catch((error) => {
        console.error('Failed to initialize Keycloak OpenID Connect discovery', error);
      });
  }

  public initialize(): Promise<void> {
    return this.initialization;
  }

  public login(): void {
    this.oauthService.initCodeFlow();
  }

  public logout(): void {
    this.oauthService.logOut();
  }

  public isAuthenticated(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  public getAccessToken(): string {
    return this.oauthService.getAccessToken();
  }

  public getIdentityClaims(): IdentityClaims | null {
    return (this.oauthService.getIdentityClaims() as IdentityClaims | null) ?? null;
  }

  public getDisplayName(): string {
    const claims = this.getIdentityClaims();
    return claims?.name || claims?.preferred_username || claims?.email || 'Authenticated user';
  }

  public getEmail(): string | null {
    return this.getIdentityClaims()?.email ?? null;
  }
}
