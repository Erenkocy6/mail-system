import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { MessageService } from 'primeng/api';
import { of } from 'rxjs';
import { AuthService } from '../services/auth/auth-service';
import { MailsService } from '../services/mails/mails-service';
import { Mail } from '../types/mails';
import { User } from '../types/user';

export const mockUser: User = {
  id: 'user-1',
  firstName: 'Max',
  lastName: 'Mustermann',
  email: 'max.mustermann@example.com',
};

export const mockMail: Mail = {
  id: 'mail-1',
  sender: mockUser,
  subject: 'Test Mail',
  content: 'This is a test mail.',
  status: 'DRAFT' as Mail['status'],
  source: 'INTERN' as Mail['source'],
  to: [mockUser],
  cc: [],
  bcc: [],
  replyTo: [],
  attachments: [],
  createdAt: '2026-05-07T09:00:00.000Z',
  updatedAt: '2026-05-07T09:00:00.000Z',
};

export function createComponentTestProviders() {
  return [
    provideRouter([]),
    providePrimeNG({
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: false,
        },
      },
    }),
    MessageService,
    {
      provide: AuthService,
      useValue: {
        initialize: () => Promise.resolve(),
        login: () => undefined,
        logout: () => undefined,
        isAuthenticated: () => true,
        getAccessToken: () => 'test-token',
        getIdentityClaims: () => ({ email: mockUser.email, name: 'Max Mustermann' }),
        getDisplayName: () => 'Max Mustermann',
        getEmail: () => mockUser.email,
      },
    },
    {
      provide: MailsService,
      useValue: {
        getIncomingMails: () => of([]),
        getDrafts: () => of([]),
        getSentMails: () => of([]),
        getMailById: () => of(mockMail),
        sendMail: () => of({}),
        deleteMail: () => of({}),
        getAllUsers: () => of([mockUser]),
        createDraft: () => of(mockMail),
        createAndSendMail: () => of(mockMail),
        updateMails: () => of(mockMail),
        fetchAttachment: () => of(new Blob()),
      },
    },
  ];
}
