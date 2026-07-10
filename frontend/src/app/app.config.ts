import { ApplicationConfig, importProvidersFrom, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import {provideRouter, withComponentInputBinding} from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { authInterceptorInterceptor } from '../interceptors/auth-interceptor-interceptor';
import { MessageService } from 'primeng/api';
import { OAuthModule } from 'angular-oauth2-oidc';
import { AuthService } from '../services/auth/auth-service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch(), withInterceptors([authInterceptorInterceptor])),
    importProvidersFrom(OAuthModule.forRoot()),
    provideAppInitializer(() => inject(AuthService).initialize()),
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: false,
        },
      },
    }),
    MessageService,
  ],
};
