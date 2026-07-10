import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailDetails } from './mail-details';
import { createComponentTestProviders, mockMail } from '../../../testing/component-test-helpers';

describe('MailDetails', () => {
  let component: MailDetails;
  let fixture: ComponentFixture<MailDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MailDetails],
      providers: createComponentTestProviders(),
    })
    .compileComponents();

    fixture = TestBed.createComponent(MailDetails);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('id', mockMail.id);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
