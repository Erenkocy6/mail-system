import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailSent } from './mail-sent';
import { createComponentTestProviders } from '../../../testing/component-test-helpers';

describe('MailSent', () => {
  let component: MailSent;
  let fixture: ComponentFixture<MailSent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailSent],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailSent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
