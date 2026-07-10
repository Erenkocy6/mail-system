import { AuthConfig } from 'angular-oauth2-oidc';

const redirectPath = window.location.port === '8081' ? '/app/' : '/';
const redirectUri = `${window.location.origin}${redirectPath}`;

export const authConfig: AuthConfig = {
  issuer: 'http://localhost:9080/realms/mail-system',
  clientId: 'mail-client',
  responseType: 'code',
  redirectUri,
  postLogoutRedirectUri: redirectUri,
  scope: 'openid profile email',
  requireHttps: false,
  showDebugInformation: true,
};
