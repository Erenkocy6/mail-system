import { Attachment } from './attachment';
import { User } from './user';

enum MailStatus {
  DRAFT = 'DRAFT',
  SENT = 'SENT',
  RECEIVED = 'RECEIVED',
  ERROR = 'ERROR',
}

enum MailSource{
  INTERN = 'INTERN',
  EXTERN = 'EXTERN',
}

export type Mail = {
  id: string;
  sender: User;
  subject: string;
  content: string;
  status: MailStatus;
  source: MailSource;
  to: User[];
  cc: User[];
  bcc: User[];
  replyTo: User[];
  externalRecipientAddress?: string;
  attachments: Attachment[];
  ticketNumber?: string;
  createdAt: string;
  updatedAt: string;
  sentAt?: string;
};

export type CreateMail = {
  subject: string;
  content: string;
  toIds: string[];
  ccIds: string[];
  bccIds: string[];
  replyToIds: string[];
  externalRecipientAddress?: string;
};

export type UpdateMail = CreateMail;
