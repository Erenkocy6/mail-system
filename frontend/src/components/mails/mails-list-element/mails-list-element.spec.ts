import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailsListElement } from './mails-list-element';
import { createComponentTestProviders, mockMail } from '../../../testing/component-test-helpers';

describe('MailsListElement', () => {
  let component: MailsListElement;
  let fixture: ComponentFixture<MailsListElement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailsListElement],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailsListElement);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('mail', mockMail);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
