import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth-service';
import { OAuthService } from 'angular-oauth2-oidc';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: OAuthService,
          useValue: {
            configure: () => undefined,
            setupAutomaticSilentRefresh: () => undefined,
            loadDiscoveryDocumentAndTryLogin: () => Promise.resolve(),
            initCodeFlow: () => undefined,
            logOut: () => undefined,
            hasValidAccessToken: () => true,
            getAccessToken: () => 'test-token',
            getIdentityClaims: () => ({ email: 'max.mustermann@example.com' }),
          },
        },
      ],
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
