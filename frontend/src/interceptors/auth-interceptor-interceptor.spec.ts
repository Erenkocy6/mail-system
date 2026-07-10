import { TestBed } from '@angular/core/testing';
import { HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { of } from 'rxjs';

import { authInterceptorInterceptor } from './auth-interceptor-interceptor';
import { AuthService } from '../services/auth/auth-service';

describe('authInterceptorInterceptor', () => {
  const interceptor: HttpInterceptorFn = (req, next) => 
    TestBed.runInInjectionContext(() => authInterceptorInterceptor(req, next));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useValue: {
            getAccessToken: () => 'test-token',
            logout: () => undefined,
          },
        },
      ],
    });
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('does not resolve AuthService for non-api requests', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useFactory: () => {
            throw new Error('AuthService should not be resolved for non-api requests');
          },
        },
      ],
    });

    const request = new HttpRequest(
      'GET',
      'http://localhost:9080/realms/mail-system/.well-known/openid-configuration',
    );

    TestBed.runInInjectionContext(() => {
      authInterceptorInterceptor(request, () => of());
    });
  });
});
